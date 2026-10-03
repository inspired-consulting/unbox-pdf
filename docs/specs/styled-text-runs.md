# Styled text runs

Status: Implemented.

## Goal

One paragraph can contain runs of text with different fonts, sizes, and colors.
A value and its unit, a label and a muted suffix, or a colored marker and grey
text share one line and one baseline.

## Why

Before this feature, callers imitated mixed styles with two half-width columns.
That only approximates the join between the two parts, and it breaks when either
part changes its width. Wrapping, alignment, and truncation also did not work
across the two columns.

## Configuration

```java
paragraph("12.5", bold).add(" kg", muted);
paragraph("Status: ").add("open", red).add(" since Monday");
```

- `TextRun(text, font)` is the unit. A plain paragraph is one run.
- `Paragraph.add(text, font)` appends a run. `add(text)` uses the paragraph font.
- `Paragraph.font()` and `Paragraph.runs()` expose the base font and the runs.

## Behavior

- `TextRunWriter` lays out all runs as one text. Words flow across run
  boundaries.
- Whitespace collapses to one space in the font of the following word. `\n`
  breaks the line. A word that is wider than the line is broken into pieces that
  fit.
- Each line is as tall as the tallest font on it. All pieces of a line share one
  baseline, so mixed sizes align.
- A paragraph whose runs are all blank produces no lines.
- Alignment, vertical alignment, line limit, and overflow follow `TextWriter`,
  including the ellipsis on the last visible line. See the
  [overflow contract](paragraph-truncation-ellipsis.md).
- A paragraph with exactly one run still uses `TextWriter`. Its output is
  byte-identical to the output before this feature, so existing reference PDFs
  did not change.
- `VerticalParagraph` renders a single rotated run and rejects appended runs.

## Not supported

- Runs that draw a shape inline, such as a legend swatch. The baseline and cap
  height metrics would allow a run with a fixed width and a draw callback, but
  no such run exists.
- Per-run decorations such as backgrounds or underlines.
