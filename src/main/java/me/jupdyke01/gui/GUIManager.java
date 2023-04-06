package me.jupdyke01.gui;

import me.jupdyke01.FlipMaster;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

public class GUIManager implements MouseListener, KeyListener {

    private final FlipMaster main;
    private String selected = "none";
    private HashMap<String, String> textBoxes = new HashMap<>();
    private final List<Integer> numbers = new ArrayList<>(Arrays.asList(
            KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3,
            KeyEvent.VK_4, KeyEvent.VK_5, KeyEvent.VK_6,
            KeyEvent.VK_7, KeyEvent.VK_8, KeyEvent.VK_9,
            KeyEvent.VK_0,
            KeyEvent.VK_NUMPAD1, KeyEvent.VK_NUMPAD2, KeyEvent.VK_NUMPAD3,
            KeyEvent.VK_NUMPAD4, KeyEvent.VK_NUMPAD5, KeyEvent.VK_NUMPAD6,
            KeyEvent.VK_NUMPAD7, KeyEvent.VK_NUMPAD8, KeyEvent.VK_NUMPAD9,
            KeyEvent.VK_NUMPAD0));


    public GUIManager(FlipMaster main) {
        this.main = main;
        textBoxes.put("min", String.valueOf(main.getAuctionManager().getMinAuctionPrice()));
        textBoxes.put("max", String.valueOf(main.getAuctionManager().getMaxAuctionPrice()));
        textBoxes.put("margin", String.valueOf(main.getAuctionManager().getMargin()));
        textBoxes.put("threshold", String.valueOf(main.getAuctionManager().getThreshold()));
    }

    //900, 650
    public void render(Graphics2D g, long delta) {

        //-------Draw lines and boxes-------
        //Setup
        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke(3));

        //Horizontal One
        g.drawLine(0, 100, 900, 100);
        //Horizontal Two
        g.drawLine(0, 150, 900, 150);
        //Vertical Center
        g.drawLine(450, 100, 450, 650);
        //Title Box Vertical - Goes from 0,0 to 130, 100
        g.drawRect(1, 1, 130 - 1, 100 - 1);
        //Status Box Vertical - Goes from 770, 0 to 900,100
        g.drawRect(770 - 1, 1, 130 - 1, 100 - 1);

        //-------Draw text-------
        //Setup
        g.setColor(Color.BLACK);
        Font currentFont = g.getFont();
        Font bigFont = new Font(currentFont.getFontName(), Font.BOLD, 32);
        Font mediumFont = new Font(currentFont.getFontName(), Font.BOLD, 24);
        Font smallFont = new Font(currentFont.getFontName(), Font.BOLD, 18);
        Font smallestFont = new Font(currentFont.getFontName(), Font.BOLD, 16);

        //Auctions Text
        g.setFont(bigFont);
        if (!main.getAuctionManager().getAuctionStatus().equals("sleep")) {
            GUIUtils.drawCenteredString(g, "Auctions - " + main.getAuctionManager().getAuctionStatus(), 0, 100, 450, 50);
        } else {
            GUIUtils.drawCenteredString(g, "Auctions - Sleeping " + main.getAuctionWindow().getRenderSleep() + "s", 0, 100, 450, 50);
        }
        //Bazzar Text
        g.setFont(bigFont);
        GUIUtils.drawCenteredString(g, "Bazaar(SOON)", 450, 100, 450, 50);
        //Title Box Text
        g.setFont(mediumFont);
        GUIUtils.drawCenteredHorizontalString(g, "Flip", 0, 32, 130);
        GUIUtils.drawCenteredHorizontalString(g, "Master", 0, 60, 130);
        GUIUtils.drawCenteredHorizontalString(g, FlipMaster.version, 0, 88, 130);

        //Power Button
        g.setColor(Color.GRAY);
        g.fillRoundRect(790, 55, 90, 35, 50, 50);
        g.setColor(Color.BLACK);
        GUIUtils.drawCenteredString(g, "Power", 790, 55, 90, 35);

