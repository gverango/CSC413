package edu.sfsu.csc413.chess.model;
//Refactor Piece: make it abstract; make the constructor protected;
// add the abstract pseudoLegalMoves; add the attacks default;
// add slidingMoves and steppingMoves.
// Sections 2, 4, and 5. (Monday Sep 21 we do this live;
// start on your own if you can.) The build now fails in Main instead:
// "Piece is abstract; cannot be instantiated.

import java.util.List;

public abstract class Piece {
    private final Color color;
    private final PieceType type;

    protected Piece(Color color, PieceType type) {     // was public
        this.color = color;
        this.type = type;
    }

    public Color color() {
        return color;
    }

    public PieceType type() {
        return type;
    }
    /** This piece's letter: uppercase for white, lowercase for black. */
    public char symbol() {
        char letter = type.symbol();
        return color == Color.WHITE ? letter : Character.toLowerCase(letter);
    }
    /**
     * Every move this piece could make from {@code from}, ignoring whether
     * it would leave its own king in check.
     */
    public abstract List<Move> pseudoLegalMoves(Board board, Position from);
//    public boolean attacks(Board board, Position from, Position target)   // default: "can I move there?"
//
//    protected List<Move> slidingMoves(Board board, Position from, int[][] directions)
//    protected List<Move> steppingMoves(Board board, Position from, int[][] offsets)
//


}

// Old Piece design
//public class Piece {
//    private final Color color;
//    private final PieceType type;
//
//    public Piece(Color color, PieceType type) {
//        this.color = color;
//        this.type = type;
//    }
//    public Color color() {
//        return color;
//    }
//    public PieceType type() {
//        return type;
//    }
//
//    /** This piece's letter: uppercase for white, lowercase for black. */
//    public char symbol() {
//        char letter = type.symbol();
//        return color == Color.WHITE ? letter : Character.toLowerCase(letter);
//    }
//    @Override
//    public String toString() {
//        return String.valueOf(symbol());
//    }
//}
