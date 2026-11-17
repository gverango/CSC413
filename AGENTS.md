# Instructions for coding agents

This is a CSC 413 chess engine (Java 25, Maven, JUnit 5). In M7 an agent writes
**views only**. The model, the rules, and the controller are written by hand.

## Layout

- `model/`, `engine/`, `factory/` — board, pieces, moves, rules. **Do not change.**
- `view/` — `BoardView`, `PlayerAction`, `GameController`, `ConsoleView`,
  `EmojiView`, `TextBoardRenderer`, `PieceGlyphs`, plus any new views.
- `Main.java` — picks a view from `args[0]` (`emoji`, `swing`, ...; default is
  the console). A new view is one new `else if` case here.

## Files you may not change

`model/**`, `engine/**`, `factory/**`, `GameController`, `BoardView`,
`PlayerAction`, `ConsoleView`, `EmojiView`, `pom.xml`, and any existing test.
If a change to one of these looks necessary, **stop and explain why** instead
of making it.

## Rules every view follows

- A view never changes the game. It does not call `game.play(...)` or
  `game.undoLastMove()`. Every change goes through
  `controller.handle(PlayerAction)`.
- A view never works out a rule. Legal moves come from `game.legalMoves()`;
  check, checkmate, and stalemate come from `game.status()`.
- A Swing view's `start` builds the window on the event thread and returns. It
  never calls `controller.runLoop()`, and nothing in a click handler loops or
  blocks waiting for input.
- **Modal dialogs.** A modal `JDialog` is allowed only when it is opened on the
  event thread, from a click. Never open one from `promotionChoice()`: the
  controller calls that for *every* four-letter move it cannot match, illegal
  ones included. Detect a real promotion in the view (a legal move from/to that
  `isPromotion()`), ask there, and send the five-letter notation (`e7e8Q`).
- Only the JDK. No new dependencies, frameworks, or look-and-feel libraries.

## Style

Match the existing code: 4-space indent, `final` on fields that do not change,
Javadoc that says *why*, one public class per file, packages under
`edu.sfsu.csc413.chess`. Keep a view to a few small classes.

## Before you finish

```bash
./mvnw test                                   # 83 or more green, none changed
git diff --stat submit-m6                     # only view/, Main.java, docs
grep -n "game.play\|undoLastMove\|runLoop" <new view files>   # no matches
```

Say what you could not check, such as a window you could not open.
