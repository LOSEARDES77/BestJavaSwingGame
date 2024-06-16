package com.loseardes77.common;

import javax.swing.JButton;

public class Wall extends JButton {
    public Wall(int x, int y, int width, int height) {
        setEnabled(false);
        setFocusable(false);
        setBounds(x, y, width, height);
    }
}
