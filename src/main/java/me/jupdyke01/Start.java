package me.jupdyke01;

import javax.swing.*;
import java.awt.*;

public class Start {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flip Master");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        FlipMaster main = new FlipMaster();
        frame.add(main);
        frame.pack();
        frame.setVisible(true);
        main.start();
        System.out.println("Test");
    }

}
