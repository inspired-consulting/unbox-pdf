# Paragraph overflow and truncation

Status: Implemented. Supersedes the original line-limit-only ellipsis design.

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
and padding. The current geometry is described in
[library principles](library-principles.md); the earlier measurement-width
defects were fixed after the initial ellipsis implementation.
`VerticalParagraph` remains a single rotated line and does not implement these
multiline policies. Font fallback and wrapping changes are outside this contract.

## Intentional API breaks and migration

The old signatures are removed so callers must review their selected behavior:

- `withOverflow(false)` becomes `with(Overflow.CLIP)`.
- `withOverflow(true)` becomes `with(Overflow.OVERFLOW)`.
- `limit(n, mode)` becomes `limit(n).with(mode)`.
- Mutable `TextWriter` overflow setters are removed; pass the mode to
  `write(stream, bounds, text, align, vAlign, lineLimit, mode)` instead.

Unlike the old bounds-only boolean, OVERFLOW now bypasses line truncation too.
Its cursor advancement follows allocated rather than overflowed content height.
Plain `limit(n)` and default CLIP preserve their existing behavior. Existing
PDF regression references must remain byte-identical.

## Validation

`ParagraphTest` covers configuration order and mode replacement, both limiting
constraints, zero visible capacity, and cursor advancement. `TextWriterTest`
covers height ellipsis, line-limit ellipsis, fitting text, and narrow widths.
`DocumentTest` checks existing PDF output byte-for-byte.
