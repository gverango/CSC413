package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * The 8x8 board: paints the position and turns two clicks into one move.
 *
 * <p>The only thing this class remembers is the selected square, the half of
 * a move the player has made so far. Which squares are worth showing comes
 * from {@link Game#legalMoves()}, and the move itself is sent through
 * {@link GameController#handle}, so the controller and the rules stay the
 * judges of what is allowed. White is always drawn at the bottom.
 */
public class SoftClubBoardPanel extends JPanel {

    private static final int FRAME = 18;
    private static final double ARC = 28;

    private final GameController controller;
    private final Runnable onClick;

    private Game game;
    private Position selected;
    private List<Move> movesFromSelected = List.of();

    /**
     * @param controller receives every move the player makes
     * @param onClick    run at the start of each click, so the window can
     *                   clear an old message
     */
    public SoftClubBoardPanel(GameController controller, Runnable onClick) {
        this.controller = controller;
        this.onClick = onClick;
        setOpaque(false);
        setPreferredSize(new Dimension(580, 580));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                clicked(event.getX(), event.getY());
            }
        });
    }

    /** Shows a new position. Any half-finished move is dropped. */
    public void show(Game game) {
        this.game = game;
        selected = null;
        movesFromSelected = List.of();
        repaint();
    }

    private void clicked(int x, int y) {
        onClick.run();
        if (game == null || game.isGameOver()) {
            return;
        }
        Position square = squareAt(x, y);
        if (square == null) {
            return;
        }
        Piece piece = game.board().pieceAt(square);
        boolean ownPiece = piece != null && piece.color() == game.sideToMove();

        if (selected == null || ownPiece) {
            select(ownPiece ? square : null);
        } else {
            moveTo(square);
        }
        repaint();
    }

    private void select(Position square) {
        selected = null;
        movesFromSelected = List.of();
        if (square == null) {
            return;
        }
        List<Move> moves = new ArrayList<>();
        for (Move move : game.legalMoves()) {
            if (move.from().equals(square)) {
                moves.add(move);
            }
        }
        if (!moves.isEmpty()) {
            selected = square;
            movesFromSelected = moves;
        }
    }

    private void moveTo(Position target) {
        String notation = selected.toString() + target;
        boolean promotion = false;
        for (Move move : movesFromSelected) {
            promotion |= move.to().equals(target) && move.isPromotion();
        }
        if (promotion) {
            PieceType choice = askPromotion();
            if (choice == null) {
                return;
            }
            notation += choice.symbol();
        }
        selected = null;
        movesFromSelected = List.of();
        controller.handle(new PlayerAction.Move(notation));
    }

    /**
     * Asks what a pawn becomes. Safe as a modal dialog because it is opened on
     * the event thread, from a click, and only for a real promotion.
     */
    private PieceType askPromotion() {
        PieceType[] types = {PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT};
        String[] names = {"queen", "rook", "bishop", "knight"};
        int answer = JOptionPane.showOptionDialog(this, "promote to", "promotion",
                JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, names, names[0]);
        return answer < 0 ? null : types[answer];
    }

    // --- geometry ---------------------------------------------------------

    private double cell() {
        return (Math.min(getWidth(), getHeight()) - 2.0 * FRAME) / Position.BOARD_SIZE;
    }

    private double originX() {
        return (getWidth() - cell() * Position.BOARD_SIZE) / 2;
    }

    private double originY() {
        return (getHeight() - cell() * Position.BOARD_SIZE) / 2;
    }

    private Position squareAt(int x, int y) {
        int file = (int) Math.floor((x - originX()) / cell());
        int row = (int) Math.floor((y - originY()) / cell());
        int rank = Position.BOARD_SIZE - 1 - row;
        return Position.isOnBoard(file, rank) ? new Position(file, rank) : null;
    }

    // --- painting ---------------------------------------------------------

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        SoftClubTheme.smooth(g);
        double cell = cell();
        double ox = originX();
        double oy = originY();
        double size = cell * Position.BOARD_SIZE;

        SoftClubTheme.paintFrosted(g, ox - FRAME, oy - FRAME, size + 2 * FRAME, size + 2 * FRAME, ARC);

        Shape oldClip = g.getClip();
        g.clip(new RoundRectangle2D.Double(ox, oy, size, size, 12, 12));
        Position checked = checkedKing();
        for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
            for (int file = 0; file < Position.BOARD_SIZE; file++) {
                paintSquare(g, new Position(file, rank), ox, oy, cell, checked);
            }
        }
        g.setClip(oldClip);

        g.setPaint(SoftClubTheme.FROST_EDGE);
        g.draw(new RoundRectangle2D.Double(ox, oy, size - 1, size - 1, 12, 12));
        g.dispose();
    }

    private void paintSquare(Graphics2D g, Position at, double ox, double oy, double cell, Position checked) {
        double x = ox + at.file() * cell;
        double y = oy + (Position.BOARD_SIZE - 1 - at.rank()) * cell;
        boolean light = (at.file() + at.rank()) % 2 == 1;

        g.setColor(light ? SoftClubTheme.ICE : SoftClubTheme.POWDER);
        g.fill(new Rectangle2D.Double(x, y, cell, cell));
        paintCoordinates(g, at, x, y, cell, light);

        if (at.equals(checked)) {
            glow(g, x, y, cell, SoftClubTheme.CHECK_GLOW);
        }
        if (at.equals(selected)) {
            glow(g, x, y, cell, SoftClubTheme.AQUA_GLOW);
        }
        for (Move move : movesFromSelected) {
            if (move.to().equals(at)) {
                hint(g, x, y, cell, move.isCapture());
            }
        }
        Piece piece = game == null ? null : game.board().pieceAt(at);
        if (piece != null) {
            paintPiece(g, piece, x, y, cell);
        }
    }

    private void paintCoordinates(Graphics2D g, Position at, double x, double y, double cell, boolean light) {
        g.setFont(SoftClubTheme.label((float) (cell * 0.16)));
        g.setColor(light ? SoftClubTheme.POWDER : SoftClubTheme.ICE);
        FontMetrics fm = g.getFontMetrics();
        if (at.rank() == 0) {
            g.drawString(String.valueOf((char) ('a' + at.file())),
                    (float) (x + cell - fm.stringWidth("a") - 5), (float) (y + cell - 5));
        }
        if (at.file() == 0) {
            g.drawString(String.valueOf(at.rank() + 1), (float) (x + 5), (float) (y + fm.getAscent() + 3));
        }
    }

    /** A soft aqua haze with a brighter core, in place of a hard outline. */
    private void glow(Graphics2D g, double x, double y, double cell, Color colour) {
        g.setColor(colour);
        g.fill(new RoundRectangle2D.Double(x + 3, y + 3, cell - 6, cell - 6, 16, 16));
        g.setComposite(AlphaComposite.SrcOver.derive(0.45f));
        g.setColor(colour);
        g.fill(new RoundRectangle2D.Double(x + 9, y + 9, cell - 18, cell - 18, 12, 12));
        g.setComposite(AlphaComposite.SrcOver);
    }

    /** A small lime dot for a quiet move, a larger translucent disc for a capture. */
    private void hint(Graphics2D g, double x, double y, double cell, boolean capture) {
        double d = capture ? cell * 0.78 : cell * 0.28;
        g.setColor(capture ? SoftClubTheme.AQUA_GLOW : SoftClubTheme.LIME_HINT);
        g.fill(new Ellipse2D.Double(x + (cell - d) / 2, y + (cell - d) / 2, d, d));
    }

    /**
     * The glyph from {@link PieceGlyphs#FIGURES}, drawn as an outline so it can
     * be filled: soft slate for black, frosted white edged in slate for white,
     * each with a faint light shadow underneath.
     */
    private void paintPiece(Graphics2D g, Piece piece, double x, double y, double cell) {
        Font font = SoftClubTheme.piece((float) (cell * 0.78));
        GlyphVector glyph = font.createGlyphVector(g.getFontRenderContext(),
                PieceGlyphs.FIGURES.glyphFor(piece));
        Rectangle2D box = glyph.getVisualBounds();
        double dx = x + (cell - box.getWidth()) / 2 - box.getX();
        double dy = y + (cell - box.getHeight()) / 2 - box.getY();
        Shape shape = glyph.getOutline((float) dx, (float) dy);

        g.setColor(SoftClubTheme.SHADOW);
        g.fill(AffineTransform.getTranslateInstance(0, 2).createTransformedShape(shape));
        boolean white = piece.color() == edu.sfsu.csc413.chess.model.Color.WHITE;
        g.setColor(white ? SoftClubTheme.ICE : SoftClubTheme.SLATE);
        g.fill(shape);
        g.setColor(SoftClubTheme.SLATE);
        g.setStroke(new BasicStroke(white ? 1.6f : 0.8f));
        g.draw(shape);
    }

    private Position checkedKing() {
        if (game != null && game.status() == GameStatus.CHECK) {
            return game.board().kingPosition(game.sideToMove());
        }
        return null;
    }
}
