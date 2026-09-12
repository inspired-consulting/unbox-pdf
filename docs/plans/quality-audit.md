# Quality and maintenance audit

Open rendering, layout, and maintenance tasks. Remove findings when resolved; keep behavior
contracts in [library principles](../specs/library-principles.md) and related
specifications.

## Open findings

## Follow-up tasks

- `TableModel` default cells and column cell prototypes are shared mutable
  objects. `TableRow.innerHeight()` sets the table's default padding on them
  permanently, so a model reused by tables with different cell paddings keeps
  the padding of the first table.
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