        //Status Text
        g.setFont(smallFont);
        GUIUtils.drawCenteredHorizontalString(g, "Status:", 770, 25, 130);
        if (main.status.equalsIgnoreCase("on")) {
            g.setColor(Color.GREEN);
        } else if (main.status.equalsIgnoreCase("off")) {
            g.setColor(Color.RED);
        }
        GUIUtils.drawCenteredHorizontalString(g, main.status, 770, 46, 130);



        // Draw Text Box Prefixes
        g.setColor(Color.BLACK);
        g.setFont(smallestFont);
        // Min
        GUIUtils.drawCenteredVerticalString(g, "Min", 135, 10, 25);
        // Max
        GUIUtils.drawCenteredVerticalString(g, "Max", 290, 10, 25);
        // Margin
        GUIUtils.drawCenteredVerticalString(g, "$Δ", 138, 45, 25);
        // Threshold
        GUIUtils.drawCenteredVerticalString(g, "Thr", 290, 45, 25);


        //Draw Text Boxes
        g.setColor(Color.BLACK);
        //Min
        if (isValidResult("min")) {
            if (isChanged("min")) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.BLACK);
            }
        } else {
            g.setColor(Color.RED);
        }
        g.drawRect(165, 10, 120, 25);

        //Max
        if (isValidResult("max")) {
            if (isChanged("max")) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.BLACK);
            }
        } else {
            g.setColor(Color.RED);
        }
        g.drawRect(325, 10, 120, 25);

        //Margin
        if (isValidResult("margin")) {
            if (isChanged("margin")) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.BLACK);
            }
        } else {
            g.setColor(Color.RED);
        }
        g.drawRect(165, 45, 120, 25);

        //Threshold
        if (isValidResult("threshold")) {
            if (isChanged("threshold")) {
                g.setColor(Color.BLUE);
            } else {
                g.setColor(Color.BLACK);
            }
        } else {
            g.setColor(Color.RED);
        }
        g.drawRect(325, 45, 120, 25);





        //Fill text boxes
        g.setColor(Color.BLACK);
        //Min
        String minText = textBoxes.get("min");
        if (selected.equalsIgnoreCase("min")) {
            minText += "_";
        }
        GUIUtils.drawCenteredVerticalString(g, minText, 170, 10,25);

        //Max
        String maxText = textBoxes.get("max");
        if (selected.equalsIgnoreCase("max")) {
            maxText += "_";
        }
        GUIUtils.drawCenteredVerticalString(g, maxText, 330, 10,25);

        //Margin
        String marginText = textBoxes.get("margin");
        if (selected.equalsIgnoreCase("margin")) {
            marginText += "_";
        }
        GUIUtils.drawCenteredVerticalString(g, marginText, 170, 45,25);

        //Threshold
        String thresholdText = textBoxes.get("threshold");
        if (selected.equalsIgnoreCase("threshold")) {
            thresholdText += "_";
        }
        GUIUtils.drawCenteredVerticalString(g, thresholdText, 330, 45,25);
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        //Text Boxes
        if (GUIUtils.isClickedRect(e.getX(), e.getY(), 165, 10, 120, 25)) {
            //Min
            selected = "min";
        } else if (GUIUtils.isClickedRect(e.getX(), e.getY(), 325, 10, 120, 25)) {
            //Max
            selected = "max";
        } else if (GUIUtils.isClickedRect(e.getX(), e.getY(), 165, 45, 120, 25)) {
            //Margin
            selected = "margin";
        } else if (GUIUtils.isClickedRect(e.getX(), e.getY(), 325, 45, 120, 25)) {
            //Threshold
            selected = "threshold";
        } else if (GUIUtils.isClickedRect(e.getX(), e.getY(), 790, 55, 90, 35)) {
            //Power
            main.status = Objects.equals(main.status, "On") ? "Off" : "On";
        }
        //Auction house window clicked
        if (GUIUtils.isClickedRect(e.getX(), e.getY(), 0, 150, 450, 500)) {
            main.getAuctionWindow().clicked(e.getX(), e.getY());
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        int tab = KeyEvent.VK_TAB;
        int enter = KeyEvent.VK_ENTER;
        int back_space = KeyEvent.VK_BACK_SPACE;
        int escape = KeyEvent.VK_ESCAPE;

        if (e.getKeyCode() == tab) {
            if (selected.equalsIgnoreCase("none")) {
                selected = "min";
            } else if (selected.equalsIgnoreCase("min")) {
                selected = "max";
            } else if (selected.equalsIgnoreCase("max")) {
                selected = "margin";
            } else if (selected.equalsIgnoreCase("margin")) {
                selected = "threshold";
            } else if (selected.equalsIgnoreCase("threshold")) {
                selected = "min";
            }
            return;
        }
        if (e.getKeyCode() == enter) {
            if (selected.equalsIgnoreCase("none")) {
                return;
            }
            if (isValidResult(selected) && isChanged(selected)) {
                if (selected.equalsIgnoreCase("min")) {
                    int result = Integer.parseInt(textBoxes.get(selected));
                    main.getAuctionManager().setMinAuctionPrice(result);
                } else if (selected.equalsIgnoreCase("max")) {
                    int result = Integer.parseInt(textBoxes.get(selected));
                    main.getAuctionManager().setMaxAuctionPrice(result);
                } else if (selected.equalsIgnoreCase("margin")) {
                    int result = Integer.parseInt(textBoxes.get(selected));
                    main.getAuctionManager().setMargin(result);
                } else if (selected.equalsIgnoreCase("threshold")) {
                    double result = Double.parseDouble(textBoxes.get(selected));
                    main.getAuctionManager().setThreshold(result);
                }
            }
            return;
        }

        if (selected.equalsIgnoreCase("") || selected.equalsIgnoreCase("none")) {
            return;
        }
        if (e.getKeyCode() == escape) {
            selected = "none";
            return;
        }
        if (e.getKeyCode() == back_space && textBoxes.get(selected).length() > 0) {
            textBoxes.put(selected, textBoxes.get(selected).substring(0, textBoxes.get(selected).length() - 1));
            return;
        }
        if (isValidInput(e.getKeyCode())) {
            textBoxes.put(selected, textBoxes.get(selected) + e.getKeyChar());
        }

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    public boolean isValidInput(int keycode) {
        int decimal = KeyEvent.VK_PERIOD;
        if (keycode == decimal) {
            return selected.equalsIgnoreCase("threshold");
        }
        return numbers.contains(keycode);
    }

    public boolean isValidResult(String box) {
        String value = textBoxes.get(box);
        if (value.length() > 12) {
            return false;
        }

        if (box.equalsIgnoreCase("min") || box.equalsIgnoreCase("max") || box.equalsIgnoreCase("margin")) {
            int result;
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return false;
            }
            return result >= 0;
        } else if (box.equalsIgnoreCase("threshold")) {
            double result;
            try {
                result = Double.parseDouble(value);
            } catch(NumberFormatException e) {
                return false;
            }
            return result >= 0;
        }
        return false;
    }

    public boolean isChanged(String box) {
        String value = textBoxes.get(box);
        if (box.equalsIgnoreCase("min")) {
            int result = Integer.parseInt(value);
            return main.getAuctionManager().getMinAuctionPrice() != result;
        } else if (box.equalsIgnoreCase("max")) {
            int result = Integer.parseInt(value);
            return main.getAuctionManager().getMaxAuctionPrice() != result;
        } else if (box.equalsIgnoreCase("margin")) {
            int result = Integer.parseInt(value);
            return main.getAuctionManager().getMargin() != result;
        } else if (box.equalsIgnoreCase("threshold")) {
            double result = Double.parseDouble(value);
            return main.getAuctionManager().getThreshold() != result;
        }
        return false;
    }
}
