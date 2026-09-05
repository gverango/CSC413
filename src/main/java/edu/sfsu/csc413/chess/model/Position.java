package edu.sfsu.csc413.chess.model;

//record header
public record Position(int file, int rank) {

    //compact constructor
    public Position {
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException(
                    "Position off board: file=" + file + ",rank=" + rank
            );
        }
    }

    @Override
    public String toString() {
        return "" + (char) ('a' + file) + (char) ('1' + rank); // int added to char ascii val to represent file
    }

    // files and rank run 0-7
    public static final int BOARD_SIZE = 8;

    // true if raw coordinate is a real square
    public static boolean isOnBoard(int file, int rank) {
        return file >= 0 && file < BOARD_SIZE && rank >= 0 && rank < BOARD_SIZE;
    }

    public static Position parse(String algebraic) {
        if (algebraic == null){
            throw new IllegalArgumentException(
                    "Not a valid position: Position can not be null"
            );
        }
        // check length
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException(
                    "Not a valid position: " + algebraic + " is not exactly 2 in length."
            );
        }
        // case sensitive
        int file;
        int rank;
        //check algebraic[0] is a letter
        if (!Character.isLetter(algebraic.charAt(0))) {
            throw new IllegalArgumentException(
                    "Not a valid position: " + algebraic.charAt(0) + " is not a letter."
            );
        }
        //check algebraic[1] is a digit
        if (!Character.isDigit(algebraic.charAt(1))) {
            throw new IllegalArgumentException(
                    "Not a valid position: " + algebraic.charAt(1) + " is not a digit."
            );
        }
        file = (int) algebraic.charAt(0) - 'a';
        rank = (int) algebraic.charAt(1) - '1';
        // Construct object (if not valid file and rank, it will throw error)
        return new Position(file, rank);
    }

    public Position offsetOrNull(int fileDelta, int rankDelta) {
        if (isOnBoard(fileDelta,rankDelta)) {
            return new Position(fileDelta, rankDelta);
        }
        return null;
    }
}