package edu.sfsu.csc413.chess.view;

import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.engine.GameStatus;
import edu.sfsu.csc413.chess.model.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Connects the player's decisions to the game: carry out what was asked, then
 * ask the view to redraw.
 *
 * <p>This is the C in MVC. It is the only class that knows about both the
 * {@link Game} and the {@link BoardView}. It holds no game state of its own:
 * the model owns the position, the view owns the display, and the controller
 * owns only the conversation between them.
 *
 * <p>When you are done, this class mentions no {@code Scanner}, no
 * {@code System.out}, no window, and none of the words "undo", "moves", or
 * "quit". Those belong to the view and to {@link PlayerAction#parse}.
 */
public class GameController {

    private final Game game;
    private final BoardView view;
    private boolean quit = false;

    public GameController(Game game, BoardView view) {
        this.game = game;
        this.view = view;
    }

    /** The game being played, so a view can read it in order to draw it. */
    public Game game() {
        return game;
    }

    /**
     * Greets the player, draws the opening position, and hands control to the
     * view with {@link BoardView#start}.
     */
    //greet the player (showMessage),
    // draw the opening position (render), then call view.start(this).
    // What happens next is the view's decision. The console view calls runLoop() back; a window in M7 will not.
    public void start() {
        view.showMessage("Chess game loading...");
        view.render(game);
        view.start(this);
    }

    /**
     * The ask-and-handle loop, for views that can wait for input.
     *
     * <p>Asks the view for the next action and handles it, until the player
     * quits or the game is over. A console view calls this from its
     * {@code start}; an event-driven view never calls it.
     */
    //ask the view for the next action (nextAction) and handle it, until the player quits or the game is over.
    public void runLoop() {
        while (!quit && !game.isGameOver()) {
            handle(view.nextAction(game));
        }
    }

    /**
     * Carries out one player action and redraws.
     *
     * <p>Every route into the game goes through here: {@link #runLoop()} calls
     * it, and so will a button's click handler in M7. The handout lists what
     * each of the four actions does and what the player is told.
     */
    //Action	What the controller does	                        What the player is told
    //Move	    find the legal move with that notation and play it	if there is none: Not a legal move: 'e2e5'.
    //Undo	    take back the last move                         	if there was none: Nothing to undo.
    //ShowMoves	nothing changes	                                    20 legal moves: a2a3 a2a4 ... (sorted)
    //Quit	    stop; close the view; no redraw	                    Goodbye.
    public void handle(PlayerAction action) {
        switch (action) {
            case PlayerAction.Move m -> {
                String notation = m.notation();
                Optional<Move> move = game.findLegalMove(notation);
                if (move.isEmpty() && notation.length() == 4) {
                    //if promotion, ask which piece and try again
                    move = game.findLegalMove(notation + view.promotionChoice(game).symbol());
                }
                if (move.isPresent()) {
                    game.play(move.get());
                } else {
                    view.showMessage("Not a legal move: '" + notation + "'.");
                }
            }
            case PlayerAction.Undo u -> {
                if (game.undoLastMove().isEmpty()) {
                    view.showMessage("Nothing to undo.");
                }
            }
            case PlayerAction.ShowMoves s -> {
                List<String> moves = new ArrayList<>();
                for (Move move : game.legalMoves()) {
                    moves.add(move.toString());
                }
                Collections.sort(moves);
                view.showMessage(moves.size() + " legal moves: " + String.join(" ", moves));
            }
            case PlayerAction.Quit q -> {
                quit = true;
                view.showMessage("Goodbye.");
                view.close();
                return;
            }
        }

        view.render(game);
        if (game.isGameOver()) {
            if (game.status() == GameStatus.CHECKMATE) {
                view.showMessage("Checkmate. " + game.sideToMove().opposite() + " wins.");
            } else {
                view.showMessage("Stalemate. The game is a draw.");
            }
            view.close();
        }
    }

}
