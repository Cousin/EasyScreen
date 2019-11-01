package net.blancodev.easyscreen;

import net.blancodev.easyscreen.frame.ScreenshotFrame;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EasyScreen {

    private static ScreenshotFrame currentFrame;

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
            BufferedImage image;
            try {
                image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
            } catch (AWTException e) {
                e.printStackTrace();
                return;
            }
            currentFrame = new ScreenshotFrame(image);
        }
    }

    public static String imgToBase64String(final RenderedImage img, final String formatName) {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            ImageIO.write(img, formatName, os);
            return Base64.getEncoder().encodeToString(os.toByteArray());
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

}
