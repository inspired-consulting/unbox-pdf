# Library patterns and principles

Status: Initial specification, based on the current implementation.

This document describes the design of Unbox PDF and the conventions to preserve
when extending it. Statements about existing behavior are descriptive; extension
guidelines express the intended direction rather than guarantees enforced by the
current code.

## Purpose and scope

Unbox PDF is a Java library for creating PDFs programmatically on top of Apache
PDFBox. It provides reusable layout and drawing components with a fluent API.
Margins, padding, borders, backgrounds, rows, and columns borrow familiar concepts
from HTML, but layout is implemented directly in Java rather than through an HTML
or CSS engine.

The library favors sequential, top-to-bottom document construction. It exposes
PDFBox objects where callers need lower-level control. It is not an application,
template language, or general browser layout engine.

## Architecture and responsibilities

| Area | Responsibility |
| --- | --- |
| `inspired.pdf.unbox` | Document lifecycle, geometry, fonts, alignment, event hooks, and convenience factories. |
| `elements` | Renderable paragraphs, containers, tables, cells, and canvases. |
| `elements.internal` | Shared implementations, layout strategies, rendering hints, and table pagination. |
| `base` | Column and table models, relative widths, and default cell selection. |
| `decorators` | Visual treatments such as backgrounds and borders. |
| `internal` | Text measurement and drawing, tokenization, font helpers, and exception adaptation. |
| `themes` | Reusable color constants. |

The normal rendering path is:

```text
Caller builds elements
    -> Document measures and positions an element
    -> Element delegates to layouts, decorators, and text/drawing helpers
    -> PDFBox writes page content
    -> Caller saves and closes the finished PDDocument
```

Keep document lifecycle, layout calculations, content rendering, and decoration
as separate responsibilities. Use existing extension points before adding new
responsibilities to `Document`.

## Document lifecycle and ownership

`Document` owns a `PDDocument`, a current page, a content stream, and a downward
cursor. Pages and streams are initialized lazily. Page creation currently uses A4,
with portrait or landscape orientation.

`Document.render(element)` measures the element, checks available page space,
renders it in the current viewport, and advances the cursor by the returned
distance. Rendering writes content immediately; it does not build a document tree
for a later global layout pass.

`finish()` ensures at least one page exists, closes the active stream, invokes
finish listeners, and returns the PDFBox document for saving. `Document` implements
`AutoCloseable`: prefer try-with-resources and save the returned PDF before leaving
the block. `close()` releases the current content stream and underlying PDF without
creating pages or invoking finish listeners, including after rendering failures.
It attempts PDF cleanup even if stream cleanup fails and wraps I/O failures in
`PdfUnboxException`. Repeated closing is harmless. Rendering, page access, and
finishing after close fail with `IllegalStateException`.

Existing callers may still close the PDF returned by `finish()` themselves.
Treat finishing as the terminal step of the construction lifecycle; repeated
finishing is not specified as idempotent.

Page lifecycle extensions implement `PdfEventListener`:

- `DocumentHeader` and `DocumentFooter` draw when a page is created.
- `DocumentFinisher` appends content to each page after the page count is known,
  supporting features such as “Page x of y.” Each callback stream is closed even
  when the callback throws; the failure propagates to the caller.

## Geometry and the box model

Geometry uses PDF coordinates: the origin is at the bottom-left and Y increases
upward. `Bounds(left, top, width, height)` is anchored at its top edge:

```text
right = left + width
bottom = top - height
moveDown(distance) decreases top
```

The document derives its regions in this order:

```text
page bounds -> apply document margin -> content bounds
content bounds -> apply document padding -> body viewport
```

Document top and bottom padding reserve the header and footer regions. The
current viewport starts at the cursor and extends to the bottom of the body area.

For elements, margin is external spacing and padding is internal spacing.
`innerHeight(viewPort)` excludes margin; `outerHeight(viewPort)` adds vertical
margin. Measurements depend on available width because text wrapping changes
height. New elements should measure using the same effective width and padding
that they use when drawing.

## Element contract and pagination

Every `PdfElement` provides:

- `innerHeight(viewPort)` for measurement.
- `render(document, viewPort)` for drawing and reporting cursor advancement.
- `renderingHints()` for layout information that an element may choose to honor.

`DONT_FORWARD` is zero: return it when no further cursor movement is required
from the caller. Elements that manage cursor movement internally must avoid
reporting the same movement again.

Ordinary elements are moved to a new page when their measured inner height plus
their top margin does not fit. The bottom margin is spacing to the next element
and may be absorbed by the page end, so it is not part of the fit check. There is
no general mechanism for splitting arbitrary elements across pages. An element
taller than a full page is not automatically made to fit.

Tables deliberately use a different strategy. `AbstractTable.innerHeight()`
returns zero, and table rendering measures and advances one row at a time. It
repeats header rows after page breaks by default and applies decorations to each
page's table segment. The final returned advancement is the bottom margin; body
row advancement has already happened internally. Rows are not split into pieces.

## Fluent API and composition

Configuration methods use names such as `with(...)`, `add(...)`, `align(...)`,
and `limit(...)`. They generally mutate the receiver and return it to support
chaining. Subclasses narrow return types where needed to preserve fluent access
to their own methods.

`Unbox` is the convenience factory entry point for paragraphs, rows, columns,
stretch layouts, backgrounds, and empty elements. Prefer adding related factory
methods there while keeping constructors available for explicit configuration.

