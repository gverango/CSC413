# M7 submission

> Items marked **TODO(you)** need facts only you have, or work only you may do.
> Everything else describes what is in the repository now.

## 1. Group

Alone.

## 2. Tools

Claude Code (desktop app, Code tab).

- Run 1: Claude Opus 5.5 (`claude-opus-5-5`). **TODO(you):** if the first
  draft of run 1's Swing view came from a different tool or model, name it here.
- Runs 2 and 3: Claude Sonnet 5.5 (`claude-sonnet-5-5`), each in a fresh
  session with no access to the other runs.
- `AnsiView`, `AGENTS.md`, the merge, and the applied change: Claude Opus 5.5.

## 3. Other view

`ansi`: `java -cp target/classes edu.sfsu.csc413.chess.Main ansi`

`view/AnsiView.java`: a bordered terminal board in ANSI colour, redrawn from the
side to move every turn (`AnsiView.java:51`), with the checked king's square
tinted (`AnsiView.java:126`). It drives through `controller.runLoop()` like the
console view (`AnsiView.java:89`). It was tested by piping moves in: an illegal
move, `undo` with nothing to undo, `moves`, `quit`, and Fool's Mate to
checkmate.

## 4. Runs

| Branch | Model | Request | Change from previous run |
|---|---|---|---|
| `m7-run-1` (`1d2d956`) | Opus 5.5 | The base request (Appendix A). **TODO(you):** also paste whatever request produced the first draft that was already in the working tree. | first run |
| `m7-run-2` (`63fd5eb`) | Sonnet 5.5 | The base request (Appendix A), word for word. | **Model only:** Opus 5.5 → Sonnet 5.5. |
| `m7-run-3` (`b94e30d`) | Sonnet 5.5 | The base request plus one added instruction (Appendix B). | **One instruction:** "every console action must be reachable in the window". Same model as run 2. |

Runs 2 and 3 also received short setup lines that are not part of the request:
the branch name, a commit-message format, "wire it into `Main` as `swing`", and
"don't read other branches".

Kept: **run 1**, merged into `main` in `a8499d9`.

## 5. `AGENTS.md`

Rule: **"Modal dialogs ... Never open one from `promotionChoice()`."**
Why: run 1's first draft opened the promotion dialog from `promotionChoice()`.
`GameController.java:87` calls `promotionChoice()` for *every* four-letter move
it cannot match, so any illegal click (for example e2→e5) opened
"promote to". With the rule in `AGENTS.md`, runs 2 and 3 both asked in the view,
only for a real promotion (`m7-run-2` `SoftClubBoardPanel.java:118`, `m7-run-3`
`SoftClubBoardPanel.java:101`), and neither overrides `promotionChoice()`.

Note on order: `AGENTS.md` was committed (`6111a2a`) after run 1's code had
been generated, but before runs 2 and 3.

## 6. Review (kept view, on `main`)

1. **`SoftClubView.java:237` and `:257`: promotion.** `promotes()` scans
   `game.legalMoves()` for a move from/to that `isPromotion()`. Only then does
   the view ask, and it sends five-letter notation (`b7a8N`), so the controller
   matches it on the first try. *Checked:* a scripted game sent real mouse
   presses. Black's illegal d8→d4 showed "Not a legal move: 'd8d4'." with no
   dialog. bxa8 opened the dialog; choosing knight left `N` on a8 (board FEN
   `Nn2kbnr/...`). *Decision:* **changed**, replacing the draft's
   `promotionChoice()` override, which caused the bug in §5.
2. **`SoftClubView.java:324`: legal-move hints.** `hintsFor()` keeps the
   destinations of `game.legalMoves()` (`:329`) whose `from` is the selected
   square, so the view computes no rule itself. *Checked:* read the code; with
   d8 selected after f3 e5 g4, the hints were exactly e7, f6, g5, h4.
   *Decision:* **kept**.
