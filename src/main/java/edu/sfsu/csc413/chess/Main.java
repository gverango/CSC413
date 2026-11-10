package edu.sfsu.csc413.chess;
// Board, Color, Piece, PieceType, Position, PieceGlyphs, TextBoardRenderer
// BoardFactory
import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.model.*;
import edu.sfsu.csc413.chess.view.*;

public final class Main {

    public static void main(String[] args) {
        //removed `BoardFactory.standard()` since Game calls it
        Game game = new Game();
        BoardView view;
        if (args.length > 0 && args[0].equals("emoji")) {
            view = new EmojiView();
        } else {
            view = new ConsoleView();
        }
        GameController controller = new GameController(game, view);
        controller.start();
    }
    private static void checkScratchTest() {
        Game game = new Game();
        String[] notations = {"f2f3", "e7e5", "g2g4", "d8h4"};
        for (String notation : notations) {
            //findLegalMove is Optional
            Move move = game.findLegalMove(notation).orElseThrow(() -> new IllegalStateException(notation + "is not a legal move"));
            game.play(move);
        }
        System.out.println(game.board());
        System.out.println("Legal moves for " + game.sideToMove() + ": " + game.legalMoves());
    }
}
