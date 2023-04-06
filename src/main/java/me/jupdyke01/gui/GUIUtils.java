package me.jupdyke01.gui;

import java.awt.*;

public class GUIUtils {

    public static void drawCenteredString(Graphics g, String text, int x, int y, int width, int height) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - textHeight) / 2 + fm.getAscent();
        g.drawString(text, textX, textY);
    }

    public static void drawCenteredHorizontalString(Graphics g, String text, int x, int y, int width) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textX = x + (width - textWidth) / 2;
        g.drawString(text, textX, y);
    }

    public static void drawCenteredVerticalString(Graphics g, String text, int x, int y, int height) {
        FontMetrics fm = g.getFontMetrics();
        int textHeight = fm.getHeight();
        int textY = y + (height - textHeight) / 2 + fm.getAscent();
        g.drawString(text, x, textY);
    }

    public static boolean isClickedRect(int mouseX, int mouseY, int x, int y, int width, int height) {
        if (mouseX > x && mouseX < x + width) {
            return mouseY > y && mouseY < y + height;
        }
        return false;
    }

}
