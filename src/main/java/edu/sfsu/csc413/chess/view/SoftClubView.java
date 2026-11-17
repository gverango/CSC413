package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

/**
 * The game in a window, drawn in the soft-club palette.
 *
 * <p>Same contract as {@link ConsoleView}: this class never decides whether a
 * move is legal, never applies one, and never works out check. A click becomes
 * a {@link PlayerAction} and goes to {@link GameController#handle}. Legal
 * destinations are whatever {@link Game#legalMoves()} returns for the selected
 * square. Check and mate are whatever {@link Game#status()} says.
 *
 * <p>A window cannot sit inside the controller's ask-and-handle loop.
 * {@link #start} builds the frame and returns; clicks call {@code handle}
 * later, on Swing's thread.
 */
public class SoftClubView implements BoardView {

    private final SoftClubBoard board = new SoftClubBoard();

    private GameController controller;
    private Game game;
    private Position selected;
    private String message = "";
    private boolean finished;

    private JFrame frame;
    private JLabel statusLabel;
    private JTextArea messageArea;
    private final List<JButton> buttons = new ArrayList<>();

    @Override
    public void render(Game game) {
        this.game = game;
        this.selected = null;
        redraw();
    }

    @Override
    public void showMessage(String message) {
        this.message = message == null ? "" : message;
        SwingUtilities.invokeLater(this::applyMessage);
    }

    /**
     * Shows the window and returns at once. The controller is kept so a click
     * can hand it a {@link PlayerAction}.
     */
    @Override
    public void start(GameController controller) {
        this.controller = controller;
        SwingUtilities.invokeLater(this::open);
    }

    /**
     * The controller calls this after "Goodbye." and also after checkmate or
     * stalemate. Only the first ends the session; a finished game stays on
     * screen so undo and new game still work.
     */
    @Override
    public void close() {
        if (message.startsWith("Goodbye")) {
            finished = true;
        }
        SwingUtilities.invokeLater(this::onClose);
    }

