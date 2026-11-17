package edu.sfsu.csc413.chess.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JButton;

/**
 * A pill-shaped button in glossy pale plastic, painted by hand.
 *
 * <p>It is a plain {@link JButton}, so keyboard and mouse behaviour stay
 * Swing's own; only the painting is replaced.
 */
public class SoftClubButton extends JButton {

    private static final double ARC = 18;

    public SoftClubButton(String text) {
        super(text);
        setFont(SoftClubTheme.label(12f));
        setForeground(SoftClubTheme.SLATE);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(170, 34));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        SoftClubTheme.smooth(g);
        int w = getWidth();
        int h = getHeight();
        boolean pressed = getModel().isPressed();
        boolean hover = getModel().isRollover();

        Color top = pressed ? new Color(0xD3E4F1) : hover ? Color.WHITE : new Color(0xF6FBFE);
        Color bottom = pressed ? new Color(0xBBD3E5) : new Color(0xC6DCEC);
        RoundRectangle2D shape = new RoundRectangle2D.Double(0, 0, w - 1, h - 1, ARC, ARC);
        g.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g.fill(shape);
        SoftClubTheme.paintGloss(g, 0, 0, w, h, ARC);
        g.setPaint(SoftClubTheme.FROST_EDGE);
        g.draw(shape);

        g.setFont(getFont());
        g.setColor(SoftClubTheme.SLATE);
        FontMetrics fm = g.getFontMetrics();
        int x = (w - fm.stringWidth(getText())) / 2;
        int y = (h - fm.getHeight()) / 2 + fm.getAscent();
        g.drawString(getText(), x, y);
        g.dispose();
    }
}
