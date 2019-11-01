package net.blancodev.easyscreen.frame;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.FileChooser;
import net.blancodev.easyscreen.*;
import net.blancodev.easyscreen.render.ImageButton;
import net.blancodev.easyscreen.render.SelectionRectangle;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class ScreenshotFrame extends JFrame {

    public ScreenshotFrame(BufferedImage bufferedImage) {

        // Setup frame
        setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);

        SelectionRectangle selectionRectangle = new SelectionRectangle(this, bufferedImage, new Rectangle(100, 100, 500, 500));

        // Store the renderable image buttons and their functions
        ImageButton[] imageButtons = {
                new ImageButton(
                        EasyScreen.loadFromResource("clipboard.png"),
                        EasyScreen.loadFromResource("clipboardhover.png"),
                        "Copy the image to clipboard", () -> {
                            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new TransferableImage(selectionRectangle.getSelectionImage()), null);
                            setVisible(false);
                            dispatchEvent(new WindowEvent(ScreenshotFrame.this, WindowEvent.WINDOW_CLOSING));
                        }
                ),
                new ImageButton(
                        EasyScreen.loadFromResource("save.png"),
                        EasyScreen.loadFromResource("savehover.png"),
                        "Save the image to your computer", () -> {
                            // emulate window modality

                            if (isJavaFXStillUsable()) {
                                Platform.runLater(() -> {

                                    setEnabled(false);

                                    FileChooser fileChooser = new FileChooser();

                                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG (*.png)", "*.png"));
                                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JPG (*.jpg, *.jpeg)", "*.jpg", "*.jpeg"));

                                    File file = fileChooser.showSaveDialog(null);

                                    if (file != null) { // return true if file was opened correctly
                                        try {
                                            ImageIO.write(selectionRectangle.getSelectionImage(), EasyScreen.getFileExtension(file), file);
                                            setVisible(false);
                                            dispatchEvent(new WindowEvent(ScreenshotFrame.this, WindowEvent.WINDOW_CLOSING));
                                        } catch (IOException e) {
                                            System.out.println("Error saving file (check file permissions)");
                                        }
                                    }

                                    setEnabled(true);
                                    requestFocus();
                                });
                            } else {
                                JFileChooser fileChooser = new JFileChooser();
                                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG (*.png)", "png"));
                                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG (*.jpg, *.jpeg)", "jpg,jpeg"));
                                fileChooser.setFileFilter(fileChooser.getChoosableFileFilters()[0]);
                                fileChooser.setDialogTitle("Save Screenshot");
                                if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

                                    File file = fileChooser.getSelectedFile();

                                    if (file != null) { // return true if file was opened correctly
                                        try {
                                            System.out.println("saved " + file.getAbsolutePath());
                                            String extension = EasyScreen.getFileExtension(file);
                                            if (extension.equals("")) {
                                                extension = "png";
                                            }
                                            ImageIO.write(selectionRectangle.getSelectionImage(), extension, file);
                                            setVisible(false);
                                            dispatchEvent(new WindowEvent(ScreenshotFrame.this, WindowEvent.WINDOW_CLOSING));
                                        } catch (IOException e) {
                                            System.out.println("Error saving file (check file permissions)");
                                        }
                                    }
                                }
                            }
                        }
                ),
                new ImageButton(
                        EasyScreen.loadFromResource("upload.png"),
                        EasyScreen.loadFromResource("uploadhover.png"),
                        "Upload screenshot to EasyScreen", () -> {
                            setVisible(false);
                            dispatchEvent(new WindowEvent(ScreenshotFrame.this, WindowEvent.WINDOW_CLOSING));
                            new UploadFrame(EasyScreen.imgToBase64String(
                                    selectionRectangle.getSelectionImage(), "png"
                            ));
                    }
                )
        };

        JPanel jPanel;

        // Setup main JPanel which renders the whole selection area
        add(jPanel = new JPanel() {
            @Override
            public void paintComponent(Graphics graphics) {
                Toolkit.getDefaultToolkit().sync();
                graphics.drawImage(selectionRectangle.render(), 0, 0, this);
                int index = 0;
                for (ImageButton imageButton : imageButtons) {
                    imageButton.setX((int) selectionRectangle.getRectangle().getMaxX() - 25 - (index * 25));
                    imageButton.setY((int) selectionRectangle.getRectangle().getMaxY() + 5);
                    graphics.drawImage(imageButton.isHovering() ? imageButton.getHoverImage() : imageButton.getImage(), imageButton.getX(), imageButton.getY(),ScreenshotFrame.this);
                    if (imageButton.isHovering()) {

                        int boxWidth = graphics.getFontMetrics().stringWidth(imageButton.getTooltip()) + 10;

                        graphics.setColor(Color.WHITE);
                        graphics.fillRect(imageButton.getX(), imageButton.getY() + 30, boxWidth, 20);

                        graphics.setColor(Color.BLACK);
                        graphics.drawString(imageButton.getTooltip(), imageButton.getX() + 5, imageButton.getY() + 44);

                    }
                    index++;
                }
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);

        // Mouse listener for dragging selection box
        jPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                selectionRectangle.onMouseMove(mouseEvent.getPoint());
                Arrays.stream(imageButtons).forEach(i -> {
                    i.handleHover(mouseEvent.getX(), mouseEvent.getY());
                });
                repaint();
            }

            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                selectionRectangle.onMouseDrag(mouseEvent.getPoint());
                repaint();
            }
        });

        // Mouse listeners
        jPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                selectionRectangle.onMousePress(mouseEvent.getPoint());
            }

            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                Arrays.stream(imageButtons).forEach(i -> {
                    i.handleClick(mouseEvent.getX(), mouseEvent.getY());
                });
            }
        });

        // Key listener to check for escape to close
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    setVisible(false);
                    dispatchEvent(new WindowEvent(ScreenshotFrame.this, WindowEvent.WINDOW_CLOSING));
                }
            }
        });

    }

    private boolean isJavaFXStillUsable() {
        try {
            final JFXPanel dummyForToolkitInitialization = new JFXPanel(); // Initializes the Toolkit required by JavaFX, as stated in the docs of Platform.runLater()
        } catch (IllegalStateException ise) {
            return false;
        }
        return true;
    }

}
