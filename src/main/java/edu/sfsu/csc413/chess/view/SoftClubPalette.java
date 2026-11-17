package edu.sfsu.csc413.chess.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.awt.font.TextAttribute;
import java.util.Map;

/**
 * Colours, type, and the one surface treatment shared by the soft-club window.
 *
 * <p>Every painted colour in that view comes from here. The look is a late-1990s
 * plastic: pale sky, ice, frosted white, and a little lime. Nothing else in the
 * program should hard-code a colour for this window.
 */
public final class SoftClubPalette {

    /** Top of the window wash, nearly white. */
    public static final Color MIST = new Color(0xF7FBFE);

    /** Bottom of the window wash, pale sky blue. */
    public static final Color SKY = new Color(0xD3E4F1);

    /** Frosted panel fill, drawn under a translucent composite. */
    public static final Color FROST = new Color(0xF4F8FB);

    /** Light square, top of its gradient. */
    public static final Color SQUARE_LIGHT_TOP = new Color(0xFBFDFE);

    /** Light square, bottom of its gradient. */
    public static final Color SQUARE_LIGHT_BOTTOM = new Color(0xE4EEF5);

    /** Dark square, a muted powder blue. */
    public static final Color SQUARE_DARK_TOP = new Color(0xD5E2EC);

    /** Dark square, slightly deeper silver-blue. */
    public static final Color SQUARE_DARK_BOTTOM = new Color(0xC3D3E0);

    /** Thin light edge on a rounded surface. */
    public static final Color LINE = new Color(0xC5D5E2);

    /** Body text. Soft slate, never pure black. */
    public static final Color INK = new Color(0x4A5966);

    /** Secondary text: coordinates, the small banner. */
    public static final Color INK_SOFT = new Color(0x6E7E8C);

    /** White piece, mid slate so the outline glyph still reads. */
    public static final Color PIECE_LIGHT = new Color(0x3E5160);

    /** Black piece. */
    public static final Color PIECE_DARK = new Color(0x243038);

    /** Faint light halo behind a glyph. */
    public static final Color PIECE_HALO = new Color(255, 255, 255, 170);

    /** Selected square, opaque aqua; the board draws it translucent. */
    public static final Color AQUA = new Color(0x7EC4BE);

    /** King in check, a soft lime wash. */
    public static final Color LIME = new Color(0xC6D98E);

    /** Legal-move dot. */
    public static final Color LIME_DOT = new Color(168, 206, 122, 210);

    /** Wider, fainter glow under a legal-move dot. */
    public static final Color LIME_GLOW = new Color(186, 220, 150, 100);

    /** Ring on a legal capture, so the piece underneath stays visible. */
    public static final Color LIME_RING = new Color(140, 186, 112, 180);

    /** Button face, top. */
    public static final Color BUTTON_TOP = new Color(0xF7FBFE);

    /** Button face, bottom. */
    public static final Color BUTTON_BOTTOM = new Color(0xD5E4EF);

    /** Button face while the pointer is over it. */
    public static final Color BUTTON_HOVER_TOP = new Color(0xFFFFFF);

    /** Button face while the pointer is over it. */
    public static final Color BUTTON_HOVER_BOTTOM = new Color(0xE7F2F8);

    private static final Color GLOSS = new Color(255, 255, 255, 120);
    private static final Color GLOSS_FADE = new Color(255, 255, 255, 0);

    private static final String UI_FAMILY = uiFamily();
    private static final String PIECE_FAMILY = pieceFamily();

    private SoftClubPalette() {
    }

    /**
     * A small geometric sans, widely tracked. Helvetica or Verdana when the
     * machine has one; {@link Font#SANS_SERIF} otherwise.
     */
    public static Font ui(float size) {
        Font font = new Font(UI_FAMILY, Font.PLAIN, 1).deriveFont(size);
        return font.deriveFont(Map.of(TextAttribute.TRACKING, 0.12f));
    }

    /** A face that can draw the chess glyphs in {@link PieceGlyphs#FIGURES}. */
    public static Font pieces(float size) {
        return new Font(PIECE_FAMILY, Font.PLAIN, 1).deriveFont(size);
    }

    /**
     * The glossy band across the top half of a rounded surface: white fading
     * to nothing, the way a moulded plastic button catches light.
     */
    static void gloss(Graphics2D g2, Shape shape) {
        Graphics2D copy = (Graphics2D) g2.create();
        copy.clip(shape);
        Rectangle bounds = shape.getBounds();
        float mid = bounds.y + bounds.height * 0.52f;
        copy.setPaint(new GradientPaint(bounds.x, bounds.y, GLOSS, bounds.x, mid, GLOSS_FADE));
        copy.fillRect(bounds.x, bounds.y, bounds.width, (int) Math.ceil(bounds.height * 0.52));
        copy.dispose();
    }

    private static String uiFamily() {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        for (String candidate : new String[] { "Helvetica Neue", "Helvetica", "Verdana" }) {
            for (String name : names) {
                if (name.equals(candidate)) {
                    return candidate;
                }
            }
        }
        return Font.SANS_SERIF;
    }

    private static String pieceFamily() {
        String[] candidates = {
            "Apple Symbols",
            "Segoe UI Symbol",
            "DejaVu Sans",
            "Noto Sans Symbols 2",
            "Noto Sans Symbols",
            "Menlo",
            Font.SANS_SERIF
        };
        for (String family : candidates) {
            Font font = new Font(family, Font.PLAIN, 48);
            if (font.canDisplay('\u2654') && font.canDisplay('\u265F')) {
                return family;
            }
        }
        return Font.SANS_SERIF;
    }
}
