package net.blancodev.easyscreen;

import net.blancodev.easyscreen.frame.ScreenshotFrame;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.event.KeyListener;

// Listener class for checking if the prntscr button was pressed, even if program isn't in focus
public class KeyboardListener implements NativeKeyListener {

    private ScreenshotFrame currentFrame;

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
        if (nativeKeyEvent.getKeyCode() == NativeKeyEvent.VC_SCROLL_LOCK) {
            EasyScreen.requestScreenshot();
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {

    }

}
