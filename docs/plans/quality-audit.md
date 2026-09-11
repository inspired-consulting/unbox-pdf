# Quality and maintenance audit

Open rendering, layout, and maintenance tasks. Remove findings when resolved; keep behavior
contracts in [library principles](../specs/library-principles.md) and related
specifications.

## Open findings

### 2. Oversized table headers recursively create pages

Priority: High. Document-generation crash. Re-confirmed on 2026-09-11.

`AbstractTable.checkPageBreak()` repeats headers after creating a page. If a header
cannot fit on a fresh page, rendering it triggers another page break and another
header repetition without a terminating condition.

Reproduction: a table header containing a custom cell whose `innerHeight()` returns
2,000 points causes `StackOverflowError`. The same happens with an ordinary
`TextCell` header whose text wraps to more lines than fit on a page, so
caller-supplied text can trigger this crash. The number of pages created before
the failure depends on the available stack and is not a test oracle.

Implementation approach:

- Define the failure behavior for headers that cannot fit. Prefer a clear,
  bounded exception as the initial containment fix; splitting headers requires
  a separate design.
- Prevent recursive header repetition during header rendering and detect an
  oversized complete header group, including multiple individually fitting rows.
- Add bounded regression cases for oversized headers, ordinary repeated headers,
  and a table starting near a page boundary. Avoid tests that depend on exhausting
  the stack or creating hundreds of pages.
- Check remaining body space after header repetition and document the supported
  behavior for an oversized body row.

Completion: invalid header geometry fails clearly without unbounded page creation,
and ordinary tables continue to repeat headers correctly.

### 6. Text outside WinAnsiEncoding aborts document generation

Priority: Medium. Crash caused by ordinary caller data.

All default fonts are PDFBox standard 14 fonts with `WinAnsiEncoding`.
`Font.width()` and `showText()` throw `IllegalArgumentException` for any
character that the encoding does not contain. The exception is a PDFBox
exception, not a `PdfUnboxException`, and it surfaces from `innerHeight()`
before anything is drawn.

Reproduction: `document.render(Unbox.paragraph("Ā 中文 😀"))` throws
`IllegalArgumentException: U+0100 ('Amacron') is not available in the font
Helvetica, encoding: WinAnsiEncoding`. A `TextCell` with an emoji fails the same
way.

Implementation approach:

- Consider a configurable replacement strategy in `TextTokenizer` or `Font`
  (for example, replace unsupported characters with `?`) so that generation
  does not abort on user-supplied text. Embedding a Unicode TrueType font is a
  separate feature.
- Add tests that cover a supported non-ASCII character such as `ä` and an
  unsupported one.

### 8. Table cell decorators are drawn twice

Priority: Low. Output bloat; visible for semi-transparent colors or borders.

`AbstractTableCell.render()` applies the cell decorators and then calls
`renderCell()`. `TextCell.renderCell()` applies them again.

Reproduction: a `TextCell` with one `BackgroundDecorator` emits two filled
rectangles in the page content stream.

Implementation approach: remove the second call in `TextCell`. This changes the
bytes of generated PDFs, so the reference PDFs of `DocumentTest` must be
regenerated and inspected.

### 9. Measurement and rendering widths differ in containers and cells

Priority: Medium. Same defect class as finding 1. Confirmed by code reading; no
runtime reproduction has been recorded yet.

- `HorizontalLayout.innerHeight()` applies only the container padding, while
  `render()` applies margin and padding. A container with horizontal margin
  measures its children too wide.
- `VerticalLayout.innerHeight()` passes the unmodified viewport to the children,
  ignoring container margin and padding.
- `ContainerCell.innerHeight()` measures the inner container at the full cell
  width, while `renderCell()` renders it with the cell padding applied.

Implementation approach: fix together with finding 1 using one helper that
derives the effective content bounds, and add tests for each combination.

## Follow-up tasks

- `TableModel` default cells and column cell prototypes are shared mutable
  objects. `TableRow.innerHeight()` sets the table's default padding on them
  permanently, so a model reused by tables with different cell paddings keeps
  the padding of the first table. `TableModel.DEFAULT_COLUMN` is a public
  static field that is not final.
- Negative viewport widths from oversized margins are accepted silently. The
  tokenizer does not loop on them; text is split into single characters and then
  clipped. Validation would give a clearer failure.
- An ordinary element taller than one page is moved to a fresh page and then
  rendered overflowing. Paragraphs clip lines unless overflow is enabled; other
  elements draw past the footer. An oversized body row behaves the same way.
  These are documented limitations, not crashes.
- `TextCell.innerHeight()` adds a two-point correction that `renderCell()` does
  not return. Rows are therefore slightly taller than the reported cell height.

## Dependency and build maintenance

### Dependency update automation

Priority: Low.

Configure Dependabot for the `github-actions` and `maven` ecosystems so dependency
and workflow updates arrive as pull requests.

### PDFBox migration planning

Priority: Low.

Assess migration to PDFBox 3.x, including changes to `PDType1Font` constants,
document loading, and PDF regression output.

## Validation for fixes

Reproduce each finding and add focused regression coverage. Run `./mvnw test`
after Java changes and visually inspect intentional PDF changes before updating
references. Update the relevant specification and remove the resolved task here.
