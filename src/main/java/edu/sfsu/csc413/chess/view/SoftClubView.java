package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

/**
 * The game in a Swing window with a late-'90s "Soft Club" look: pale sky blue,
 * frosted panels and glossy plastic buttons.
 *
 * <p>Like every view it decides nothing about the rules. Turn and status come
 * from {@link Game}, and every button and every move goes out as a
 * {@link PlayerAction} through {@link GameController#handle}. A window is told
 * to start rather than asked for input, so {@link #start} builds the window and
 * returns at once; it never takes over the controller's input loop.
 *
 * <p>The controller calls {@code render} and {@code showMessage} from the
 * {@code main} thread before the window exists, and from the event thread after
 * a click. So this class only <em>remembers</em> what it is told, and touches a
 * component only on the event thread and only once the window has been built.
 */
public class SoftClubView implements BoardView {

    private Game game;
    private String message = "";
    private GameController controller;

    private JFrame window;
    private SoftClubBoardPanel board;
    private JLabel turnLabel;
    private JLabel statusLabel;
    private JTextArea messageArea;

    @Override
    public void render(Game game) {
        this.game = game;
        onEventThread(() -> {
            if (window != null) {
                board.deselect();
                showStatus();
            }
        });
    }

    @Override
    public void showMessage(String message) {
        this.message = message.toLowerCase();
        onEventThread(() -> {
            if (window != null) {
                messageArea.setText(this.message);
                messageArea.setCaretPosition(0);
            }
        });
    }

    /** Builds the window on the event thread and returns. The window runs the game from here on. */
    @Override
    public void start(GameController controller) {
        this.controller = controller;
        EventQueue.invokeLater(this::build);
    }

    /**
     * Closes the window when the player quits. A finished game keeps its window
     * open, because the checkmate message is the one thing left to read.
     */
    @Override
    public void close() {
        onEventThread(() -> {
            if (window != null && !game.isGameOver()) {
                window.dispose();
            }
        });
    }

    private void build() {
        board = new SoftClubBoardPanel(controller.game(), this::send);
        turnLabel = new JLabel();
        turnLabel.setFont(SoftClubTheme.label(17f));
        turnLabel.setForeground(SoftClubTheme.SLATE);
        statusLabel = new JLabel();
        statusLabel.setFont(SoftClubTheme.label(12f));
        statusLabel.setForeground(SoftClubTheme.MIST);

        JLabel title = new JLabel("soft club chess");
        title.setFont(SoftClubTheme.label(11f));
        title.setForeground(SoftClubTheme.MIST);

        messageArea = new JTextArea(message);
        messageArea.setFont(SoftClubTheme.label(11f));
        messageArea.setForeground(SoftClubTheme.SLATE);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        messageArea.setOpaque(false);
        JScrollPane messages = new JScrollPane(messageArea);
        messages.setBorder(BorderFactory.createEmptyBorder());
        messages.setOpaque(false);
        messages.getViewport().setOpaque(false);

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(22));
        header.add(turnLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(statusLabel);
        header.add(Box.createVerticalStrut(18));

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 10));
        buttons.setOpaque(false);
        buttons.add(new GlossButton("undo", () -> send(new PlayerAction.Undo())));
        buttons.add(new GlossButton("legal moves", () -> send(new PlayerAction.ShowMoves())));
        buttons.add(new GlossButton("quit", () -> send(new PlayerAction.Quit())));

        FrostedPanel side = new FrostedPanel(new BorderLayout(0, 12));
        side.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        side.setPreferredSize(new Dimension(240, 0));
        side.add(header, BorderLayout.NORTH);
        side.add(messages, BorderLayout.CENTER);
        side.add(buttons, BorderLayout.SOUTH);

        SkyPanel content = new SkyPanel(new BorderLayout(20, 0));
        content.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        content.add(board, BorderLayout.CENTER);
        content.add(side, BorderLayout.EAST);

        window = new JFrame("soft club chess");
        window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        window.setContentPane(content);
        window.pack();
        window.setMinimumSize(new Dimension(680, 520));
        window.setLocationRelativeTo(null);
        showStatus();
        window.setVisible(true);
    }

    /** Every request from the window goes through here, and so through the controller. */
    private void send(PlayerAction action) {
        // A message belongs to the request that caused it, so an old one goes first.
        showMessage("");
        controller.handle(action);
    }

    private void showStatus() {
        if (game == null) {
            return;
        }
        turnLabel.setText(game.isGameOver() ? "game over"
                : game.sideToMove().toString().toLowerCase() + " to move");
        statusLabel.setText(game.status().toString().toLowerCase().replace('_', ' '));
    }

    private static void onEventThread(Runnable work) {
        if (EventQueue.isDispatchThread()) {
            work.run();
        } else {
            EventQueue.invokeLater(work);
        }
    }

    /** The pale sky behind everything, with two soft discs of aqua for atmosphere. */
    private static final class SkyPanel extends JPanel {

        SkyPanel(BorderLayout layout) {
            super(layout);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setPaint(new GradientPaint(0, 0, SoftClubTheme.SKY_TOP,
                    0, getHeight(), SoftClubTheme.SKY_BOTTOM));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(SoftClubTheme.withAlpha(SoftClubTheme.AQUA, 38));
            g.fill(new Ellipse2D.Double(-getWidth() * 0.12, getHeight() * 0.55,
                    getWidth() * 0.5, getWidth() * 0.5));
            g.setColor(SoftClubTheme.withAlpha(Color.WHITE, 90));
            g.fill(new Ellipse2D.Double(getWidth() * 0.55, -getHeight() * 0.25,
                    getWidth() * 0.6, getWidth() * 0.6));
            g.dispose();
        }
    }

    /** A translucent rounded pane, like frosted plastic. */
    private static final class FrostedPanel extends JPanel {

        FrostedPanel(BorderLayout layout) {
            super(layout);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            SoftClubTheme.paintGlass(g, new RoundRectangle2D.Float(
                    0.5f, 0.5f, getWidth() - 1, getHeight() - 1, 30, 30), 150);
            g.dispose();
        }
    }

    /** A pill-shaped glossy button that runs a request when clicked. */
    private static final class GlossButton extends JButton {

        GlossButton(String text, Runnable onClick) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(SoftClubTheme.label(12f));
            setForeground(SoftClubTheme.SLATE);
            setPreferredSize(new Dimension(0, 38));
            addActionListener(event -> onClick.run());
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int alpha = getModel().isArmed() ? 110 : getModel().isRollover() ? 250 : 190;
            SoftClubTheme.paintGlass(g, new RoundRectangle2D.Float(
                    0.5f, 0.5f, getWidth() - 1, getHeight() - 1, getHeight(), getHeight()), alpha);
            g.setFont(getFont());
            g.setColor(getForeground());
            FontMetrics metrics = g.getFontMetrics();
            g.drawString(getText(), (getWidth() - metrics.stringWidth(getText())) / 2,
                    (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2);
            g.dispose();
        }
    }
}
