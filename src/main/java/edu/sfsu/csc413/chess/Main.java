package edu.sfsu.csc413.chess;
// Board, Color, Piece, PieceType, Position, PieceGlyphs, TextBoardRenderer
import edu.sfsu.csc413.chess.model.Board;
import edu.sfsu.csc413.chess.model.Color;
import edu.sfsu.csc413.chess.model.Piece;
import edu.sfsu.csc413.chess.model.PieceType;
import edu.sfsu.csc413.chess.model.Position;
import edu.sfsu.csc413.chess.view.PieceGlyphs;
import edu.sfsu.csc413.chess.view.TextBoardRenderer;
/**
 * Entry point.
 *
 * <p>At M0 this does nothing but prove the toolchain works. It grows into the
 * real launcher as the engine appears underneath it.
 */
public final class Main {
    private static final PieceType[] STARTING_RANK_POSITIONS = {
            PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
            PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
    };
    /* Starting position */
    // r n b q k b n r
    // p p p p p p p p
    // . . . . . . . .
    // . . . . . . . .
    // . . . . . . . .
    // . . . . . . . .
    // P P P P P P P P
    // R N B Q K B N R
    public static void main(String[] args) {
        Board board = new Board();
        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            board.place(new Position(file, 0), new Piece(Color.WHITE, STARTING_RANK_POSITIONS[file]));
            board.place(new Position(file, 1), new Piece(Color.WHITE, PieceType.PAWN));
            board.place(new Position(file, 6), new Piece(Color.BLACK, PieceType.PAWN));
            board.place(new Position(file, 7), new Piece(Color.BLACK, STARTING_RANK_POSITIONS[file]));
        }
        System.out.println(new TextBoardRenderer(PieceGlyphs.LETTERS).render(board));
    }
//    public static void main(String[] args) {
//        System.out.println("CSC 413 Chess — environment OK.");
//    }

//    private Main() {
//    }
}
