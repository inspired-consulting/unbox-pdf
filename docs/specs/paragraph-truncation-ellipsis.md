# Paragraph overflow and truncation

Status: Implemented. Originates from
[issue #16](https://github.com/inspired-consulting/unbox-pdf/issues/16).

## Goal

A reader can see that paragraph text was cut off. The caller chooses one explicit
policy for text that does not fit: omit it silently, mark the omission with an
ellipsis, or draw it anyway.

## Why

`limit(n)` cut text silently, sometimes in the middle of a word. In dense tables
with single-line cells, a reader could not tell a complete value from a fragment,
for example `Maschinenraum Deck` from `Maschinenraum Deck 3 achtern`.

Text can be cut by two constraints: the line limit and the available height. One
policy covers both, so a paragraph behaves the same whichever constraint applies.
The earlier boolean `withOverflow(...)` setting applied to the height only, and
the line limit had no setting at all.

`CLIP` is the default because it keeps the output of existing documents and
reference PDFs byte-identical.

## Configuration

A paragraph owns one `Overflow` mode, defaulting to `CLIP`. `TextWriter` receives
that mode per write and retains no mutable overflow configuration.

```java
paragraph(text).limit(3);                         // default CLIP
paragraph(text).limit(3).with(Overflow.ELLIPSIS);
paragraph(text).withInnerHeight(40).with(Overflow.OVERFLOW);
```

`limit(n)` sets only the allocated line count and requires a positive number.
`with(mode)` sets only the overflow policy and rejects null. Their order does not
matter; setting a new mode replaces the previous one.

## Rendering and layout

For CLIP and ELLIPSIS, the visible line capacity is the smaller of the explicit
line limit (if any) and the number of complete lines fitting the effective text
height. Height fitting preserves the existing 0.99 line-height rounding tolerance.

- `CLIP` omits excess lines without a marker.
- `ELLIPSIS` appends `…` to the last visible line only if text was omitted. The
  line is shortened until it and the marker fit the available width. If even
  the marker is too wide, a bare marker is drawn, preserving the original
  best-effort narrow-width behavior.
- `OVERFLOW` draws every wrapped line, ignoring the line limit and available
  height as drawing constraints.

If no complete line fits, CLIP and ELLIPSIS draw nothing. ELLIPSIS aligns the
visible lines vertically; OVERFLOW aligns all lines. CLIP retains its legacy
vertical alignment based on the line-limited text before height clipping.

Measurement still reserves the line-limited height, or the explicitly configured
inner height. Overflow does not enlarge that allocation: it returns the allocated
height plus margins, including rendering-hint padding for automatic height.
Overflowing text can overlap following content or extend outside a page; it does
not trigger paragraph splitting or additional pagination.

Measurement and drawing use the effective text width after paragraph margins
and padding. The geometry is described in
[library principles](library-principles.md).
`VerticalParagraph` remains a single rotated line and does not implement these
multiline policies. Font fallback and wrapping changes are outside this contract.

## API decisions

- The line limit and the overflow mode are separate settings: `limit(n)` and
  `with(mode)`. There is no combined `limit(n, mode)`, because the mode also
  applies to height clipping without a line limit.
- The boolean `withOverflow(...)` was removed instead of deprecated. `OVERFLOW`
  also bypasses the line limit, which the boolean did not, so callers must
  review which behavior they want. The
  [changelog](../../CHANGELOG.md) lists the replacements.
- `TextWriter` receives the mode with each `write(...)` call and has no overflow
  setters. A writer therefore carries no configuration that could leak between
  uses.
- Plain `limit(n)` and the default `CLIP` produce the same output as before the
  feature. PDF regression references stay byte-identical.

## Validation

`ParagraphTest` covers configuration order and mode replacement, both limiting
constraints, zero visible capacity, and cursor advancement. `TextWriterTest`
covers height ellipsis, line-limit ellipsis, fitting text, and narrow widths.
`DocumentTest` checks existing PDF output byte-for-byte.
