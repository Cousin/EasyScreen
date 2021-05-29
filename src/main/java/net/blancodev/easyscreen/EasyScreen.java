package net.blancodev.easyscreen;

import javafx.application.Platform;
import javafx.stage.FileChooser;
import net.blancodev.easyscreen.frame.OCRFrame;
import net.blancodev.easyscreen.frame.ScreenshotFrame;
import net.blancodev.easyscreen.frame.UploadFrame;
import net.blancodev.easyscreen.render.ImageButton;
import net.sourceforge.tess4j.Tesseract;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import javax.imageio.ImageIO;
import javax.print.*;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EasyScreen {

    private static ScreenshotFrame currentFrame;

    private static Tesseract tesseract;

    private static final Set<JFrame> openFrames = new HashSet<>();

    public static final Robot ROBOT = createRobot();

    public static void main(String[] args) throws Exception {

        // Disable logs (there's a lot)
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        // Register native hook
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException x) {
            x.printStackTrace();
        }

        tesseract = new Tesseract();
        tesseract.setDatapath("C:\\Users\\joema\\Desktop\\IdeaProjects\\EasyScreen\\src\\main\\resources\\tessdata");

        // Register listeners
        GlobalScreen.addNativeKeyListener(new KeyboardListener());

        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = loadFromResource("icon.png");

            PopupMenu popup = new PopupMenu();

            MenuItem takeScreenshotButton = new MenuItem("Take Screenshot");
            takeScreenshotButton.addActionListener(actionEvent -> requestScreenshot());
            popup.add(takeScreenshotButton);

            TrayIcon trayIcon = new TrayIcon(image, "Tray Demo", popup);
            trayIcon.setImageAutoSize(true);

            try {
                tray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println(e);
            }
        }

    }

    public static void requestScreenshot() {
        if (currentFrame == null || !currentFrame.isVisible()) {
            BufferedImage image = EasyScreen.ROBOT.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            currentFrame = new ScreenshotFrame(image);
            System.out.println(currentFrame.toString());
        }
    }

    public static String imgToBase64String(final RenderedImage img, final String formatName) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ImageIO.write(img, formatName, Base64.getEncoder().wrap(os));
            return os.toString(StandardCharsets.ISO_8859_1.name());
        } catch (final IOException ioe) {
            return "";
        }
    }

    public static BufferedImage loadFromResource(String fileName) {
        try {
            return ImageIO.read(EasyScreen.class.getResource("/" + fileName));
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFileExtension(File file) {
        String[] split = file.getAbsolutePath().split("\\.");
        if (split.length > 0) {
            return split[split.length - 1];
        }

        return "";
    }

    public static void closeFrame(JFrame frame) {
        frame.setVisible(false);
        frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        getOpenFrames().remove(frame);
    }

    public static ImageButton[] IMAGE_BUTTONS = {
            new ImageButton(
                    EasyScreen.loadFromResource("clipboard.png"),
                    EasyScreen.loadFromResource("clipboardhover.png"),
                    "Copy the image to clipboard", (frame) -> {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(frame.getSelectionRectangle().getSelectionImage()), null);
                closeFrame(frame);
            }),
            new ImageButton(
                    EasyScreen.loadFromResource("save.png"),
                    EasyScreen.loadFromResource("savehover.png"),
                    "Save the image to your computer", frame -> {
                // emulate window modality

                if (frame.isJavaFXStillUsable()) {
                    Platform.runLater(() -> {

                        frame.setEnabled(false);

                        FileChooser fileChooser = new FileChooser();

                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"));
                        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG (*.jpg, *.jpeg)", "*.jpg", "*.jpeg"));

                        File file = fileChooser.showSaveDialog(null);

                        if (file != null) { // return true if file was opened correctly
                            try {
                                ImageIO.write(frame.getSelectionRectangle().getSelectionImage(), EasyScreen.getFileExtension(file), file);
                                closeFrame(frame);
                            } catch (IOException e) {
                                System.out.println("Error saving file (check file permissions)");
                                frame.setEnabled(true);
                                frame.requestFocus();
                            }
                        }

                    });
                } else {
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG (*.jpg, *.jpeg)", "jpg,jpeg"));
                    fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[0]);
                    fileChooser.setDialogTitle("Save Screenshot");
                    if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {

                        File file = fileChooser.getSelectedFile();

                        if (file != null) { // return true if file was opened correctly
                            try {
                                System.out.println("saved " + file.getAbsolutePath());
                                String extension = EasyScreen.getFileExtension(file);
                                if (extension.equals("")) {
                                    extension = "png";
                                }
                                ImageIO.write(frame.getSelectionRectangle().getSelectionImage(), extension, file);
                                closeFrame(frame);
                            } catch (IOException e) {
                                System.out.println("Error saving file (check file permissions)");
                            }
                        }
                    }
                }
            }),
            new ImageButton(
                    EasyScreen.loadFromResource("upload.png"),
                    EasyScreen.loadFromResource("uploadhover.png"),
                    "Upload screenshot to EasyScreen", (frame) -> {
                closeFrame(frame);
                new UploadFrame(EasyScreen.imgToBase64String(
                        frame.getSelectionRectangle().getSelectionImage(), "png"
                ), false);
            }),
            new ImageButton(
                    EasyScreen.loadFromResource("upload.png"),
                    EasyScreen.loadFromResource("uploadhover.png"),
                    "Read all text from image", (frame -> {

                closeFrame(frame);
                new OCRFrame(frame.getSelectionRectangle().getSelectionImage());

            })
            ),
            new ImageButton(
                    EasyScreen.loadFromResource("upload.png"),
                    EasyScreen.loadFromResource("uploadhover.png"),
                    "Search Google for similar images", (frame -> {

                closeFrame(frame);
                new UploadFrame(EasyScreen.imgToBase64String(
                        frame.getSelectionRectangle().getSelectionImage(), "png"
                ), true);

            })
            ),
            new ImageButton(
                    EasyScreen.loadFromResource("upload.png"),
                    EasyScreen.loadFromResource("uploadhover.png"),
                    "Print Image", (frame -> {

                closeFrame(frame);

                BufferedImage image = frame.getSelectionRectangle().getSelectionImage();

                PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
                pras.add(new Copies(1));
                PrintService pss[] = PrintServiceLookup.lookupPrintServices(DocFlavor.INPUT_STREAM.GIF, pras);
                if (pss.length == 0)
                    throw new RuntimeException("No printer services available.");
                PrintService ps = pss[0];
                System.out.println("Printing to " + ps);
                DocPrintJob job = ps.createPrintJob();
                Doc doc = new SimpleDoc(image, DocFlavor.INPUT_STREAM.PNG, null);
                try {
                    job.print(doc, pras);
                } catch (PrintException e) {
                    e.printStackTrace();
                }

            })
            ),
            new ImageButton(
                    EasyScreen.loadFromResource("upload.png"),
                    EasyScreen.loadFromResource("uploadhover.png"),
                    "Paintbrush", (frame -> {
                        frame.setPainting(!frame.isPainting());
                    }
            ))
    };

    public static int print(BufferedImage image, Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
        if (pageIndex > 0) {
            return (Printable.NO_SUCH_PAGE);
        } else {
            double pageHeight = pageFormat.getImageableHeight(), pageWidth = pageFormat.getImageableWidth();

            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            if (pageHeight < image.getHeight() || pageWidth < image.getWidth()) {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.drawImage(image, 0, 0, (int) pageWidth, (int) pageHeight, null);
            } else {
                g2d.drawImage(image, 0, 0, null);
            }
            g2d.dispose();
            return (Printable.PAGE_EXISTS);
        }
    }

    private static Robot createRobot() {
        try {
            return new Robot();
        } catch (AWTException e) {
            return null;
        }
    }

    public static Set<JFrame> getOpenFrames() {
        return openFrames;
    }

    public static Tesseract getTesseract() {
        return tesseract;
    }
}
