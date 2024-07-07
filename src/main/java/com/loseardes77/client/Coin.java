package com.loseardes77.client;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;

public class Coin extends JLabel {
    public Coin(Rectangle bounds) {
        setText("O");
        setFont(new Font("Arial", Font.BOLD, 16));
        setForeground(new Color(255, 255, 0));
        setBounds(bounds);
    }
}
