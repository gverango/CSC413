# Design

## Class diagram: engine/ and model/

```mermaid
classDiagram
    class Piece {
        color()
        type()
        symbol()
        pseudoLegalMoves()
        attacks()
    }
    class Pawn
    class Knight
    class Bishop
    class Rook
    class Queen
    class King

    class Board {
        kingPosition()
        pieceAt()
        isEmpty()
        place()
        positionsOf()
        apply()
        undo()
    }

    class Move {
        from()
        to()
        moved()
        captured()
        promotesTo()
        isCapture()
        isPromotion()
    }

    class Position {
        file()
        rank()
    }

    class Color {
        opposite()
    }

    class PieceType {
        symbol()
    }

    class Game {
        board()
        sideToMove()
        history()
        legalMoves()
        findLegalMove()
        play()
        undoLastMove()
    }

    class MoveGenerator {
        isInCheck()
        isAttacked()
        legalMoves()
    }

    Piece <|-- Pawn
    Piece <|-- Knight
    Piece <|-- Bishop
    Piece <|-- Rook
    Piece <|-- Queen
    Piece <|-- King

    Game <-- Board : owns
    Game <-- Move : history
    Game --> Color : sideToMove
    Game ..> MoveGenerator : uses

    MoveGenerator ..> Board : uses
    MoveGenerator ..> Move : returns

    Move --> Position : from, to
    Move --> Piece : moved, captured
    Move --> PieceType : promotesTo

    Board --> Piece : holds

```

## Sequence diagram: game.legalMoves()

```mermaid
sequenceDiagram
    participant Caller
    participant Game
    participant MoveGenerator
    participant Board
    participant Piece

    Caller->>Game: legalMoves()
    Game->>MoveGenerator: legalMoves(board, color)

    MoveGenerator->>Board: positionsOf(color)
    MoveGenerator->>Piece: pseudoLegalMoves(board, from)

    loop each candidate move
        MoveGenerator->>Board: apply(move)
        MoveGenerator->>Board: kingPosition(color)
        MoveGenerator->>Piece: attacks(board, from, king)
        MoveGenerator->>Board: undo(move)
        alt king is not attacked
            MoveGenerator->>MoveGenerator: keep the move
        else king is attacked
            MoveGenerator->>MoveGenerator: drop the move
        end
    end

    MoveGenerator-->>Game: legal moves
    Game-->>Caller: legal moves
```