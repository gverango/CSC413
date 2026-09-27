package edu.sfsu.csc413.chess.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The pawn — the piece that breaks every rule the others follow.
 *
 * <p>It is the only piece that moves in just one direction, the only one whose
 * capture differs from its move, the only one with a special first move, and
 * the only one that turns into something else. It is worth noticing that all of
 * that awkwardness is contained in this one file. No other class in the engine
 * knows that pawns are strange. That containment is the payoff of polymorphism:
 * the irregular case costs one class, not a special case in every method that
 * touches a piece.
 *
 * <p>En passant is not handled here. Like castling, it depends on the previous
 * move rather than on the current board, so it waits for Week 15 when
 * {@code Game} owns the move history.
 */
public class Pawn extends Piece {

    /**
     * What a pawn may become on reaching the far rank.
     */
    private static final PieceType[] PROMOTION_CHOICES = { PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT };

    public Pawn(Color color) {
        super(color, PieceType.PAWN);
    }

    @Override
    public List<Move> pseudoLegalMoves(Board board, Position from) {
        List<Move> result = new ArrayList<>();
        int file = from.file();
        int nextRank = from.rank() + color().pawnDirection();

        // Move forward one square
        if (Position.isOnBoard(file, nextRank)) {
            Position oneStep = new Position(file, nextRank);

            if (board.isEmpty(oneStep)) {
                addMove(result, from, oneStep, null, color().promotionRank());

                // Moves forward two squares (ONLY from the starting rank)
                if (from.rank() == color().pawnStartRank()) {
                    Position twoSteps = new Position(file, nextRank + color().pawnDirection());
                    if (board.isEmpty(twoSteps)) {
                        result.add(Move.quiet(from, twoSteps, this));
                    }
                }
            }
        }

        // Capture diagonally
        tryCapture(result, board, from, file - 1, nextRank, color().promotionRank()); //left
        tryCapture(result, board, from, file + 1, nextRank, color().promotionRank()); //right

        return result;
    }
    /** Adds a capture onto (file, rank) if an enemy piece is standing there. */
    private void tryCapture(List<Move> result, Board board, Position from,
                            int file, int rank, int promotionRank) {
        if (!Position.isOnBoard(file, rank)) {
            return;
        }
        Position to = new Position(file, rank);
        if (board.isEmpty(to)) {
            return;
        }
        Piece target = board.pieceAt(to);
        if (target.color() == color()) {
            return;
        }
        addMove(result, from, to, target, promotionRank);
    }

    /**
     * Adds one move to the list. If the pawn lands on the last rank,
     * it adds four moves instead: one for each piece it could become.
     * {@code captured} is null when nothing is captured.
     */
    private void addMove(List<Move> result, Position from, Position to, Piece captured, int promotionRank) {
        if (to.rank() == promotionRank) {
            for (PieceType choice : PROMOTION_CHOICES) {
                result.add(Move.promotion(from, to, this, captured, choice));
            }
        } else if (captured == null) {
            result.add(Move.quiet(from, to, this));
        } else {
            result.add(Move.capture(from, to, this, captured));
        }
    }

    /**
     * A pawn attacks the two squares diagonally ahead of it, whether or not
     * anything stands there.
     *
     * <p>This override exists because the inherited version answers "can this
     * piece move to that square", and for a pawn that is the wrong question.
     * An empty square in front of a pawn is a square the pawn can move to but
     * does <em>not</em> attack — which matters enormously for king safety: a
     * king may not be blocked from a square merely because a pawn could advance
     * onto it, but it certainly may not step onto a square a pawn guards.
     */
    @Override
    public boolean attacks(Board board, Position from, Position target) {
        boolean oneRankAhead = target.rank() == from.rank() +color().pawnDirection();
        boolean oneFileAside = Math.abs(target.file() - from.file()) == 1;
        return oneRankAhead && oneFileAside;
    }
}
