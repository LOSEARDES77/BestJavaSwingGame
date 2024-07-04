package com.loseardes77.common;

import com.formdev.flatlaf.FlatDarkLaf;
import static com.loseardes77.common.Logger.info;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {

        Logger.setDebug(true);

        String os = System.getProperty("os.name") == null ? "" : System.getProperty("os.name").toLowerCase();
        String vendor = System.getenv("__GLX_VENDOR_LIBRARY_NAME") == null ? "" : System.getenv("__GLX_VENDOR_LIBRARY_NAME").toLowerCase();

        if (os.equals("linux") && !vendor.equals("nvidia")) {
            System.setProperty("sun.java2d.opengl", "True");
            info("Enable OpenGL");
        }

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MainMenu mainMenu = new MainMenu();

        mainMenu.showMainMenu();
    }
}
