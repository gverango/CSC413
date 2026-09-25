package edu.sfsu.csc413.chess;
// Board, Color, Piece, PieceType, Position, PieceGlyphs, TextBoardRenderer
// BoardFactory
import edu.sfsu.csc413.chess.factory.BoardFactory;
import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;
import edu.sfsu.csc413.chess.view.PieceGlyphs;
import edu.sfsu.csc413.chess.view.TextBoardRenderer;
public final class Main {
    public static void main(String[] args) {
        BoardFactory.standard();
    }
}