    private void open() {
        if (frame != null) {
            return;
        }
        frame = new JFrame("soft club");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                if (finished || controller == null) {
                    disposeFrame();
                } else {
                    controller.handle(new PlayerAction.Quit());
                }
            }
        });

        JPanel root = new JPanel(new BorderLayout(22, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g2 = (Graphics2D) graphics.create();
                g2.setPaint(new GradientPaint(0, 0, SoftClubPalette.MIST, 0, getHeight(), SoftClubPalette.SKY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(true);
        root.setBorder(new EmptyBorder(22, 22, 22, 22));

        board.onSquare(this::squareClicked);
        root.add(board, BorderLayout.CENTER);
        root.add(sidePanel(), BorderLayout.EAST);

        frame.setContentPane(root);
        applyMessage();
        redraw();
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel sidePanel() {
        JPanel side = new FrostedPanel();
        side.setLayout(new BorderLayout(0, 18));
        side.setPreferredSize(new Dimension(236, 480));
        side.setBorder(new EmptyBorder(22, 20, 20, 20));

        JLabel banner = new JLabel("soft club");
        banner.setFont(SoftClubPalette.ui(12f));
        banner.setForeground(SoftClubPalette.INK_SOFT);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(SoftClubPalette.ui(16f));
        statusLabel.setForeground(SoftClubPalette.INK);

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 12));
        header.setOpaque(false);
        header.add(banner);
        header.add(statusLabel);

        messageArea = new JTextArea();
        messageArea.setFont(SoftClubPalette.ui(13f));
        messageArea.setForeground(SoftClubPalette.INK_SOFT);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setEditable(false);
        messageArea.setOpaque(false);
        messageArea.setFocusable(false);
        messageArea.setBorder(new EmptyBorder(4, 0, 0, 0));

        JPanel controls = new JPanel(new GridLayout(4, 1, 0, 10));
        controls.setOpaque(false);
        controls.add(newGameButton());
        controls.add(actionButton("undo", new PlayerAction.Undo()));
        controls.add(actionButton("moves", new PlayerAction.ShowMoves()));
        controls.add(actionButton("quit", new PlayerAction.Quit()));

        side.add(header, BorderLayout.NORTH);
        side.add(messageArea, BorderLayout.CENTER);
        side.add(controls, BorderLayout.SOUTH);
        return side;
    }

    private JButton actionButton(String label, PlayerAction action) {
        JButton button = new FrostedButton(label);
        button.addActionListener(event -> {
            if (controller != null && !finished) {
                clearMessage();
                controller.handle(action);
            }
        });
        buttons.add(button);
        return button;
    }

    /**
     * There is no "new game" action, so this takes back every move through the
     * controller, one {@link PlayerAction.Undo} at a time. The game's own undo
     * restores the opening position; the view touches nothing directly.
     */
    private JButton newGameButton() {
        JButton button = new FrostedButton("new game");
        button.addActionListener(event -> {
            if (controller == null || finished) {
                return;
            }
            int played = game.history().size();
            for (int i = 0; i < played; i++) {
                controller.handle(new PlayerAction.Undo());
            }
            clearMessage();
        });
        buttons.add(button);
        return button;
    }

    private void squareClicked(Position clicked) {
        if (finished || controller == null || game == null || game.isGameOver()) {
            return;
        }
        Piece piece = game.board().pieceAt(clicked);
        if (selected == null) {
            if (piece != null && piece.color() == game.sideToMove()) {
                selected = clicked;
                redraw();
            }
            return;
        }
        if (clicked.equals(selected)
                || (piece != null && piece.color() == game.sideToMove())) {
            selected = clicked.equals(selected) ? null : clicked;
            redraw();
            return;
        }
        String notation = selected.toString() + clicked.toString();
        if (promotes(selected, clicked)) {
            // Ask here, and only for a real promotion. The controller asks
            // promotionChoice() for every unmatched four-letter move, illegal
            // ones included, so a dialog there would open on any bad click.
            notation += askPromotion().symbol();
        }
        selected = null;
        // Drop a stale "not a legal move" before the controller reports a new one.
        clearMessage();
        controller.handle(new PlayerAction.Move(notation));
    }

    /** True when the game lists a promotion from {@code from} to {@code to}. */
    private boolean promotes(Position from, Position to) {
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from) && move.to().equals(to) && move.isPromotion()) {
                return true;
            }
        }
        return false;
    }

    private void clearMessage() {
        message = "";
        applyMessage();
    }

    private void redraw() {
        if (SwingUtilities.isEventDispatchThread()) {
            applyBoard();
        } else {
            SwingUtilities.invokeLater(this::applyBoard);
        }
    }

    private void applyBoard() {
        if (board == null || game == null || statusLabel == null) {
            return;
        }
        statusLabel.setText(statusLine(game));
        board.show(game.board(), selected, checkedKing(), hintsFor(selected));
        board.setEnabled(!finished && !game.isGameOver());
        for (JButton button : buttons) {
            // Listing moves means nothing once the game is over; undo and new game still do.
            boolean idle = button.getText().equals("moves") && game.isGameOver();
            button.setEnabled(!finished && !idle);
        }
    }

    private void applyMessage() {
        if (messageArea != null) {
            messageArea.setText(message.toLowerCase());
        }
    }

    private void onClose() {
        if (message.startsWith("Goodbye")) {
            disposeFrame();
            return;
        }
        // Checkmate and stalemate also call close(). Leave the window up so
        // the final position and the result stay readable; the title-bar
        // close is what releases it.
        applyBoard();
    }

    private void disposeFrame() {
        if (frame != null) {
            frame.dispose();
        }
    }

    private Position checkedKing() {
        if (game.status() != GameStatus.CHECK && game.status() != GameStatus.CHECKMATE) {
            return null;
        }
        return game.board().kingPosition(game.sideToMove());
    }

    /** Destinations of the selected piece, taken from the game's own legal list. */
    private List<Position> hintsFor(Position from) {
        if (from == null || game == null) {
            return List.of();
        }
        List<Position> hints = new ArrayList<>();
        for (Move move : game.legalMoves()) {
            if (move.from().equals(from)) {
                hints.add(move.to());
            }
        }
        return hints;
    }

    private static String statusLine(Game game) {
        String side = game.sideToMove().name().toLowerCase();
        return switch (game.status()) {
            case IN_PROGRESS -> side + " to move";
            case CHECK -> side + " to move    ·    check";
            case CHECKMATE -> "checkmate";
            case STALEMATE -> "stalemate";
        };
    }

    /**
     * Asks which piece a promoting pawn becomes. Closing the dialog keeps the
     * queen, which is what {@link BoardView#promotionChoice} already does.
     */
    private PieceType askPromotion() {
        PieceType[] chosen = { PieceType.QUEEN };
        JDialog dialog = new JDialog(frame, "promote", true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JPanel panel = new FrostedPanel();
        panel.setLayout(new BorderLayout(0, 16));
        panel.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel prompt = new JLabel("promote to");
        prompt.setFont(SoftClubPalette.ui(13f));
        prompt.setForeground(SoftClubPalette.INK);

        JPanel choices = new JPanel(new GridLayout(1, 4, 10, 0));
        choices.setOpaque(false);
        for (PieceType type : List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT)) {
            JButton button = new FrostedButton(type.name().toLowerCase());
            button.addActionListener(event -> {
                chosen[0] = type;
                dialog.dispose();
            });
            choices.add(button);
        }
        panel.add(prompt, BorderLayout.NORTH);
        panel.add(choices, BorderLayout.CENTER);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
        return chosen[0];
    }

    /** A rounded, translucent panel. The window gradient shows through it. */
    private static final class FrostedPanel extends JPanel {

        private FrostedPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Float(
                    1, 1, getWidth() - 3f, getHeight() - 3f, 28, 28);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
            g2.setColor(SoftClubPalette.FROST);
            g2.fill(shape);
            g2.setComposite(AlphaComposite.SrcOver);
            SoftClubPalette.gloss(g2, shape);
            g2.setColor(SoftClubPalette.LINE);
            g2.draw(shape);
            g2.dispose();
        }
    }

    /** A rounded plastic button. Painted in full so the platform look does not replace it. */
    private static final class FrostedButton extends JButton {

        private FrostedButton(String text) {
            super(text);
            setFont(SoftClubPalette.ui(13f));
            setForeground(SoftClubPalette.INK);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFocusable(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(88, 40));
        }

        /** Skip the platform button chrome; the whole control is drawn here. */
        @Override
        public void paint(Graphics graphics) {
            paintComponent(graphics);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (!isEnabled()) {
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            }
            RoundRectangle2D shape = new RoundRectangle2D.Float(
                    1, 1, getWidth() - 3f, getHeight() - 3f, 18, 18);
            boolean hot = getModel().isRollover() || getModel().isPressed();
            Rectangle2D bounds = shape.getBounds2D();
            g2.setPaint(new GradientPaint(
                    0, (float) bounds.getY(),
                    hot ? SoftClubPalette.BUTTON_HOVER_TOP : SoftClubPalette.BUTTON_TOP,
                    0, (float) bounds.getMaxY(),
                    hot ? SoftClubPalette.BUTTON_HOVER_BOTTOM : SoftClubPalette.BUTTON_BOTTOM));
            g2.fill(shape);
            if (!getModel().isPressed()) {
                SoftClubPalette.gloss(g2, shape);
            }
            g2.setColor(SoftClubPalette.LINE);
            g2.draw(shape);

            g2.setFont(getFont());
            g2.setColor(SoftClubPalette.INK);
            FontMetrics metrics = g2.getFontMetrics();
            int tx = (getWidth() - metrics.stringWidth(getText())) / 2;
            int ty = (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }
}
