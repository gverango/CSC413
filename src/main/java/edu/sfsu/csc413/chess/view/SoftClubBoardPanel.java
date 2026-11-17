package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The 8x8 board of the Soft Club window: draws the position and turns two
 * clicks into one {@link PlayerAction}.
 *
 * <p>The only state this class keeps is the half-finished move, the square the
 * player has picked up ({@link #selected}) and the legal moves from it. That
 * is a fact about the <em>display</em>, not about chess, so it lives here and
 * not in the model. Everything else is read from the {@link Game} each time we
 * paint, and nothing is worked out: the dots are exactly the destinations of
 * {@link Game#legalMoves()} that start on the selected square.
 *
 * <p>White is always at the bottom. Screen rows count down while ranks count
 * up, so the conversions in {@link #squareAt} and {@link #toScreen} are the
 * one place where the two are flipped.
 */
final class SoftClubBoardPanel extends JPanel {

    private static final int MARGIN = 36;
    private static final String[] PROMOTIONS = {"queen", "rook", "bishop", "knight"};
    private static final String PROMOTION_LETTERS = "QRBN";

    private final Game game;
    private final Consumer<PlayerAction> actions;

    private Position selected;
    private List<Move> hints = List.of();

    SoftClubBoardPanel(Game game, Consumer<PlayerAction> actions) {
        this.game = game;
        this.actions = actions;
        setOpaque(false);
        setPreferredSize(new Dimension(640, 640));
        setMinimumSize(new Dimension(360, 360));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                Position square = squareAt(event.getX(), event.getY());
                if (square != null) {
                    click(square);
                }
            }
        });
    }

    /** Puts the piece back down. Called whenever the game has changed under us. */
    void deselect() {
        selected = null;
        hints = List.of();
        repaint();
    }

    private void click(Position square) {
        if (game.isGameOver()) {
            return;
        }
        Piece piece = game.board().pieceAt(square);
        boolean ownPiece = piece != null && piece.color() == game.sideToMove();

        if (selected == null || ownPiece) {
            // First click, or a change of mind: pick up this piece, or nothing.
            if (ownPiece && !square.equals(selected)) {
                select(square);
            } else {
                deselect();
            }
            return;
        }

        String notation = selected.toString() + square;
        for (Move move : hints) {
            if (move.to().equals(square) && move.isPromotion()) {
                int choice = JOptionPane.showOptionDialog(this, "promote to", "promotion",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                        PROMOTIONS, PROMOTIONS[0]);
                if (choice < 0) {
                    return;
                }
                notation += PROMOTION_LETTERS.charAt(choice);
                break;
            }
        }
        deselect();
        // Even a square that is not a destination is sent: the controller owns
        // the "Not a legal move" message, so this view does not invent its own.
        actions.accept(new PlayerAction.Move(notation));
    }

    private void select(Position square) {
        selected = square;
        List<Move> moves = new ArrayList<>();
        for (Move move : game.legalMoves()) {
            if (move.from().equals(square)) {
                moves.add(move);
            }
        }
        hints = moves;
        repaint();
    }

    // ---- geometry -------------------------------------------------------

    private int cell() {
        return Math.max(8, (Math.min(getWidth(), getHeight()) - 2 * MARGIN) / Position.BOARD_SIZE);
    }

    private int originX() {
        return (getWidth() - cell() * Position.BOARD_SIZE) / 2;
    }

    private int originY() {
        return (getHeight() - cell() * Position.BOARD_SIZE) / 2;
    }

    /** The square under a mouse position, or null if the click missed the board. */
    private Position squareAt(int x, int y) {
        int cell = cell();
        int column = Math.floorDiv(x - originX(), cell);
        int row = Math.floorDiv(y - originY(), cell);
        // Row 0 is the top of the screen, which is rank 8 for white.
        int rank = Position.BOARD_SIZE - 1 - row;
        return Position.isOnBoard(column, rank) ? new Position(column, rank) : null;
    }

    /** The top-left corner of a square, in pixels. */
    private Point2D toScreen(Position square) {
        int cell = cell();
        return new Point2D.Double(originX() + square.file() * cell,
                originY() + (Position.BOARD_SIZE - 1 - square.rank()) * cell);
    }

    // ---- painting -------------------------------------------------------

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int cell = cell();
            int size = cell * Position.BOARD_SIZE;
            int ox = originX();
            int oy = originY();

            SoftClubTheme.paintGlass(g, new RoundRectangle2D.Float(
                    ox - 14, oy - 14, size + 28, size + 28, 30, 30), 150);

            RoundRectangle2D.Float board = new RoundRectangle2D.Float(ox, oy, size, size, 16, 16);
            Shape outside = g.getClip();
            g.clip(board);
            paintSquares(g, cell);
            g.setPaint(new java.awt.GradientPaint(0, oy, new java.awt.Color(255, 255, 255, 70),
                    0, oy + size / 2f, new java.awt.Color(255, 255, 255, 0)));
            g.fill(board);
            g.setClip(outside);
            g.setPaint(new java.awt.Color(255, 255, 255, 200));
            g.draw(board);

            paintCoordinates(g, cell);
            paintHints(g, cell);
            paintPieces(g, cell);
        } finally {
            g.dispose();
        }
    }

    private void paintSquares(Graphics2D g, int cell) {
        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
                Position square = new Position(file, rank);
                Point2D at = toScreen(square);
                // a1 is dark, so an even file+rank is a dark square.
                g.setColor((file + rank) % 2 == 0 ? SoftClubTheme.POWDER : SoftClubTheme.ICE);
                g.fill(new Rectangle2D.Double(at.getX(), at.getY(), cell, cell));
            }
        }
        Position checked = checkedKing();
        if (checked != null) {
            glow(g, square(checked, cell), SoftClubTheme.LIME, 190, 60);
        }
        if (selected != null) {
            glow(g, square(selected, cell), SoftClubTheme.AQUA, 200, 70);
        }
    }

    private void paintHints(Graphics2D g, int cell) {
        for (Move move : hints) {
            Rectangle2D square = square(move.to(), cell);
            if (move.isCapture()) {
                // A glow over the whole square: a hard ring would shout.
                glow(g, new RoundRectangle2D.Double(square.getX() + 3, square.getY() + 3,
                        cell - 6, cell - 6, 14, 14), SoftClubTheme.AQUA, 40, 170);
            } else {
                double radius = cell * 0.15;
                double cx = square.getCenterX();
                double cy = square.getCenterY();
                glow(g, new Ellipse2D.Double(cx - radius * 2.2, cy - radius * 2.2,
                        radius * 4.4, radius * 4.4), SoftClubTheme.LIME, 120, 0);
                glow(g, new Ellipse2D.Double(cx - radius, cy - radius, radius * 2, radius * 2),
                        SoftClubTheme.LIME, 235, 170);
            }
        }
    }

    private void paintPieces(Graphics2D g, int cell) {
        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
                Position square = new Position(file, rank);
                Piece piece = game.board().pieceAt(square);
                if (piece != null) {
                    Rectangle2D area = square(square, cell);
                    paintPiece(g, piece, area.getCenterX(), area.getCenterY(), cell);
                }
            }
        }
    }

    /**
     * One figure. Both sides use the solid glyphs so they read on either square
     * colour: black is soft slate with a faint light shadow, white is frosted
     * white drawn inside a thin slate line.
     */
    private void paintPiece(Graphics2D g, Piece piece, double cx, double cy, int cell) {
        GlyphVector glyphs = SoftClubTheme.pieces(cell * 0.78f)
                .createGlyphVector(g.getFontRenderContext(), glyph(piece));
        Rectangle2D bounds = glyphs.getVisualBounds();
        Shape figure = glyphs.getOutline((float) (cx - bounds.getCenterX()),
                (float) (cy - bounds.getCenterY()));
        Shape shadow = AffineTransform.getTranslateInstance(1.5, 2.5).createTransformedShape(figure);

        if (piece.color() == Color.BLACK) {
            g.setPaint(new java.awt.Color(255, 255, 255, 150));
            g.fill(shadow);
            g.setPaint(SoftClubTheme.SLATE);
            g.fill(figure);
        } else {
            g.setPaint(SoftClubTheme.withAlpha(SoftClubTheme.SLATE, 60));
            g.fill(shadow);
            g.setPaint(SoftClubTheme.FROST);
            g.fill(figure);
            g.setPaint(SoftClubTheme.SLATE);
            g.setStroke(new java.awt.BasicStroke(1.3f));
            g.draw(figure);
        }
    }

    private void paintCoordinates(Graphics2D g, int cell) {
        g.setFont(SoftClubTheme.label(Math.max(9f, cell * 0.17f)));
        g.setColor(SoftClubTheme.MIST);
        FontMetrics metrics = g.getFontMetrics();
        for (int i = 0; i < Position.BOARD_SIZE; i++) {
            String file = String.valueOf((char) ('a' + i));
            Rectangle2D bottom = square(new Position(i, 0), cell);
            g.drawString(file, (float) (bottom.getCenterX() - metrics.stringWidth(file) / 2.0),
                    (float) (bottom.getMaxY() + 26));
            String rank = String.valueOf(i + 1);
            Rectangle2D left = square(new Position(0, i), cell);
            g.drawString(rank, (float) (left.getX() - 24 - metrics.stringWidth(rank) / 2.0),
                    (float) (left.getCenterY() + metrics.getAscent() / 2.0 - 1));
        }
    }

    private Rectangle2D square(Position square, int cell) {
        Point2D at = toScreen(square);
        return new Rectangle2D.Double(at.getX(), at.getY(), cell, cell);
    }

    /** Fills a shape with a soft round glow: {@code centre} alpha in the middle, {@code edge} at the rim. */
    private static void glow(Graphics2D g, Shape shape, java.awt.Color tint, int centre, int edge) {
        Rectangle2D box = shape.getBounds2D();
        float radius = (float) Math.max(box.getWidth(), box.getHeight()) * 0.75f;
        g.setPaint(new RadialGradientPaint(
                new Point2D.Double(box.getCenterX(), box.getCenterY()), radius,
                new float[] {0f, 1f},
                new java.awt.Color[] {SoftClubTheme.withAlpha(tint, centre),
                        SoftClubTheme.withAlpha(tint, edge)}));
        g.fill(shape);
    }

    /** The king of the side to move if it is in check, otherwise null. */
    private Position checkedKing() {
        GameStatus status = game.status();
        if (status != GameStatus.CHECK && status != GameStatus.CHECKMATE) {
            return null;
        }
        return game.board().kingPosition(game.sideToMove());
    }

    private static String glyph(Piece piece) {
        return switch (piece.type()) {
            case KING -> "♚";
            case QUEEN -> "♛";
            case ROOK -> "♜";
            case BISHOP -> "♝";
            case KNIGHT -> "♞";
            case PAWN -> "♟";
        };
    }
}
