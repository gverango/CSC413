package edu.sfsu.csc413.chess.model;

//record header
public record Position(int file, int rank) {

    public static Position parse(String algebraic) {
        return null;
    }

    public Position offsetOrNull(int fileDelta, int rankDelta) {
        return null;
    }
}