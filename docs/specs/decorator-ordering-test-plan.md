# Test plan: decorator ordering and level override

Status: Implemented. Covers testing for
[decorator-ordering.md](decorator-ordering.md), which settles the "level" vs.
"z-index" question from issue #11.

This plan expands the spec's [Validation](decorator-ordering.md#validation)
section into test cases you can check one at a time. It does not implement
`Decorator.atLevel(int)`. Write these tests once `atLevel` exists and
`Decorator.level` is no longer `final`.

## Scope

- A new `DecoratorTest` in `src/test/java/inspired/pdf/unbox/decorators/`, as
  the spec requires. Most cases test ordering with a small recording
  `Decorator` subclass declared inside the test. Its `decorate(...)` appends a
  name to a shared list and returns `0`. This checks paint order directly,
  without parsing PDF operators.
- A few cases also check real output with the `PDFStreamParser` pattern from
  `RoundedCornerTest` and `TableCellDecoratorTest`. A `BackgroundDecorator`
  emits a fill (`f`) and a `BorderDecorator` emits a stroke (`S`), so the order
  of the first `f` and the first `S` shows which one painted first.
- The host element is a `Paragraph` rendered into a fixed `Bounds` inside a
  throwaway `Document`, set up as in `RoundedCornerTest.render(...)`.
  `Paragraph` extends `AbstractDecoratable` and calls `applyDecorators` once per
  render.
- No new `DocumentTest` reference PDF is needed. The spec says existing
  references are unaffected (TC10).

## Test cases

### TC1 — Default order: background before border, background added first

- Level: Integration
- Setup: `new Paragraph("box").with(Unbox.background(Color.LIGHT_GRAY)).with(BorderDecorator.border(1f, Color.BLACK))`.
  Render it once and parse page 0's operators.
- Expect: the first `f` comes before the first `S`.
- Guards: spec Validation — "a `BackgroundDecorator` and `BorderDecorator` ...
  in either `with(...)` order both paint background before border (background
  level 100 < border level 1000)."

### TC2 — Default order: background before border, border added first

- Level: Integration
- Setup: same as TC1, but call `with(border(...))` before
  `with(background(...))`.
- Expect: the first `f` still comes before the first `S`.
- Guards: same clause as TC1 ("in either `with(...)` order"). This is the case
  that actually shows the sort doing work.

### TC3 — `atLevel` returns the same instance and sets its level

- Level: Unit
- Setup: `BackgroundDecorator d = Unbox.background(Color.WHITE)`, then
  `Decorator r = d.atLevel(50)`.
- Expect: `r` and `d` are the same object (`assertSame`). Compared with a
  recording decorator built at level 100 (through `super(100)`), `d` now sorts first
  (`d.compareTo(other) < 0`).
- Guards: spec Configuration — "`atLevel(int level)` ... mutates the receiver's
  level and returns it (`Decorator`)". The comparison avoids needing a `level`
  getter, which the spec doesn't add.

### TC4 — `atLevel` override reverses the default order

- Level: Integration
- Setup: `with(Unbox.background(Color.LIGHT_GRAY))`, then
  `with(BorderDecorator.border(1f, Color.BLACK).atLevel(0))`. Render once.
- Expect: the first `S` comes before the first `f`.
- Guards: spec Validation — "a `BorderDecorator.atLevel(0)` added alongside a
  default `BackgroundDecorator` paints the border first, reversing the
  default."

### TC5 — Lowering a background keeps it first

- Level: Integration
- Setup: the spec's own example,
  `paragraph.with(Unbox.background(Color.WHITE).atLevel(0)).with(BorderDecorator.border(1f, Color.BLACK))`.
- Expect: the first `f` comes before the first `S`.
- Guards: spec Configuration — "background still first: 0 < 1000". This checks
  that a documented example matches actual behavior. Drop it if a reviewer
  thinks TC1 already covers it.

### TC6 — Equal levels paint in the order they were added

- Level: Integration
- Setup: recording decorators `A` and `B`, both built at level 500 (through
  `super(500)`, or both given `.atLevel(500)`), added as `with(A).with(B)`. Add a third
  decorator `C` at level 500 after them. Render once.
- Expect: the recorded order is `[A, B, C]`.
- Guards: spec Behavior — "Two decorators with equal levels keep the relative
  order in which they were added via `with(...)`. This tie-break is now a
  documented guarantee." The equal-level case in the spec's Validation list.

### TC7 — `atLevel` after `with(...)`, before the first render

- Level: Integration
- Setup: recording decorators `A` (level 100) and `B` (level 200), added as
  `with(A).with(B)`. Before rendering, call `A.atLevel(300)`. Render once.
