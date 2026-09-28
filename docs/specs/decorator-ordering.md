# Decorator ordering and level override

Status: Implemented. Extends the existing level-based sort in `AbstractDecoratable`
(added for backgrounds/borders) with a per-instance override API and settles the
"level" vs. "z-index" naming raised in issue #11.

`Decorator` already carries an `int level` used to order decorators before they
paint (backgrounds under borders, by default). What's missing is a way for a
caller to override that order for one specific decorator instance without
mutating shared state. This spec defines that override and the ordering
guarantees callers can rely on.

## Terminology

The codebase and [library principles](library-principles.md) already call this
value "level," not "z-index." This spec keeps that name: `Decorator.level`,
`Decorator(int level)`, `atLevel(int level)`. No renaming is introduced.

## Configuration

```java
Unbox.background(Color.LIGHT_GRAY);                          // default level 100
Unbox.background(Color.LIGHT_GRAY).atLevel(50);               // painted even earlier
BorderDecorator.border(1f, Color.BLACK).atLevel(1500);        // painted after the default border level
paragraph.with(Unbox.background(Color.WHITE).atLevel(0))
         .with(BorderDecorator.border(1f, Color.BLACK));      // background still first: 0 < 1000
```

`Decorator` gains a fluent `atLevel(int level)` method that mutates the
receiver's level and returns it (`Decorator`), matching the existing
`with(...)`/`align(...)`/`limit(...)` convention of mutate-and-return. The
`level` field, currently `private final`, becomes mutable so `atLevel` can set
it after construction. The existing constructors (`Decorator()`,
`Decorator(int level)`) are unchanged and continue to set the initial level;
`atLevel` is purely an override applied afterward, including after the
decorator has been attached with `with(...)`.

`BackgroundDecorator.LEVEL` (100) and `BorderDecorator.LEVEL` (1000) remain as
the default level each type's constructors pass to `super(level)`. They are
unchanged in meaning: the "default z-index" the issue asks for. `atLevel(...)`
is the per-instance "that can be overridden" half of the same sentence. No new
constructor overloads or `Unbox` factory overloads are needed for this, since
`atLevel(...)` composes with every existing construction path, including the
`BorderDecorator.border(...)` static factories.

## Behavior

- `AbstractDecoratable.decorators()` sorts by `level` ascending immediately
  before each render pass, so a level changed by `atLevel(...)` after
  `with(...)` is honored on the next render.
- The stored decorator list always keeps insertion order. `decorators()` sorts
  a copy of that list and never sorts the stored list in place. Today's
  implementation sorts in place, so it changes: an in-place sort would leave
  the previous render's order in the list, and a later tie would keep that
  order instead of insertion order.
- The sort is stable (`List.sort`/`Collections.sort` on the copy). Two
  decorators with equal levels paint in the order they were added via
  `with(...)`, on every render, including after an earlier render and a later
  `atLevel(...)` change. This tie-break is a documented guarantee, not an
  incidental property of the sort implementation.
- Every `int` is a valid level. `Decorator.compareTo` compares levels without
  overflow, using `Integer.compare(level, other.level)` instead of today's
  `level - other.level`. The subtraction overflows for widely separated
  levels, for example `Integer.MIN_VALUE` against any positive level, and
  would sort them in the wrong order.
- `atLevel(...)` may be called at any time, including after `wrap(...)` or
  `with(...)` has already attached the decorator, and including mid-composition.
  There is no "sealed" state that rejects a later override; sorting happens at
  render time, not at attach time, so a level set at any point before rendering
  starts takes effect. Mutating `level` on a `Decorator` that is currently being
  rendered, or rendering the same `AbstractDecoratable` concurrently from
  multiple threads, is not supported, consistent with this library's existing
  single-threaded, render-is-not-pure posture (see
  [library principles](library-principles.md)).
- `Container` is unaffected by this spec: it continues to apply decorators in
  insertion order regardless of `level`, as already documented in library
  principles. This is a deliberate decision (not just an artifact of the
  current implementation): unifying `Container` onto level-based sorting would
  change existing rendering behavior for any caller that currently attaches a
  border before a background to a `Container`, including byte-for-byte
  `DocumentTest` references. Keeping `Container`'s behavior stable takes
  priority over consistency between the two decoration mechanisms for now.
  The same built-in `Decorator` classes therefore continue to order
  differently depending on whether they're attached via `Container` or via
  `AbstractDecoratable`; a future spec can revisit unifying them if that
  inconsistency becomes a real problem.

## Validation

Add a `DecoratorTest` (new, under `src/test/java/inspired/pdf/unbox/decorators/`)
covering:

- Default ordering: a `BackgroundDecorator` and `BorderDecorator` added to the
  same `AbstractDecoratable`-based element in either `with(...)` order both
  paint background before border (background level 100 < border level 1000).
- `atLevel(...)` override: a `BorderDecorator.atLevel(0)` added alongside a
  default `BackgroundDecorator` paints the border first, reversing the default.
- Equal-level tie-break: two decorators given the same explicit level paint in
  the order they were added.
- `atLevel(...)` called after `with(...)` still affects the next render, since
  sorting happens at render time, not at attach time.
- Tie-break after a re-sort: add `A` (level 10), then `B` (level 5), render,
  then call `B.atLevel(10)`. The next render paints `A` before `B`.
- Extreme levels: a decorator at `Integer.MIN_VALUE` paints before one at
  `Integer.MAX_VALUE`, whichever is added first.

Existing `DocumentTest` byte-for-byte references are unaffected unless a test
deliberately reorders a decorator on an existing reference document.
