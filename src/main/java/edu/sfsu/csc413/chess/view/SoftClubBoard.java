package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JPanel;

/**
 * The board, drawn and nothing else.
 *
 * <p>This panel does not know which moves are legal and does not apply any.
 * The view tells it which square is selected, which squares to hint, and
 * whether a king is currently in check. A click is reported as a
 * {@link Position} and the view decides what that means.
 *
 * <p>White sits at the bottom: rank 1 along the lower edge, the a-file on the
 * left. That is the same orientation {@link TextBoardRenderer} uses for
 * {@link Color#WHITE}.
 */
public class SoftClubBoard extends JPanel {

    static final int SQUARE = 72;

    private static final int SPAN = SQUARE * Position.BOARD_SIZE;
    private static final int PAD = 18;
    private static final int LABEL = 28;
    private static final float INSET = 4.5f;
    private static final float ARC = 16f;

    private Board board;
    private Position selected;
    private Position checked;
    private List<Position> hints = List.of();
    private Consumer<Position> onSquare;

    public SoftClubBoard() {
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                if (!isEnabled() || onSquare == null) {
                    return;
                }
                Position position = positionAt(event.getX(), event.getY());
                if (position != null) {
                    onSquare.accept(position);
                }
            }
        });
    }

    /** Where a click should be delivered, as a board square. */
    void onSquare(Consumer<Position> listener) {
        this.onSquare = listener;
    }

    /**
     * Replaces what is drawn. {@code hints} are destination squares of the
     * selected piece; {@code checked} is the king in check, or null.
     */
    void show(Board board, Position selected, Position checked, List<Position> hints) {
        this.board = board;
        this.selected = selected;
        this.checked = checked;
        this.hints = hints == null ? List.of() : List.copyOf(hints);
        repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(LABEL + SPAN + PAD, PAD + SPAN + LABEL);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x0 = originX();
        int y0 = originY();
        paintTray(g2, x0, y0);

        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            int rank = Position.BOARD_SIZE - 1 - row;
            for (int file = 0; file < Position.BOARD_SIZE; file++) {
                Shape square = squareShape(x0, y0, file, row);
                paintSquare(g2, square, light(file, rank));
                Position here = new Position(file, rank);
                if (here.equals(checked)) {
                    wash(g2, square, SoftClubPalette.LIME, 0.55f);
                }
                if (here.equals(selected)) {
                    wash(g2, square, SoftClubPalette.AQUA, 0.50f);
                }
                if (board != null) {
                    paintPiece(g2, square, board.pieceAt(here));
                }
                if (hints.contains(here)) {
                    paintHint(g2, square, board != null && board.pieceAt(here) != null);
                }
            }
        }

        paintLabels(g2, x0, y0);
        g2.dispose();
    }

    private void paintTray(Graphics2D g2, int x0, int y0) {
        RoundRectangle2D tray = new RoundRectangle2D.Float(
                x0 - 10, y0 - 10, SPAN + 20, SPAN + 20, 30, 30);
        g2.setColor(SoftClubPalette.FROST);
        g2.fill(tray);
        SoftClubPalette.gloss(g2, tray);
        g2.setColor(SoftClubPalette.LINE);
        g2.draw(tray);
    }

    private void paintSquare(Graphics2D g2, Shape square, boolean light) {
        Rectangle2D bounds = square.getBounds2D();
        if (light) {
            g2.setPaint(new GradientPaint(
                    (float) bounds.getX(), (float) bounds.getY(), SoftClubPalette.SQUARE_LIGHT_TOP,
                    (float) bounds.getX(), (float) bounds.getMaxY(), SoftClubPalette.SQUARE_LIGHT_BOTTOM));
        } else {
            g2.setPaint(new GradientPaint(
                    (float) bounds.getX(), (float) bounds.getY(), SoftClubPalette.SQUARE_DARK_TOP,
                    (float) bounds.getX(), (float) bounds.getMaxY(), SoftClubPalette.SQUARE_DARK_BOTTOM));
        }
        g2.fill(square);
        SoftClubPalette.gloss(g2, square);
        g2.setColor(SoftClubPalette.LINE);
        g2.draw(square);
    }

    /** A translucent colour laid over a square. Pieces are drawn after this. */
    private static void wash(Graphics2D g2, Shape square, java.awt.Color color, float alpha) {
        Composite previous = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2.setColor(color);
        g2.fill(square);
        g2.setComposite(previous);
    }

    private void paintPiece(Graphics2D g2, Shape square, Piece piece) {
        if (piece == null) {
            return;
        }
        String glyph = PieceGlyphs.FIGURES.glyphFor(piece);
        g2.setFont(SoftClubPalette.pieces(SQUARE * 0.62f));
        FontRenderContext context = g2.getFontRenderContext();
        GlyphVector vector = g2.getFont().createGlyphVector(context, glyph);
        Rectangle2D bounds = vector.getVisualBounds();
        Rectangle2D squareBounds = square.getBounds2D();
        float tx = (float) (squareBounds.getCenterX() - bounds.getCenterX());
        float ty = (float) (squareBounds.getCenterY() - bounds.getCenterY());

        g2.setColor(SoftClubPalette.PIECE_HALO);
        g2.drawString(glyph, tx + 1.2f, ty + 1.2f);
        g2.setColor(piece.color() == Color.WHITE
                ? SoftClubPalette.PIECE_LIGHT
                : SoftClubPalette.PIECE_DARK);
        g2.drawString(glyph, tx, ty);
    }

    private void paintHint(Graphics2D g2, Shape square, boolean capture) {
        Rectangle2D bounds = square.getBounds2D();
        float cx = (float) bounds.getCenterX();
        float cy = (float) bounds.getCenterY();
        if (capture) {
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(SoftClubPalette.LIME_RING);
            g2.draw(new RoundRectangle2D.Float(
                    (float) bounds.getX() + 6, (float) bounds.getY() + 6,
                    (float) bounds.getWidth() - 12, (float) bounds.getHeight() - 12,
                    ARC, ARC));
            return;
        }
        g2.setColor(SoftClubPalette.LIME_GLOW);
        g2.fillOval(Math.round(cx - 14), Math.round(cy - 14), 28, 28);
        g2.setColor(SoftClubPalette.LIME_DOT);
        g2.fillOval(Math.round(cx - 8), Math.round(cy - 8), 16, 16);
    }

    private void paintLabels(Graphics2D g2, int x0, int y0) {
        g2.setFont(SoftClubPalette.ui(12f));
        g2.setColor(SoftClubPalette.INK_SOFT);
        FontMetrics metrics = g2.getFontMetrics();
        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            String letter = String.valueOf((char) ('a' + file));
            int width = metrics.stringWidth(letter);
            int x = x0 + file * SQUARE + (SQUARE - width) / 2;
            int y = y0 + SPAN + metrics.getAscent() + 4;
            g2.drawString(letter, x, y);
        }
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            int rank = Position.BOARD_SIZE - row;
            String numeral = String.valueOf(rank);
            int width = metrics.stringWidth(numeral);
            int x = x0 - width - 10;
            int y = y0 + row * SQUARE + (SQUARE + metrics.getAscent() - metrics.getDescent()) / 2;
            g2.drawString(numeral, x, y);
        }
    }

    private Position positionAt(int x, int y) {
        int file = (x - originX()) / SQUARE;
        int row = (y - originY()) / SQUARE;
        if (file < 0 || row < 0 || file >= Position.BOARD_SIZE || row >= Position.BOARD_SIZE) {
            return null;
        }
        // The click has to land on the square itself, not in the margin past it.
        int localX = x - originX() - file * SQUARE;
        int localY = y - originY() - row * SQUARE;
        if (localX < 0 || localY < 0 || localX >= SQUARE || localY >= SQUARE) {
            return null;
        }
        return new Position(file, Position.BOARD_SIZE - 1 - row);
    }

    private Shape squareShape(int x0, int y0, int file, int row) {
        return new RoundRectangle2D.Float(
                x0 + file * SQUARE + INSET,
                y0 + row * SQUARE + INSET,
                SQUARE - INSET * 2,
                SQUARE - INSET * 2,
                ARC, ARC);
    }

    private int originX() {
        int extra = Math.max(0, getWidth() - (LABEL + SPAN + PAD));
        return LABEL + extra / 2;
    }

    private int originY() {
        int extra = Math.max(0, getHeight() - (PAD + SPAN + LABEL));
        return PAD + extra / 2;
    }

    /** a1 is dark, so an even file+rank is a dark square. */
    private static boolean light(int file, int rank) {
        return (file + rank) % 2 != 0;
    }
}
