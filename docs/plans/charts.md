# Charts

Status: idea, not specified.

## Goal

Give users building blocks for simple diagrams in reports, so they do not start
from an empty `Canvas`. The scope is much smaller than JavaScript chart libraries:
static, print-oriented charts with a clear API, not interactive or animated ones.

## Current state

- `Canvas` gives direct access to `PDPageContentStream` with a fixed inner height.
  Its only helper is `drawCircle`.
- `samples.DrawnContentOnTextLine` draws bars and dots aligned to text lines by hand.

## Scope ideas

Two layers:

1. **Basic tools**: scales that map data values to PDF coordinates (linear,
   categorical), axes with ticks and labels, grid lines, bars, lines, points,
   areas, arcs, and legends.
2. **High-level charts** built from these tools: bar charts (vertical,
   horizontal, stacked), line charts, and pie or donut charts.

High-level charts should be elements that take part in normal layout. Users who
need a different chart should be able to combine the basic tools themselves.

## Open questions

- Is a chart a `PdfElement` with a fixed height, or can its height be derived
  from width, data, and labels?
- How are axis and legend labels measured, so the plot area shrinks to fit them?
  Text measurement must use the same fonts as drawing.
- How do charts get their colours and fonts? This depends on [theming](theming.md).
- Which number and date formats do tick labels need, and who formats them?
- Is a chart allowed to break across pages? Probably not.
- Which helpers belong on `Canvas` and which in a separate `charts` package?

## Out of scope

- Interactivity, animation, and JavaScript chart feature parity.
- Automatic chart type selection or data aggregation.
