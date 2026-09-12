# Styled text runs

Status: implemented.

## Goal

Let one paragraph contain runs of text with different fonts, sizes, and colors,
so that a value and its unit, a label and a muted suffix, or a colored marker and
grey text can share one line and one baseline. Consumers previously faked this
with two half-width columns, which only approximates the join and breaks when
either part changes width.

## Design

- `TextRun(text, font)` is the unit. `Paragraph.add(text, font)` appends a run;
  `add(text)` uses the paragraph font. A plain paragraph is one run.
- `TextRunWriter` lays runs out as one text: words flow across run boundaries,
  whitespace collapses to one space in the font of the following word, `\n` breaks
  the line, and an overlong word is broken into pieces that fit. Each line is as
  tall as the tallest font on it, and all pieces on a line share the baseline.
- Alignment, vertical alignment, line limit, and overflow follow `TextWriter`,
  including the ellipsis on the last visible line.
- Paragraphs with exactly one run keep using `TextWriter`, so existing output is
  byte-identical and the reference PDFs do not change.

## Out of scope

- Runs that draw a shape inline, such as a legend swatch. The layout could accept
  a run with a fixed width and a draw callback later; the baseline and cap height
  metrics already allow drawing next to text.
- Per-run decorations such as backgrounds or underlines.
