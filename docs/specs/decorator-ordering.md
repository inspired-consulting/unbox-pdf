# Decorator ordering and level override

Status: Implemented. Originates from issue #11.

## Goal

Decorators have a natural paint order: a background is painted before a border,
whatever order the caller attaches them in. A caller can override that order for
one decorator instance.

## Why

Without an order, the result depends on the sequence of `with(...)` calls. A
background attached after a border paints over the inner half of the border
stroke. Callers had to know and repeat the correct sequence everywhere.

A per-type default alone is not enough. Some layouts need an exception, for
example a border below a background, and changing the default of a type would
affect every element that uses that type.

## Terminology

The value is called "level", not "z-index": `Decorator.level`,
`Decorator(int level)`, `atLevel(int level)`. Issue #11 used "z-index" as a
working name. "Level" was already used in the code and in
[library principles](library-principles.md), and it avoids suggesting CSS
stacking-context behavior that the library does not have.

## Configuration

```java
Unbox.background(Color.LIGHT_GRAY);                          // default level 100
Unbox.background(Color.LIGHT_GRAY).atLevel(50);               // painted even earlier
BorderDecorator.border(1f, Color.BLACK).atLevel(1500);        // painted after the default border level
paragraph.with(Unbox.background(Color.WHITE).atLevel(0))
         .with(BorderDecorator.border(1f, Color.BLACK));      // background still first: 0 < 1000
```

- Each decorator type passes its default level to the `Decorator(int level)`
  constructor. `BackgroundDecorator.LEVEL` is 100 and `BorderDecorator.LEVEL` is
  1000. Lower levels paint first.
- `atLevel(int level)` overrides the level of one instance. It mutates the
  receiver and returns it, like the other fluent methods of the library.
- `atLevel(...)` works with every construction path, including the `Unbox`
  factories and the `BorderDecorator.border(...)` static factories. No extra
  constructor or factory overloads exist for levels.

## Behavior

- `AbstractDecoratable.decorators()` returns the decorators sorted by level in
  ascending order. It sorts immediately before each render pass, so a level
  changed after `with(...)` takes effect on the next render. There is no sealed
  state that rejects a later override.
- The stored decorator list keeps insertion order. `decorators()` sorts a copy.
  An in-place sort would keep the order of an earlier render in the list, and a
  later tie would then follow that order instead of insertion order.
- The sort is stable. Two decorators with equal levels paint in the order they
  were added with `with(...)`, on every render. This tie-break is a guarantee,
  not a side effect of the sort implementation.
- Every `int` is a valid level. `Decorator.compareTo` uses `Integer.compare`,
  because subtracting levels overflows for widely separated values such as
  `Integer.MIN_VALUE` and a positive level.
- Changing the level of a decorator while it renders, or rendering the same
  element from several threads, is not supported. This matches the
  single-threaded rendering model in [library principles](library-principles.md).

## Container keeps insertion order

`Container` applies its decorators in insertion order and ignores levels. This is
a deliberate decision. Sorting by level in `Container` would change the output of
existing documents that attach a border before a background, including the
byte-for-byte `DocumentTest` references. Stable output has priority over
consistency between the two decoration mechanisms.

As a result, the same decorator classes order differently on a `Container` and on
an `AbstractDecoratable`. A later specification can unify them if this becomes a
real problem.

A decorator attached through the default `PdfElement.with(...)` wraps the element.
There is no list to order, so the level has no effect there.

## Validation

`DecoratorTest` covers the default order for both attachment sequences, the
`atLevel(...)` override before and between renders, the insertion-order tie-break
including after an earlier re-sort, extreme levels, and the insertion order of
`Container`. Existing `DocumentTest` references are unchanged by this feature.
