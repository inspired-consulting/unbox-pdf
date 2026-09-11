# Paragraph truncation ellipsis

Status: Proposed. Tracks [issue #16](https://github.com/inspired-consulting/unbox-pdf/issues/16).

## Problem

`Paragraph.limit(n)` caps a paragraph at `n` wrapped lines via
`TextWriter.write(..., lineLimit)`. When the wrapped text needs more lines than
the limit, the excess lines are dropped silently:

```java
List<String> chunks = chunk(text, bounds.width());
if (lineLimit != null && lineLimit > 0 && lineLimit < chunks.size()) {
    chunks = chunks.subList(0, lineLimit);   // hard cut, nothing appended
}
```

The last rendered line ends wherever word wrap happened to break — including
mid-word, since `TextTokenizer.breakUp()` splits a single token that is wider
than `maxWidth` on its own. A reader has no way to tell truncated text from a
short value that happens to fit, which matters for dense single-line table
cells (e.g. `limit(1)` on room or device names in a landscape cable-pull-list
table).

## Goals

- Let a caller mark a `Paragraph` so that, when `limit(n)` actually truncates
  the wrapped text, the last visible line ends with an ellipsis (`…`) instead
  of an abrupt cut.
- Keep the ellipsis inside `bounds.width()`: trim trailing characters from the
  last kept line until `font.width(line + "…") <= maxWidth`.
- Preserve current output — and therefore all byte-for-byte `DocumentTest`
  references — for every `Paragraph` that does not opt in.

## Non-goals

- Changing wrapping, hyphenation, or `TextTokenizer.breakUp()` behavior.
- Changing `calculateHeight(...)`: truncation never changes the number of
  rendered lines, only the content of the last one, so measured height is
  unaffected.
- A font-glyph fallback from `…` to `...` for fonts lacking the glyph. Not
  required for the motivating use case (Helvetica has `…`); worth a follow-up
  if a caller hits it.
- Ellipsis support for `VerticalParagraph` (single-line rotated text; not
  affected by `lineLimit`).

## Proposed API

Extend `Overflow` handling at the point it is already configured — the
`limit(...)` call — rather than adding a second, independently-settable flag
that callers could set inconsistently with `limit(...)`:

```java
public enum Overflow { CLIP, ELLIPSIS }
```

```java
Paragraph limit(int lineLimit);                    // unchanged; CLIP, same as today
Paragraph limit(int lineLimit, Overflow overflow);  // new
```

```java
paragraph(text, font).limit(1);                     // current behavior (CLIP)
paragraph(text, font).limit(1, Overflow.ELLIPSIS);  // new: append "…" when truncated
```

`limit(int)` delegates to `limit(int, Overflow.CLIP)`, so `Overflow` is
established as a `Paragraph`-level concept alongside `Align`/`VAlign` rather
than a `TextWriter`-only detail — consistent with how the library already
threads `Align`/`VAlign` through `Paragraph.align(...)` into `TextWriter`.

`Paragraph` stores the mode next to `lineLimit` and passes both through to
`TextWriter.write(...)` and, if it needs the mode too, `calculateHeight(...)`
(it doesn't — see Goals). `TextWriter` gains an `Overflow` parameter on the
line-limit-aware `write(...)` overload; existing overloads that omit
`lineLimit` are unaffected and keep defaulting to `CLIP`.

## Behavior

When `lineLimit != null && lineLimit > 0 && lineLimit < chunks.size()`, the
line list is truncated as today. If the mode is `ELLIPSIS`, the last kept line
is replaced by an ellipsis-trimmed version:

```java
List<String> kept = new ArrayList<>(chunks.subList(0, lineLimit));
if (overflow == Overflow.ELLIPSIS) {
    int last = kept.size() - 1;
    kept.set(last, withEllipsis(kept.get(last), bounds.width()));
}
chunks = kept;
```

`withEllipsis(line, maxWidth)`:

1. If `font.width(line + "…") <= maxWidth`, return `line + "…"`.
2. Otherwise drop trailing characters from `line` one at a time (trimming any
   trailing space left behind) until the appended form fits, then return it.
3. If even a bare `"…"` does not fit `maxWidth` (pathological — narrower than
   one glyph), return `"…"` anyway rather than an empty string, matching the
   library's existing best-effort behavior for content that cannot fit
   (`withOverflow`/`enoughSpace` already allow bounds to be exceeded rather
   than silently drop content).

Only the last *kept* line is touched. Lines before it, and paragraphs where
`lineLimit >= chunks.size()` (no truncation actually happens), are unchanged —
the ellipsis is a truncation marker, not a decoration applied whenever
`limit(...)` is set.

`calculateHeight(text, viewPort, lineLimit)` is unaffected: it already caps
`count` at `lineLimit`, and swapping the last line's text for an
ellipsis-trimmed version doesn't change the line count.

## Interaction with existing code

- `TextTokenizer` is unchanged; ellipsis trimming happens after chunking, in
  `TextWriter`, operating on the already-wrapped last line.
- `Paragraph.render(...)` and `Paragraph.innerHeight(...)` pass `lineLimit`
  through unchanged; add the `Overflow` field alongside `lineLimit` and thread
  it through the same call sites.
- Default construction (`limit(int)`, or no `limit(...)` call at all) keeps
  today's output byte-for-byte, so no `DocumentTest` reference PDFs need
  regeneration for this change alone.

## Testing

Per `AGENTS.md`, add focused tests rather than relying on a full-document
regression:

- `TextWriter`/`TextTokenizer`-level tests (or a `Paragraph` test using a
  known-width `SimpleFont`) covering:
  - A line limit that truncates: last line ends with `…` and
    `font.width(line) <= maxWidth`.
  - A line limit that does *not* truncate (`lineLimit >= wrapped line count`):
    no ellipsis appended.
  - Truncation that lands mid-word (`breakUp()` case): ellipsis still fits and
    the line is trimmed further than the plain `CLIP` cut would be.
  - `limit(n)` (no `Overflow` argument) still produces today's unmodified
    output, to guard the default-unchanged goal.
- If a document-level example is useful for visual review, add it under
  `samples/`, not as a new `DocumentTest` reference, unless the reviewer
  explicitly wants a byte-for-byte regression fixture for this feature.

## Source references

- [`Paragraph.limit(...)`](../../src/main/java/inspired/pdf/unbox/elements/Paragraph.java)
- [`TextWriter.write(...)`](../../src/main/java/inspired/pdf/unbox/internal/TextWriter.java)
- [`TextTokenizer.breakUp(...)`](../../src/main/java/inspired/pdf/unbox/internal/TextTokenizer.java)
- [`Font.width(...)`](../../src/main/java/inspired/pdf/unbox/Font.java)
- [Library principles: text and lower-level drawing](library-principles.md#text-and-lower-level-drawing)
