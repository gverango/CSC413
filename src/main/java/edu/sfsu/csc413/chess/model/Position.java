package edu.sfsu.csc413.chess.model;

//record header
public record Position(int file, int rank) {
    // files and rank run 0-7
    public static final int BOARD_SIZE = 8;
    // true if raw coordinate is a real square
    public static boolean isOnBoard(int file, int rank) {
        return file >= 0 && file < BOARD_SIZE && rank >= 0 && rank < BOARD_SIZE;
        }
    }
    //compact constructor
    public Position {
        if (!isOnBoard(file,rank)) {
            throw new IllegalArgumentException(
                    "Position off board: file=" + file + ",rank=" + rank
            );
        }
    }
    @Override
    public String toString() {
        return "" + (char)('a'+file) + (char)('1'+rank); // int added to char ascii val to represent file
    }

    public static Position parse(String algebraic) {
        return null;
    }

    public Position offsetOrNull(int fileDelta, int rankDelta) {
        return null;
    }
}