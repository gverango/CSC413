package edu.sfsu.csc413.chess.model;

public enum PieceType {
    PAWN('P'), KNIGHT('N'), BISHOP('B'), ROOK('R'), QUEEN('Q'), KING('K');

    private final char symbol;

    //private enum constructor
    PieceType(char symbol) {
        this.symbol = symbol;
    }
    /** This piece's letter: uppercase for white, lowercase for black. */
    public char symbol() {
        return symbol;
    }
    /** The inverse: the type for a letter, in either case. Throws if it names no piece. */
    public static PieceType fromSymbol(char letter) {
        return switch (Character.toUpperCase(letter)) {
            case 'K' -> KING;
            case 'Q' -> QUEEN;
            case 'R' -> ROOK;
            case 'B' -> BISHOP;
            case 'N' -> KNIGHT;
            case 'P' -> PAWN;
            default -> throw new IllegalArgumentException("Not a piece letter: " + letter);
        };
    }
    @Override
    public String toString() {
        return String.valueOf(symbol());
    }
}
