package edu.sfsu.csc413.chess.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Every colour, font and shared brush of the Soft Club window, in one place.
 *
 * <p>The look is late-'90s ambient: pale sky, ice, frosted white and silver,
 * with one small lime accent. Nothing here is saturated. Keeping the palette
 * in one class means a view class never writes a colour literal, so changing
 * the mood is a change to this file only.
 */
public final class SoftClubTheme {

    public static final Color SKY_TOP = new Color(0xEAF4FB);
    public static final Color SKY_BOTTOM = new Color(0xBFD9EC);
    public static final Color ICE = new Color(0xF3F9FD);
    public static final Color POWDER = new Color(0xAFC6DC);
    public static final Color SILVER = new Color(0xC9D3DC);
    public static final Color FROST = new Color(255, 255, 255, 140);
    public static final Color FROST_EDGE = new Color(255, 255, 255, 200);
    public static final Color GLOSS = new Color(255, 255, 255, 120);
    public static final Color SLATE = new Color(0x4A5A6B);
    public static final Color SLATE_SOFT = new Color(0x7B8B9B);
    public static final Color SHADOW = new Color(255, 255, 255, 150);
    public static final Color AQUA_GLOW = new Color(120, 225, 235, 120);
    public static final Color AQUA_CORE = new Color(90, 205, 220, 190);
    public static final Color LIME_HINT = new Color(170, 230, 120, 170);
    public static final Color CHECK_GLOW = new Color(215, 245, 140, 150);

    /** Families tried in order; the first one this machine has is used. */
    private static final String[] TEXT_FAMILIES =
            {"Helvetica Neue", "Helvetica", "Verdana", "SansSerif"};
    private static final String[] PIECE_FAMILIES =
            {"Apple Symbols", "Segoe UI Symbol", "DejaVu Sans", "Arial Unicode MS", "SansSerif"};

    private static final Set<String> INSTALLED = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

    private SoftClubTheme() {
    }

    /** A small, widely spaced label font; the tracking is what gives the CD-insert feel. */
    public static Font label(float size) {
        Font base = new Font(firstInstalled(TEXT_FAMILIES), Font.PLAIN, 12).deriveFont(size);
        return base.deriveFont(Map.of(TextAttribute.TRACKING, 0.14f));
    }

    /** The font the chess figures are drawn in. */
    public static Font piece(float size) {
        return new Font(firstInstalled(PIECE_FAMILIES), Font.PLAIN, 12).deriveFont(size);
    }

    /** Turns on the smoothing every painted surface here depends on. */
    public static void smooth(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    /**
     * A frosted plastic panel: white fading to pale blue, a 1px light border,
     * and a glossy sheen over the top half.
     */
    public static void paintFrosted(Graphics2D g, double x, double y, double w, double h, double arc) {
        RoundRectangle2D shape = new RoundRectangle2D.Double(x, y, w - 1, h - 1, arc, arc);
        g.setPaint(new GradientPaint(0, (float) y, new Color(255, 255, 255, 190),
                0, (float) (y + h), new Color(205, 226, 242, 150)));
        g.fill(shape);
        paintGloss(g, x, y, w, h, arc);
        g.setPaint(FROST_EDGE);
        g.draw(shape);
    }

    /** The highlight on the upper half of a surface, like moulded plastic. */
    public static void paintGloss(Graphics2D g, double x, double y, double w, double h, double arc) {
        RoundRectangle2D sheen = new RoundRectangle2D.Double(x + 2, y + 2, w - 5, h / 2 - 2, arc, arc);
        g.setPaint(new GradientPaint(0, (float) y, GLOSS, 0, (float) (y + h / 2),
                new Color(255, 255, 255, 0)));
        g.fill(sheen);
    }

    private static String firstInstalled(String[] families) {
        for (String family : families) {
            if (INSTALLED.contains(family)) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }
}
