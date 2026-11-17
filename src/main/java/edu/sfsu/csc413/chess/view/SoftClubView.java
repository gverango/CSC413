package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Color;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * The game in a Swing window with a late-'90s "soft club" look: pale sky,
 * frosted panels, glossy buttons.
 *
 * <p>Like every view it decides nothing about the rules. Whose turn it is,
 * check, checkmate and stalemate come from {@link Game}; every move and every
 * undo goes out through {@link GameController#handle}.
 *
 * <p>The window is event-driven, so {@link #start} builds it and returns; it
 * never calls {@link GameController#runLoop()}. The controller may call
 * {@link #render} and {@link #showMessage} from {@code main} before the window
 * exists, so both hand their work to the event thread, which runs it in order
 * once {@code start} has queued the window ahead of it.
 *
 * <p>{@link #close()} is left alone on purpose: the controller calls it when a
 * game ends, and the player should still see the final position and be able
 * to undo. Closing the window ends the program.
 */
public class SoftClubView implements BoardView {

    private static final int SIDE_WIDTH = 210;

    private JFrame frame;
    private SoftClubBoardPanel board;
    private JLabel turnLabel;
    private JLabel stateLabel;
    private JLabel countLabel;
    private JLabel messageLabel;

    private Game game;
    private String message = "";

    @Override
    public void render(Game game) {
        SwingUtilities.invokeLater(() -> {
            this.game = game;
            if (board != null) {
                board.show(game);
            }
            refresh();
        });
    }

    @Override
    public void showMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            this.message = message;
            refresh();
        });
    }

    @Override
    public void start(GameController controller) {
        SwingUtilities.invokeLater(() -> {
            build(controller);
            if (game != null) {
                board.show(game);
            }
            refresh();
        });
    }

    private void build(GameController controller) {
        board = new SoftClubBoardPanel(controller, () -> showMessage(""));

        turnLabel = infoLabel(15f);
        stateLabel = infoLabel(12f);
        countLabel = infoLabel(11f);
        messageLabel = infoLabel(11f);

        JPanel side = new JPanel() {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                SoftClubTheme.smooth(g);
                SoftClubTheme.paintFrosted(g, 0, 0, getWidth(), getHeight(), 28);
                g.dispose();
            }
        };
        side.setOpaque(false);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(26, 22, 22, 22));
        side.add(infoLabel(10f, "chess · soft club"));
        side.add(Box.createVerticalStrut(18));
        side.add(turnLabel);
        side.add(Box.createVerticalStrut(6));
        side.add(stateLabel);
        side.add(Box.createVerticalStrut(6));
        side.add(countLabel);
        side.add(Box.createVerticalStrut(22));
        side.add(messageLabel);
        side.add(Box.createVerticalGlue());

        SoftClubButton undo = new SoftClubButton("undo");
        undo.addActionListener(event -> {
            showMessage("");
            controller.handle(new PlayerAction.Undo());
        });
        side.add(undo);
        side.setPreferredSize(new Dimension(SIDE_WIDTH, 580));

        JPanel root = new JPanel(new BorderLayout(20, 0)) {
            @Override
            protected void paintComponent(Graphics graphics) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setPaint(new GradientPaint(0, 0, SoftClubTheme.SKY_TOP,
                        0, getHeight(), SoftClubTheme.SKY_BOTTOM));
                g.fillRect(0, 0, getWidth(), getHeight());
                g.dispose();
            }
        };
        root.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));
        root.add(board, BorderLayout.CENTER);
        root.add(side, BorderLayout.EAST);

        frame = new JFrame("chess · soft club");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        frame.pack();
        frame.setMinimumSize(new Dimension(640, 480));
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private void refresh() {
        if (frame == null || game == null) {
            return;
        }
        GameStatus status = game.status();
        String mover = game.sideToMove() == Color.WHITE ? "white" : "black";
        switch (status) {
            case CHECKMATE -> {
                turnLabel.setText(text("checkmate"));
                stateLabel.setText(text((game.sideToMove() == Color.WHITE ? "black" : "white") + " wins"));
            }
            case STALEMATE -> {
                turnLabel.setText(text("stalemate"));
                stateLabel.setText(text("a draw"));
            }
            case CHECK -> {
                turnLabel.setText(text(mover + " to move"));
                stateLabel.setText(text("check"));
            }
            default -> {
                turnLabel.setText(text(mover + " to move"));
                stateLabel.setText(text("calm"));
            }
        }
        countLabel.setText(text("moves played · " + game.history().size()));
        messageLabel.setText(message.isEmpty() ? " " : html(message));
    }

    private static JLabel infoLabel(float size) {
        return infoLabel(size, " ");
    }

    private static JLabel infoLabel(float size, String text) {
        JLabel label = new JLabel(text);
        label.setFont(SoftClubTheme.label(size));
        label.setForeground(SoftClubTheme.SLATE);
        label.setAlignmentX(0f);
        return label;
    }

    private static String text(String words) {
        return words.toLowerCase();
    }

    /** Lets a long message wrap inside the side panel; escapes what the controller wrote. */
    private static String html(String words) {
        String safe = words.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
        return "<html><body style='width:" + (SIDE_WIDTH - 60) + "px'>" + safe + "</body></html>";
    }
}
