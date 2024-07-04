package com.loseardes77.common;

import javax.swing.JButton;
import java.awt.*;

public class Wall extends JButton {
    public Wall(int x, int y, int width, int height) {
        setFocusable(false);
        setBackground(new Color(0x333333));
        setBounds(x, y, width, height);
    }
}
