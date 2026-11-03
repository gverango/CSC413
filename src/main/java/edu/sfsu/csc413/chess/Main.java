package edu.sfsu.csc413.chess;
// Board, Color, Piece, PieceType, Position, PieceGlyphs, TextBoardRenderer
// BoardFactory
import edu.sfsu.csc413.chess.engine.Game;
import edu.sfsu.csc413.chess.factory.BoardFactory;
import edu.sfsu.csc413.chess.model.*;
import edu.sfsu.csc413.chess.view.PieceGlyphs;
import edu.sfsu.csc413.chess.view.TextBoardRenderer;
public final class Main {
    public static void main(String[] args) {
        BoardFactory.standard();
        checkScratchTest();
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