3. **`SoftClubBoard.java:230`: click to square.** `positionAt()` turns pixels
   into a `Position`. Screen rows count down and ranks count up, hence
   `BOARD_SIZE - 1 - row` at `:242`. *Checked:* every scripted click landed on
   the intended square. *Decision:* **kept**.
4. **`SoftClubView.java:202`: new game.** There is no new-game `PlayerAction`.
   The button sends one `Undo` per move in `game.history()` through
   `controller.handle` (`:210`), so the view never calls `undoLastMove`.
   *Checked:* after a promotion game, new game restored the starting FEN.
   *Decision:* **added**.
5. **`SoftClubView.java:94`: `close()`.** The controller calls `close()` after
   "Goodbye." and also after checkmate or stalemate. Only Goodbye ends the
   session, so after mate the window stays up and undo and new game still work.
   *Checked:* undo after Fool's Mate returned to "black to move". *Decision:*
   **changed**: the draft disabled every button after mate.

Rule checks on `main`: `./mvnw test` 83/83 green with no test changed;
`git diff --stat submit-m6` lists only allowed files; grep for
`game.play|undoLastMove|runLoop` in `SoftClub*.java` finds nothing.

## 7. Comparison

| | Run 1 (kept) | Run 2 | Run 3 |
|---|---|---|---|
| **Selected square lives in** | the view: `SoftClubView.java:56`. The board only draws a copy (`SoftClubBoard.java:53`). | the board panel: `SoftClubBoardPanel.java:49` | the board panel: `SoftClubBoardPanel.java:55` |
| **Legal moves from** | `game.legalMoves()`, filtered by `from` (`SoftClubView.java:329`) | `game.legalMoves()` (`SoftClubBoardPanel.java:105`) | `game.legalMoves()` (`SoftClubBoardPanel.java:121`) |
| **Promotion** | own modal dialog, opened from the click, only for a legal promotion (`SoftClubView.java:237`, `:351`) | `JOptionPane` from the click (`SoftClubBoardPanel.java:141`); closing it cancels (`:124`) | `JOptionPane` from the click (`SoftClubBoardPanel.java:102`); closing it cancels (`:105`) |
| **Buttons** | new game, undo, moves, quit | undo | undo, legal moves, quit (`SoftClubView.java:135-137`) |
| **Check tint** | check and checkmate (`SoftClubView.java:316`) | check only, not at mate (`SoftClubBoardPanel.java:276`) | check and checkmate (`SoftClubBoardPanel.java:313`) |

**Easier to change:** **TODO(you)**, in your own words. A starting point: run 1
keeps the half-finished move in the view and leaves the board as a pure
painter, so changing input means editing one class. Runs 2 and 3 put
selection and promotion inside the board panel, which keeps the view shorter
but mixes drawing with input.

**Kept:** run 1. It is the only run with new game as well as every console
action (undo, moves, quit).

## 8. Applied

From runs 2 and 3: **closing the promotion dialog cancels the move** instead
of silently choosing a queen (`m7-run-2` `SoftClubBoardPanel.java:124`,
`m7-run-3` `SoftClubBoardPanel.java:105`). In the kept view it is
`SoftClubView.java:242`, and `askPromotion()` now starts from `null`
(`SoftClubView.java:351`). Commit `46e167e`. *Checked:* closed the dialog on
bxa8; nothing was played, b7 stayed selected; then chose knight and it played.

## 9. Debugger evidence

Real bug: the promotion dialog opened on illegal moves (see §5).
Symptom: clicking a piece onto an illegal square opened "promote to".
Evidence so far comes from reading the code and a scripted harness:
`GameController.java:87` calls `view.promotionChoice(game)` whenever a
four-letter move does not match, and the draft's `promotionChoice` opened the
dialog whenever it ran on the event thread.
Fix: §6 item 1.
**TODO(you):** set a breakpoint on `GameController.java:87` (or in
`squareClicked`, `SoftClubView.java:218`) while playing an illegal move, and
record what `notation` and `move` held. That is the debugger evidence the
handout asks for.

## 10. By hand

**TODO(you):** commit hash of your `M7 by hand:` commit, and what you had to
read to make the change.

