package edu.sfsu.csc413.chess.model;
import java.util.ArrayList;
import java.util.List;

public class Board {
    // Indexed [file][rank], same order as position 0-7.
    private final Piece[][] squares = new Piece[Position.BOARD_SIZE][Position.BOARD_SIZE];

    // An empty board
    public Board() {

    }

    // Checks a position on board and returns Piece or null
    public Piece pieceAt(Position position){
        return squares[position.file()][position.rank()];
    }

    // Checks a position on board and returns if empty
    public boolean isEmpty(Position position){
        return squares[position.file()][position.rank()] == null;
    }

    // Puts piece on position of board, replacing whatever was there. (Passes null or any other Piece object)
    public void place(Position position, Piece piece){
        squares[position.file()][position.rank()] = piece;
    }

    // Returns a list of Positions where color is on the board
    public List<Position> positionsOf(Color color) {
        List<Position> positions = new ArrayList<>();
        for (int file = 0; file < Position.BOARD_SIZE; file++) {
            for (int rank = 0; rank < Position.BOARD_SIZE; rank++) {
                Piece piece = squares[file][rank];
                if (piece != null && piece.color() == color) {
                    positions.add(new Position(file, rank));
                }
            }
        }
        return positions;
    }

    // FEN placement field eg. rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR
    @Override
    public String toString() {
        StringBuilder text = new StringBuilder();
        for (int rank = Position.BOARD_SIZE - 1; rank >= 0; rank--) {  // rank 8 first
            int emptyRun = 0;
            for (int file=0; file< Position.BOARD_SIZE; file++){
                Piece piece = squares[file][rank];
                if (piece != null ) {
                    if (emptyRun > 0) {
                        text.append(emptyRun);
                        emptyRun = 0;
                    }
                    text.append(piece.symbol());
                } else {
                    emptyRun++;
                }
            }
            if (emptyRun>0){
                text.append(emptyRun);
            }
            if (rank > 0) {
                text.append('/');
            }
        }
        return text.toString();
    }
}
