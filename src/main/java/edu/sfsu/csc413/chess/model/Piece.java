package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
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
        // Loop over all possible moves
        List<Move> moves = pseudoLegalMoves(board,from);
        for (Move move: moves){
            // if "to" is "equal" to target positions e.g. @code c2d3, d3===d3
            if (move.to().equals(target)){
                return true;
            }
        }
        return false;
    }
    /** Steps to each {file, rank} offset that is on the board and not a friend. */
    protected List<Move> steppingMoves(Board board, Position from, int[][] offsets) {
        List<Move> result = new ArrayList<>();
        for (int[] d : offsets) {
            int file = from.file() + d[0];
            int rank = from.rank() + d[1];
            if ( !Position.isOnBoard(file,rank)) continue;

            Position to = new Position(file, rank);
            Piece target = board.pieceAt(to);
            if (target == null) {
                result.add(Move.quiet(from, to, this));
            } else if (target.color() != color) {
                result.add(Move.capture(from, to, this, target));
            }
        }
        return result;
    }
    /** Slides outward from {@code from} along each {file, rank} direction until blocked. */
    protected List<Move> slidingMoves(Board board, Position from, int[][] directions) {
        List<Move> result = new ArrayList<>();
        for (int[] d : directions) {
            int file = from.file() + d[0];
            int rank = from.rank() + d[1];
            while (Position.isOnBoard(file, rank)) {
                Position to = new Position(file, rank);
                // if empty, keep sliding
                if (board.isEmpty(to)) {
                    result.add(Move.quiet(from, to, this));
                } else {
                    Piece target = board.pieceAt(to);
                    // if enemy
                    if (target.color() != color()) {
                        result.add(Move.capture(from, to, this, target));
                    }
                    break;
                }
                file += d[0];
                rank += d[1];
            }
        }
        return result;
    }


    @Override
    public String toString() {
        return String.valueOf(symbol());
    }

}