# M7 submission

> Items marked **TODO(you)** need facts only you have, or work still to do.
> Everything else describes what is in the repository now.

## 1. Group

Alone.

## 2. Tools

Claude Code (desktop app, Code tab), model Claude Opus 5.5 (`claude-opus-5-5`).
**TODO(you):** if the first draft of the Swing view came from a different tool
or model, name it here as well.

## 3. Other view

`ansi`: `java -cp target/classes edu.sfsu.csc413.chess.Main ansi`

`view/AnsiView.java`: a bordered terminal board in ANSI colour, redrawn from the
side to move every turn (`AnsiView.java:51`), with the checked king's square
tinted (`AnsiView.java:126`). It drives through `controller.runLoop()` like the
console view (`AnsiView.java:89`). It was tested by piping moves in:
an illegal move, `undo` with nothing to undo, `moves`, `quit`, and Fool's Mate to
checkmate.

## 4. Runs

| Branch | Request (word for word) | Change from previous run |
|---|---|---|
| `m7-run-1` | **TODO(you):** paste the request that produced the first draft of `SoftClubView`/`SoftClubBoard`/`SoftClubPalette`, then the follow-up request in this session (the "Gen X Soft Club" prompt). | first run |
| `m7-run-2` | **TODO(you)** | **TODO(you):** one change only, either the model or one `AGENTS.md` instruction |
| `m7-run-3` | **TODO(you)** | **TODO(you)** |

Note on order: `AGENTS.md` was committed (`6111a2a`) after run 1's code had been
generated, not before it. Run 1's code is committed on top of it in `1d2d956`.

## 5. `AGENTS.md`

Rule: **"Modal dialogs ... Never open one from `promotionChoice()`."**
Why: run 1's first draft opened the promotion dialog from `promotionChoice()`.
`GameController.java:87` calls `promotionChoice()` for *every* four-letter move
it cannot match, so any illegal click (for example e2→e5) popped up
"promote to". The rule makes sure later runs ask only for a real promotion.

## 6. Review

1. **`SoftClubView.java:237` and `:250`: promotion.** `promotes()` scans
   `game.legalMoves()` for a move from/to that `isPromotion()`. Only then does
   the view ask, and it sends five-letter notation (`b7a8N`), so the controller
   matches it on the first try. *Checked:* a scripted game sent real mouse
   presses. Black's illegal d8→d4 showed "Not a legal move: 'd8d4'." with no
   dialog. bxa8 opened the dialog; choosing knight left `N` on a8 (board FEN
   `Nn2kbnr/...`). *Decision:* **changed**, replacing the draft's
   `promotionChoice()` override. That override caused the bug in §5.
2. **`SoftClubView.java:317`: legal-move hints.** `hintsFor()` keeps the
   destinations of `game.legalMoves()` whose `from` is the selected square. It
   never computes a rule itself. *Checked:* read the code; with d8 selected after
   f3 e5 g4, the hints were exactly e7, f6, g5, h4. *Decision:* **kept**.
3. **`SoftClubBoard.java:230`: click to square.** `positionAt()` turns pixels
   into a `Position`. Screen rows count down and ranks count up, hence
   `BOARD_SIZE - 1 - row` at `:242`. *Checked:* every scripted click landed on the
   intended square (e2e4, d7d5 and so on all played). *Decision:* **kept**.
4. **`SoftClubView.java:202`: new game.** There is no new-game `PlayerAction`.
   The button sends one `Undo` per move in `game.history()` through
   `controller.handle` (`:210`), so the view never calls `undoLastMove`.
   *Checked:* after a promotion game, new game restored the starting FEN.
   *Decision:* **added** in run 1's follow-up.
5. **`SoftClubView.java:94`: `close()`.** The controller calls `close()` after
   "Goodbye." and also after checkmate or stalemate. Only Goodbye ends the
   session, so after mate the window stays up and undo and new game still work.
   *Checked:* undo after Fool's Mate returned to "black to move". *Decision:*
   **changed**: the draft disabled every button after mate.

Rule checks on `m7-run-1`: `./mvnw test` 83/83 green with no test changed;
`git diff --stat submit-m6` lists only `view/`, `Main.java`, `AGENTS.md`,
`CLAUDE.md`, `docs/milestones/m7.md`, and this file; grep for
`game.play|undoLastMove|runLoop` in `SoftClub*.java` finds nothing.

## 7. Comparison

**TODO(you), after runs 2 and 3.** Run 1's answers, for the table:

- **Selected square:** `SoftClubView.selected` (`SoftClubView.java:56`). The
  board only draws a copy (`SoftClubBoard.java:53`).
- **Legal moves:** `game.legalMoves()`, filtered by `from` (`SoftClubView.java:322`).
- **Promotion:** asks with a modal dialog from the click, only for a legal
  promoting move (`SoftClubView.java:237`, `:344`).
- **Easier to change:** **TODO(you)**. **Kept:** **TODO(you)**.

## 8. Applied

**TODO(you):** an idea from run 2 or 3 that you did not keep, written into the
kept view, with file and line.

## 9. Debugger evidence

Real bug: the promotion dialog opened on illegal moves (see §5).
Symptom: clicking a piece onto an illegal square opened "promote to".
Evidence so far comes from reading the code and a scripted harness:
`GameController.java:87` calls `view.promotionChoice(game)` whenever a
four-letter move does not match, and the draft's `promotionChoice` opened the
dialog whenever it ran on the event thread.
Fix: §6 item 1.
**TODO(you):** a breakpoint on `GameController.java:87` (or in
`squareClicked`, `SoftClubView.java:218`) while playing an illegal move would
give the debugger evidence the handout asks for. Record what `notation` and
`move` held.

## 10. By hand

**TODO(you):** commit hash of your `M7 by hand:` commit, and what you had to
read to make the change.