---

## Appendix A: base request (runs 1, 2 and 3)

> You’re working in an existing Java chess engine codebase. Your task is to add a new Swing view with a Gen X Soft Club look. The engine logic, rules and move handling must stay exactly as they are. This is a visual layer only.
> Step 1: Read before you write.
> Before changing anything, look through the codebase and write a short summary (under 15 lines) of:
>
> * How the current UI (if there is one) is structured: class names, packages, and how it talks to the engine (direct calls, listeners, observer, MVC, etc.)
> * The entry point and how the view is launched
> * Code style conventions: naming, package layout, indentation, use of `final`, comments/Javadoc, how resources are loaded, Java version
> * Where board state, move input and game status (check, mate, turn) come from
>
> Don’t continue until you understand these. If there’s an existing view, the new one should plug into the engine the same way it does.
> Step 2: Build the view.
> Constraints (these matter more than the aesthetic):
>
> * Do not modify engine, rules, move generation, or game-state classes. If you think a change there is unavoidable, stop and explain why instead of making it.
> * All existing functionality must still work in the new view: selecting and moving pieces, legal-move enforcement, turn display, check/checkmate/stalemate, promotion, new game, and anything else the current UI supports [list any specific features here].
> * Match the codebase’s existing style and patterns. New code should look like the same person wrote it.
> * Keep it small. Aim for roughly 2–4 new classes (e.g. a frame/panel, a board component, a small theme/palette class). No frameworks, no new dependencies, no look-and-feel libraries, no abstraction layers, factories or plugin systems “for future themes.”
> * Use plain Swing and Java2D (`paintComponent`, `GradientPaint`, `AlphaComposite`, `RoundRectangle2D`, antialiasing hints). No animation unless it’s trivial and doesn’t touch game logic.
> * If the codebase has a way to switch views, register this one there. Otherwise add the simplest launch option that fits how the app already starts [or: replace the existing view / add a command-line flag / add a menu item].
>
> Aesthetic: Gen X Soft Club (late-’90s/early-2000s ambient, Y2K-adjacent futurism):
>
> * Palette: pale sky blue, ice/aqua, frosted white, soft silver/gray, with a small accent of lime or soft cyan. Low contrast and calm, nothing saturated or harsh. Put all colors in one constants/theme class.
> * Surfaces: soft vertical gradients (white to pale blue), frosted translucent-looking panels made with semi-transparent fills, subtle glossy highlights on the top half of buttons and panels, like iMac G3 or Dreamcast-era plastic.
> * Shapes: rounded corners everywhere, thin 1px light borders, generous padding, airy spacing.
> * Board: light squares in frosted white/ice, dark squares in muted powder blue or silver-blue. Selected square and legal-move hints use a soft glowing aqua or lime (translucent overlay or rounded dot), never a hard outline.
> * Typography: clean geometric or grotesque sans (use `SansSerif` or check for fonts like Helvetica/Verdana and fall back gracefully). Small sizes, lowercase labels, wide letter spacing where practical (e.g. “white to move”, “new game”).
> * Status/info area: a frosted side or bottom panel with minimal lowercase text. The tone is calm and ambient, like a late-’90s Japanese electronic CD insert.
> * Pieces: reuse the existing piece images or rendering if there are any. If pieces are drawn with Unicode glyphs, render them in a soft dark slate with a faint light drop shadow. Don’t add new image assets unless the codebase already loads images that way.
>
> Step 3: Verify.
>
> * Build the project and run any existing tests. All must pass, and no engine files should appear in your diff.
> * Play through a short game in the new view, including a capture, check, castling, en passant and promotion if supported, and confirm behavior matches the original UI.
> * Finish with a brief summary: files added or changed, how to launch the view, and anything you chose not to do.

## Appendix B: run 3's one added instruction

Inserted into Step 2's constraints, right after the "All existing functionality"
bullet:

> * Every action the original console game offers must also be reachable in the window: undo, listing the possible (legal) moves, and quit, each sent through the controller, along with any messages the console shows for them.
