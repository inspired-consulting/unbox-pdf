# Quality and maintenance audit

Open rendering, layout, and maintenance tasks. Remove findings when resolved; keep behavior
contracts in [library principles](../specs/library-principles.md) and related
specifications.

## Open findings

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
  elements draw past the footer. An oversized body row behaves the same way, and
  so does an oversized header when header repetition is disabled. These are
  documented limitations, not crashes.
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
