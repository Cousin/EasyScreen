package net.blancodev.easyscreen;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

// Listener class for checking if the prntscr button was pressed, even if program isn't in focus
public class KeyboardListener implements NativeKeyListener {

    private ScreenshotFrame currentFrame;

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        if (nativeKeyEvent.getKeyCode() == 3666) {
            if (currentFrame == null || !currentFrame.isVisible()) {
                BufferedImage image = null;
                try {
                    image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
                } catch (AWTException e) {
                    e.printStackTrace();
                    return;
                }
                currentFrame = new ScreenshotFrame(image);
            }
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {

    }

}