- Expect: the recorded order is `[B, A]`.
- Guards: spec Validation — "`atLevel(...)` called after `with(...)` still
  affects the next render, since sorting happens at render time, not at attach
  time."

### TC8 — `atLevel` between two renders affects the second render

- Level: Integration
- Setup: `A` (level 100) and `B` (level 200), added as `with(A).with(B)`.
  Render once and clear the recording. Call `B.atLevel(50)`, then render the
  same `Paragraph` again into a new `Document`.
- Expect: the first render records `[A, B]` and the second records `[B, A]`.
- Guards: spec Behavior — "a level changed by `atLevel(...)` after `with(...)`
  is honored on the next render" and "There is no 'sealed' state that rejects
  a later override." This is different from TC7 because the list has already
  been sorted once.

### TC9 — `Container` ignores levels and keeps insertion order

- Level: Integration
- Setup: a `Container` from `Unbox.column()` holding one small `Paragraph`.
  Call `with(BorderDecorator.border(1f, Color.BLACK))`, then `with(Unbox.background(Color.LIGHT_GRAY))`. Render once. Repeat with a
  second pair of recording decorators, where the one added first has level
  `1000` and the second has level `0` set through `atLevel`.
- Expect: in the operator stream the first `S` comes before the first `f`
  (border first, because it was added first). The recording pair is recorded
  in insertion order regardless of level.
- Guards: spec Behavior — "`Container` ... continues to apply decorators in
  insertion order regardless of `level` ... This is a deliberate decision."
  This case pins that decision so an accidental change shows up as a failure.

### TC10 — Existing references stay byte-for-byte identical

- Level: System
- Setup: the unchanged `DocumentTest` suite after `atLevel` is added, the
  `level` field is made mutable, `decorators()` sorts a copy, and `compareTo`
  uses `Integer.compare`. None of these changes the order for today's fixed,
  small levels, so no output should change.
- Expect: every existing `Files.mismatch` comparison still returns `-1`. No
  reference PDF is regenerated.
- Guards: spec Validation — "Existing `DocumentTest` byte-for-byte references
  are unaffected unless a test deliberately reorders a decorator on an
  existing reference document." This needs no new test code. It is covered by
  running `./mvnw test`.

### TC11 — Ties keep insertion order after an earlier re-sort

- Level: Integration
- Setup: recording decorators `A` (level 10) and `B` (level 5), added as
  `with(A).with(B)`. Render once, which records `[B, A]`, then clear the
  recording. Call `B.atLevel(10)` and render the same `Paragraph` again into a
  new `Document`.
- Expect: the second render records `[A, B]`. Against today's in-place sort it
  records `[B, A]`, so this test fails until `decorators()` sorts a copy.
- Guards: spec Behavior — "The stored decorator list always keeps insertion
  order. `decorators()` sorts a copy" and "Two decorators with equal levels
  paint in the order they were added ... including after an earlier render and
  a later `atLevel(...)` change." Also the spec's "Tie-break after a re-sort"
  Validation item.

### TC12 — Extreme levels sort without overflow

- Level: Unit and Integration
- Setup: recording decorators `LOW` at `atLevel(Integer.MIN_VALUE)` and `HIGH`
  at `atLevel(Integer.MAX_VALUE)`. Unit: call `compareTo` in both directions.
  Integration: add them to a `Paragraph` as `with(HIGH).with(LOW)`, render,
  and repeat with a new `Paragraph` in the order `with(LOW).with(HIGH)`.
- Expect: `LOW.compareTo(HIGH) < 0` and `HIGH.compareTo(LOW) > 0`. Both renders
  record `[LOW, HIGH]`. Also check `Integer.MIN_VALUE` against a small positive
  level such as `1`, where subtraction overflows. With today's `level -
  other.level` these assertions fail.
- Guards: spec Behavior — "Every `int` is a valid level. `Decorator.compareTo`
  compares levels without overflow." Also the spec's "Extreme levels"
  Validation item.

## Out of scope for this plan

- Concurrent rendering, or changing a level while its decorator is being
  rendered. The spec explicitly says these are not supported.
- Decorators attached through `PdfElement.with(...)`'s default `wrap(...)`
  path. The result is a single wrapped element and there is no list to order,
  so `atLevel` has nothing to reorder there.
- Renaming to "z-index." The spec's Terminology section rules this out, so
  there is nothing to test.
- Table segment decoration timing (tables apply decorations after rendering
  rows). This is existing, documented behavior that the spec leaves unchanged.
- `BackgroundDecorator.LEVEL` and `BorderDecorator.LEVEL` keeping their values
  (100/1000). TC1 and TC2 already depend on those defaults, so a separate
  constant-value assertion would repeat them.
