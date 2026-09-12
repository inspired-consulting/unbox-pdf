# Changelog

Changes grouped by release, newest first.

## Unreleased — 0.11.0-SNAPSHOT

No changes yet.

## 0.10.0 — 2026-09-12

Changes after 0.9.3, primarily from 2026-09-10 through 2026-09-12.

### Added

- Styled paragraph text runs: `Paragraph.add(String, Font)` mixes fonts, sizes,
  and colors while wrapping runs together and sharing a baseline. Added `TextRun`,
  `add(String)` for the paragraph's base font, and `font()` and `runs()` accessors.
- `Overflow.ELLIPSIS` marks text omitted by a paragraph line limit or fixed height.
  Paragraphs now use one explicit policy: `CLIP` (default), `ELLIPSIS`, or `OVERFLOW`.
- TrueType font loading through `Document.loadFont(Path)` and
  `Document.loadFont(InputStream)`, returning a document-owned `FontFace` with
  `at(size)` and `at(size, color)` methods.
- `SimpleFont` replaces unsupported characters with `?` by default. Configure
  replacement with `withReplacement(...)`, or pass `null` to fail explicitly.
  Added `Font.encodable(String)` so measurement and drawing use the same text;
  custom `Font` implementations retain unchanged text by default.
- `Document` implements `AutoCloseable` for try-with-resources. Added
  `finishTo(OutputStream)`, `finishTo(Path)`, and `finishToBytes()` to save and
  close in one call; PDFBox also closes caller-provided output streams.
- Rounded borders and backgrounds, including `Border.withRadius(...)`.
  Rounded borders require uniform thickness; existing square-border constructors
  remain available.
- Independent table row and column strokes through `withRowStroke(...)` and
  `withColumnStroke(...)`. `with(Stroke)` continues to set both axes.
- Fluent table-cell styling preserves `TextCell` and `ContainerCell` return types
  for alignment, padding, default padding, and decorators.
- `Font.capHeight()`, `Paragraph.padding()`, and `TextWriter.baseline(...)` for
  aligning custom drawings with text.
- Samples for flexible table reports and drawing shapes alongside text, plus
  corrections to the stretching-columns sample.

### Fixed

- Page-fit calculations now exclude bottom margin and padding, keeping content
  that fits a page out of the footer area. Querying space before rendering creates
  the first page on demand.
- Table pagination keeps headers with the first body row when they fit together
  on a fresh page. Initial headers are retained when the table moves to a new page
  with `repeatHeader(false)`, including tables containing only headers.
  Oversized repeated headers
  raise a clear `PdfUnboxException` instead of recursing into a stack overflow;
  oversized body rows do not trigger repeated page breaks when a new page would
  provide no additional space.
- Paragraph measurement accounts for horizontal margins, and rendering no longer
  counts vertical margins twice.
- Containers and container cells measure children at the effective rendering
  width after margins and padding. Vertical stretch rendering uses consistent
  viewport bounds.
- Stretch padding is reset between renders, preventing accumulated spacing when
  elements are reused. Empty vertical stretch containers render safely.
- `FlexTable` applies horizontal margins consistently and grows implicit column
  models when rows contain additional values.
- Adding text cells beyond a table model's declared columns no longer indexes
  past the model.
- Text-cell decorators render once instead of being applied by both
  `AbstractTableCell.render()` and `TextCell.renderCell()`.
- Column widths reject negative, infinite, and NaN values. Scaling empty column
  models is safe; nonempty models must have a positive total width to scale.
- Nonblank text with a nonpositive or NaN layout width fails with a descriptive
  exception. Blank styled runs produce no lines, and `VerticalParagraph` explicitly
  rejects appended runs because it renders a single rotated run.
- Document and finisher content streams are cleaned up on failure, and wrapped
  PDF I/O exceptions retain their original causes.
- `TableModel.DEFAULT_COLUMN` is now final.

### API migration

- Replace `withOverflow(false)` with `with(Overflow.CLIP)` and
  `withOverflow(true)` with `with(Overflow.OVERFLOW)`.
- If using the intermediate snapshot API `limit(n, mode)`, use
  `limit(n).with(mode)` instead. `OVERFLOW` now bypasses both height clipping and
  line truncation, while reserving only the paragraph's allocated layout height.
- Direct `TextWriter` users must pass the overflow mode to `write(...)`;
  mutable overflow setters have been removed. See the
  [overflow specification](docs/specs/paragraph-truncation-ellipsis.md).
- `ColumnModel.scale(...)` and `scaleToSize(...)` now return `ColumnModel<C>`;
  concrete implementations retain their concrete return types. Custom
  implementations should update the former method-level generic signatures.

### Build, tests, and documentation

- Updated PDFBox from 2.0.30 to 2.0.37.
- Added the Maven 3.9.16 Wrapper with distribution checksum verification.
- CI tests JDK 17 and 21; publishing uses JDK 21 with Java 17-compatible artifacts
  enforced through `maven.compiler.release`.
- Migrated release publishing from OSSRH staging to the Maven Central Portal,
  updated GPG signing configuration, and documented snapshot/release publishing.
- Added weekly Dependabot checks for Maven dependencies and GitHub Actions.
- Expanded regression coverage for text, fonts, geometry, layout, pagination,
  table styling, document lifecycle, and output helpers.
- Updated README examples and build instructions; added library and overflow
  specifications, quality audits, implementation plans, and shared contributor
  guidance for Codex and Claude Code.

## 0.9.3 — 2024-02-15

- Replaced boolean overflow constructors with fluent
  `Paragraph.withOverflow(boolean)` configuration.
- `Paragraph.limit(n)` now rejects nonpositive values rather than ignoring them.
- Added a line-height rounding tolerance to avoid clipping text that fits.
- Added `Padding.add(Padding)` and consolidated stretch-padding addition.
- Cleaned up generic types, formatting, Javadoc, and examples.

## 0.9.2 — 2024-02-15

- Paragraphs honor explicit newline characters while wrapping text.
- Text drawing stops when complete lines no longer fit the available height;
  added boolean overflow constructors to allow drawing beyond those bounds.
- Empty text returns zero drawn height.
- Added a multiline paragraph sample and shared `.editorconfig` settings.

## 0.9.1 — 2024-02-14

- Updated library and sample release versions; no library behavior changes
  relative to 0.9.0.

## 0.9.0 — 2024-02-13

- Added `VerticalParagraph` for rotated text, with a vertical-text sample.
- `ContainerCell` accepts an existing `Container`, allowing custom container
  configuration inside table cells.
- Updated PDFBox from 2.0.26 through 2.0.28 to 2.0.30.
- Updated JUnit to 5.9.3 and Maven compiler, resources, test, source, Javadoc,
  and signing plugins; improved code style and documentation.

## 0.8.0 — 2023-06-07

- Added table- and row-wide default cell padding, with individual cell overrides,
  and a `TableRow.addCell(String, Font)` convenience overload.
- Text-cell measurement and positioning account for configured padding.
- Fixed padding in container cells and updated affected PDF regression references.
- API migration: replace table-cell `withPadding(Padding)` with `with(Padding)`.
  Custom subclasses should use `padding()` instead of the formerly protected
  `padding` field. Direct `TableCell` implementations must provide
  `withDefaultPadding(Padding)`.

## 0.7.0 — 2023-04-24

- Added `Paragraph.limit(int)` to cap the allocated number of text lines.
- Added decorator levels to control drawing order for elements using
  `AbstractDecoratable`.
- Long words are split when they exceed the available text width.
- Expanded API documentation and standardized formatting.
