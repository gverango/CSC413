package edu.sfsu.csc413.chess.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.GraphicsEnvironment;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Every colour, font and glassy surface of the Soft Club window, in one place.
 *
 * <p>Nothing here is chess. Keeping the look in a single class means the board
 * and the side panel can disagree about <em>what</em> to draw but never about
 * <em>what colour</em> it is: changing the mood of the whole window is a matter
 * of editing the constants below.
 *
 * <p>The palette is deliberately low contrast: pale sky, ice, frosted white and
 * silver, with a little lime and aqua kept for the things the player needs to
 * notice (the selected square, the legal-move dots, a king in check).
 */
final class SoftClubTheme {

    static final Color SKY_TOP = new Color(0xEEF6FC);
    static final Color SKY_BOTTOM = new Color(0xC6DDEE);
    static final Color FROST = new Color(0xFAFDFF);
    static final Color ICE = new Color(0xE9F5F8);
    static final Color POWDER = new Color(0xA8C0D6);
    static final Color SILVER = new Color(0xB8C4D0);
    static final Color SLATE = new Color(0x4B5B6C);
    static final Color MIST = new Color(0x7B8DA0);
    static final Color AQUA = new Color(0x7FDCDC);
    static final Color LIME = new Color(0xB9F28F);

    private static final String TEXT_FAMILY = firstInstalled(Font.SANS_SERIF,
            "Verdana", "Helvetica", "Helvetica Neue");
    private static final String PIECE_FAMILY = firstInstalled(Font.SANS_SERIF,
            "Segoe UI Symbol", "DejaVu Sans", "Apple Symbols");

    private SoftClubTheme() {
    }

    /** The same colour with its own transparency, 0 (clear) to 255 (solid). */
    static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * A small, widely spaced sans for labels. The spacing is what makes lowercase
     * text read like a CD insert rather than a form.
     */
    static Font label(float size) {
        return new Font(TEXT_FAMILY, Font.PLAIN, 12).deriveFont(
                Map.of(TextAttribute.SIZE, size, TextAttribute.TRACKING, 0.1f));
    }

    /** A font that actually contains the chess figures, falling back to the default sans. */
    static Font pieces(float size) {
        return new Font(PIECE_FAMILY, Font.PLAIN, 12).deriveFont(size);
    }

    /**
     * Paints a pane of frosted plastic: a white-to-pale-blue gradient, a glossy
     * highlight over the top half, and a thin light border.
     *
     * @param alpha how see-through the plastic is, 0 to 255
     */
    static void paintGlass(Graphics2D g, RoundRectangle2D.Float shape, int alpha) {
        g.setPaint(new GradientPaint(0, shape.y, withAlpha(FROST, alpha),
                0, shape.y + shape.height, withAlpha(ICE, alpha * 3 / 4)));
        g.fill(shape);

        float arc = Math.max(2f, shape.arcwidth - 4f);
        RoundRectangle2D.Float gloss = new RoundRectangle2D.Float(
                shape.x + 2, shape.y + 2, shape.width - 4, shape.height / 2f - 2, arc, arc);
        g.setPaint(new GradientPaint(0, gloss.y, new Color(255, 255, 255, 140),
                0, gloss.y + gloss.height, new Color(255, 255, 255, 12)));
        g.fill(gloss);

        g.setPaint(new Color(255, 255, 255, 210));
        g.setStroke(new java.awt.BasicStroke(1f));
        g.draw(shape);
    }

    /** The first family that is installed, so a missing font degrades instead of failing. */
    private static String firstInstalled(String fallback, String... preferred) {
        List<String> installed = Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        for (String family : preferred) {
            if (installed.contains(family)) {
                return family;
            }
        }
        return fallback;
    }
}
