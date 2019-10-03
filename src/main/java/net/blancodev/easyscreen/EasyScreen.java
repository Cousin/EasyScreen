package net.blancodev.easyscreen;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EasyScreen {

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

    }

}
