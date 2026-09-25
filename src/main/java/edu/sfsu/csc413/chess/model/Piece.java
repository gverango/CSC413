package edu.sfsu.csc413.chess.model;

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

    public boolean attacks(Board board, Position from, Position target)  // default: "can I move there?"
    {
        return false;
    }
    protected List<Move> slidingMoves(Board board, Position from, int[][] directions) {
        return null;
    }
    protected List<Move> steppingMoves(Board board, Position from, int[][] offsets) {
        return null;
    }


    @Override
    public String toString() {
        return String.valueOf(symbol());
    }

}