Containers compose `PdfElement` children and delegate geometry to a
`ContainerLayout` strategy:

- Horizontal layout places children side by side using relative column widths.
- Vertical layout stacks children and sums their occupied heights.
- Horizontal stretching adds padding to shorter children to match the tallest.
- Vertical stretching passes extra padding to the last child of a column.

Stretching uses mutable `RenderingHints`; individual elements may ignore them.
Do not assume rendering is a pure operation or that sharing an element between
layouts is safe without considering accumulated state.

## Decoration

Decorators implement `PdfElement` and separate visual styling from content.
There are two existing composition mechanisms:

- The default `PdfElement.with(decorator)` wraps the element and returns the
  decorator, which decorates before rendering the wrapped content.
- `AbstractDecoratable` and `Container` store decorators and return themselves,
  preserving their concrete fluent APIs.

`AbstractDecoratable` sorts decorators by level before applying them. `Container`
uses insertion order. Placement of decoration within rendering is controlled by
the owning element; tables apply their segment decorations after rendering rows.
Extensions should account for this ordering rather than assuming a universal
background-first pass.

## Tables and cell customization

`FixedColumnsTable` uses one `TableModel` across rows. `FlexTable` allows each row
to carry its own column model. Column widths are relative weights scaled to the
available table width.

Table models associate columns with titles, alignment, fonts, and optional cell
prototypes. Rows can contain explicit cells or values converted into cells. Default
selection uses an empty cell for null, a right-aligned text cell for numbers, and
a text cell for other objects, subject to column-specific configuration.

Custom cell implementations extend `AbstractTableCell` and supply measurement,
value assignment, and cell drawing. Keep row pagination in the table and local
content drawing in the cell.

`AbstractTableCell.with(Decorator)` retains the base cell type. `TextCell` and
`ContainerCell` retain their concrete types when configuring decorators, padding,
default padding, and alignment, so chains can be passed directly to `addCell(...)`
and container-cell chains can continue with `add(...)`.

## Text and lower-level drawing

`Font` combines PDFBox font access with size, color, and metrics. `TextTokenizer`
handles line breaks, whitespace-based wrapping, and splitting long words.
`TextWriter` measures and draws text with horizontal and vertical alignment,
line limits, and overflow handling. Paragraphs own one overflow policy for both
line limits and available height; see the [overflow contract](paragraph-truncation-ellipsis.md).
`VerticalParagraph` supports rotated text.

`SimpleFont` wraps the PDFBox standard 14 fonts, which use `WinAnsiEncoding`.
Measuring or drawing a character outside that encoding throws a PDFBox
`IllegalArgumentException`; the library does not substitute characters. Callers
that render arbitrary text must filter it or provide an embedded Unicode font
through the `Font` interface. See the quality audit for the planned handling.

`Canvas` exposes a PDFBox content stream and viewport for custom graphics. This
is the escape hatch for drawing that does not warrant a reusable element.
PDFBox I/O failures are generally wrapped in `PdfUnboxException` inside the library.
The exception preserves the original I/O cause and supports an optional contextual
message.

## Build and verification conventions

The root Maven project targets Java 17 (using Maven's `release` setting) and builds
the library JAR, source JAR,
and Javadoc JAR. Its direct production dependency is PDFBox, currently 2.0.37.
The `samples/` directory is a separate Maven project that depends on an installed
library artifact of the matching version.

Use `./mvnw test` for the JUnit Jupiter suite. Use `./mvnw -Dgpg.skip install` for a local
installation without signing; GPG signing is otherwise bound to `verify`.

Tests combine focused geometry and text tests with complete document regression
tests. `DocumentTest` fixes the document ID and compares output byte-for-byte with
reference PDFs in `src/test/resources`. A rendering or PDFBox change can therefore
fail tests even when the output looks equivalent.

For changes that intentionally alter generated output, inspect the resulting PDFs
visually before accepting updated references. For new behavior, add focused tests
that exercise meaningful geometry, wrapping, or pagination boundaries. Do not
treat a regenerated reference alone as evidence that a layout change is correct.

## Boundaries for future work

Preserve the small, composable API and direct PDFBox integration. Make any changes
to cursor ownership, measurement, pagination, decorator ordering, or mutable
rendering state explicit in feature specifications because they affect how
elements compose.

This initial specification does not promise arbitrary content splitting, full
HTML/CSS behavior, thread safety, or repeatable rendering of mutable elements.
Those capabilities require separate designs and validation if introduced.

Place feature and behavior specifications in `docs/specs/`. Place implementation
plans and execution steps in `docs/plans/`. Known defects and limitations are
tracked in the [quality audit](../plans/quality-audit.md).

## Source references

- [Document lifecycle](../../src/main/java/inspired/pdf/unbox/Document.java)
- [Element contract](../../src/main/java/inspired/pdf/unbox/elements/PdfElement.java)
- [Factories](../../src/main/java/inspired/pdf/unbox/Unbox.java)
- [Container composition](../../src/main/java/inspired/pdf/unbox/elements/Container.java)
- [Table rendering](../../src/main/java/inspired/pdf/unbox/elements/internal/AbstractTable.java)
- [Document regression tests](../../src/test/java/inspired/pdf/unbox/DocumentTest.java)
- [Build configuration](../../pom.xml)
