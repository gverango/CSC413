package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.Position;

import java.io.PrintStream;
import java.util.Scanner;

/**
 * The game in a terminal, in ANSI colour, with a border, drawn from the side
 * to move.
 *
 * <p>Like {@link ConsoleView} it lets the controller drive, and like every
 * view it decides nothing about the rules: check comes from
 * {@link Game#status()}, and the board is whatever {@link Game#board()} holds.
 *
 * <p>What it adds is a display decision the text views do not make: the board
 * turns around every move, so the player whose turn it is always sits at the
 * bottom. The king of the side to move is tinted when it is in check.
 */
public class AnsiView implements BoardView {

    private static final String RESET = "\u001B[0m";
    private static final String LIGHT_SQUARE = "\u001B[48;5;189m";
    private static final String DARK_SQUARE = "\u001B[48;5;110m";
    private static final String CHECK_SQUARE = "\u001B[48;5;150m";
    private static final String WHITE_PIECE = "\u001B[1;38;5;231m";
    private static final String BLACK_PIECE = "\u001B[1;38;5;236m";
    private static final String FRAME = "\u001B[38;5;103m";

    private final PrintStream out;
    private final Scanner in;

    /** A colour view reading from standard input. */
    public AnsiView() {
        this(System.out, new Scanner(System.in));
    }

    /** Injectable streams, so tests can drive the view without a terminal. */
    public AnsiView(PrintStream out, Scanner in) {
        this.out = out;
        this.in = in;
    }

    @Override
    public void render(Game game) {
        Color perspective = game.sideToMove();
        boolean flipped = perspective == Color.BLACK;
        Position checked = checkedKing(game);

        out.println();
        out.println(FRAME + "   ┌" + "─".repeat(Position.BOARD_SIZE * 3) + "┐" + RESET);
        for (int row = 0; row < Position.BOARD_SIZE; row++) {
            // Row 0 is the top line. The side to move sees its own back rank
            // at the bottom, so white sees rank 8 on top and black sees rank 1.
            int rank = flipped ? row : Position.BOARD_SIZE - 1 - row;
            StringBuilder line = new StringBuilder();
            line.append(FRAME).append(' ').append(rank + 1).append(" │").append(RESET);
            for (int column = 0; column < Position.BOARD_SIZE; column++) {
                int file = flipped ? Position.BOARD_SIZE - 1 - column : column;
                Position here = new Position(file, rank);
                line.append(square(game.board().pieceAt(here), file, rank, here.equals(checked)));
            }
            line.append(FRAME).append('│').append(RESET);
            out.println(line);
        }
        out.println(FRAME + "   └" + "─".repeat(Position.BOARD_SIZE * 3) + "┘" + RESET);

        StringBuilder files = new StringBuilder("    ");
        for (int column = 0; column < Position.BOARD_SIZE; column++) {
            int file = flipped ? Position.BOARD_SIZE - 1 - column : column;
            files.append(' ').append((char) ('a' + file)).append(' ');
        }
        out.println(FRAME + files + RESET);
        out.println();
        out.println(game.sideToMove() + " to move  |  " + game.status());
    }

    @Override
    public void showMessage(String message) {
        out.println(message);
    }

    /** A terminal can wait for typing, so the controller drives. */
    @Override
    public void start(GameController controller) {
        controller.runLoop();
    }

    @Override
    public PlayerAction nextAction(Game game) {
        out.print("Your move (e.g. e2e4, 'undo', 'moves', 'quit'): ");
        out.flush();
        // End of input means the player is done, exactly like typing "quit".
        return PlayerAction.parse(in.hasNextLine() ? in.nextLine() : "quit");
    }

    /** One three-column cell: background for the square, then the piece. */
    private static String square(Piece piece, int file, int rank, boolean inCheck) {
        // a1 is dark, so an even file+rank is a dark square.
        String background = inCheck ? CHECK_SQUARE
                : (file + rank) % 2 == 0 ? DARK_SQUARE : LIGHT_SQUARE;
        if (piece == null) {
            return background + "   " + RESET;
        }
        // Filled figures for both sides: they read on either square colour,
        // and the foreground colour is what tells white from black.
        String foreground = piece.color() == Color.WHITE ? WHITE_PIECE : BLACK_PIECE;
        return background + foreground + " " + filledGlyph(piece) + " " + RESET;
    }

    private static String filledGlyph(Piece piece) {
        return switch (piece.type()) {
            case KING -> "♚";
            case QUEEN -> "♛";
            case ROOK -> "♜";
            case BISHOP -> "♝";
            case KNIGHT -> "♞";
            case PAWN -> "♟";
        };
    }

    private static Position checkedKing(Game game) {
        GameStatus status = game.status();
        if (status != GameStatus.CHECK && status != GameStatus.CHECKMATE) {
            return null;
        }
        return game.board().kingPosition(game.sideToMove());
    }
}
