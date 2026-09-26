# Quality and maintenance audit

Open rendering, layout, and maintenance tasks. Remove findings when resolved; keep behavior
contracts in [library principles](../specs/library-principles.md) and related
specifications.

## Open findings

## Follow-up tasks

## Dependency and build maintenance

### PDFBox migration planning

Priority: Low.

Assess migration to PDFBox 3.x, including changes to `PDType1Font` constants,
document loading, and PDF regression output.

## Validation for fixes

Reproduce each finding and add focused regression coverage. Run `./mvnw test`
after Java changes and visually inspect intentional PDF changes before updating
references. Update the relevant specification and remove the resolved task here.
