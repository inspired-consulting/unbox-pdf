# Rendering and layout quality audit

Status: Planned. Initial findings were reproduced during repository review;
the three fixes below remain open.

## Objective

Prevent silent content loss, document-generation crashes, and incorrect page
placement. Fix confirmed issues in small, independently reviewable changes and
extend coverage around the boundaries that existing PDF references do not test.

Read [library principles](../specs/library-principles.md) before changing rendering
contracts. Keep this work separate from the [Maven Wrapper setup](maven-wrapper.md).

## Confirmed open findings

### 1. Paragraph measurement ignores horizontal margins

Priority: High — silent text loss.

`Paragraph.innerHeight()` measures text using the viewport width minus padding,
while rendering also subtracts horizontal margins. Text therefore wraps into more
lines than the allocated height permits, and trailing content can disappear.

Reproduction: render a default paragraph containing `"word ".repeat(30) + "END"`
with `Margin.left(450)` on a default document. During review, only ten words were
extracted from the resulting document and `END` was missing.

Implementation approach:

- Add a regression test that checks the complete text survives rendering with
  horizontal margins when no line limit or fixed height requests truncation.
- Make measurement and rendering agree on the effective text width; check callers
  to avoid subtracting margins twice.
- Cover left and right margins, padding, and paragraphs in containers. Preserve
  intentional line limits and fixed-height clipping.
- Review resulting height and page-break changes, including reference PDFs.

Completion: text is retained within the intended bounds and subsequent elements
are placed after the full paragraph height.

### 2. Oversized table headers recursively create pages

Priority: High — document-generation crash.

`AbstractTable.checkPageBreak()` repeats headers after creating a page. If a header
cannot fit on a fresh page, rendering it triggers another page break and another
header repetition without a terminating condition.

Reproduction: a table header containing a custom cell whose `innerHeight()` returns
2,000 points causes `StackOverflowError`. A review probe created 243 pages before
failing; the exact count depends on the available stack and is not a test oracle.

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

### 3. Document page-fit checks exclude element margins

Priority: Medium — content can extend into the footer region.

`Document.render()` checks `innerHeight()` against the remaining page space, but
ordinary element rendering also consumes vertical margins.

Reproduction: leave 20 points in the body viewport, then render a default paragraph
with `Margin.top(30)`. During review it stayed on the same page and left roughly
minus 23 points of remaining body space.

Implementation approach:

- Add boundary tests for top and bottom margins, exact fits, and elements that
  fit only after advancing to a new page.
- Include occupied vertical margins in ordinary element fit checks.
- Preserve tables' internal row advancement and pagination; do not apply a generic
  height change without checking that contract.
- Cover containers and header/footer reservations, and inspect pagination changes.

Completion: an ordinary element that fits on a fresh page is moved there when its
total occupied height exceeds the current body space.

## Follow-up audit areas

These are investigation targets, not additional confirmed findings:

- Consistency of measured and rendered widths/heights in horizontal and vertical
  containers, especially with nested margins and padding.
- Empty stretch containers and reuse of elements with mutable rendering hints.
- Table cell prototypes, padding propagation, and decorator ordering.
- Text wrapping at very narrow widths and preservation of explicit line breaks.
- Oversized ordinary elements and table rows: distinguish documented limitations
  from unintended clipping, crashes, or cursor corruption.

For each new finding, record a minimal reproduction, expected and actual behavior,
impact, and source location before scheduling a fix. Keep broader refactoring out
of individual bug fixes unless it is needed to correct the behavior.

## Already completed

- Corrected the `TableRow.addCell()` model-boundary comparison from `>=` to `>`.
- Corrected `TextWriter.withOverflow(boolean)` to honor its argument.
- Added six regression tests for those fixes. All 22 tests passed after the fixes,
  including the existing byte-exact PDF comparisons. This is historical validation,
  not a substitute for running checks after future changes.

## Execution and validation

1. Reproduce each open finding against the current revision and add focused
   regression coverage before its fix.
2. Address each confirmed bug in a separate reviewable change, starting with the
   high-priority findings. Document expected layout changes and API implications.
3. Run `mvn test` after each Java change; use `./mvnw test` once the wrapper plan
   has been implemented.
4. Visually inspect PDFs when positions, wrapping, or pagination change. Update
   reference PDFs only after confirming the new output is intentional.
5. Update the library specification where behavior or supported boundaries change,
   and mark findings completed here with their validation results.

The initial audit is complete when the three confirmed findings are resolved,
follow-up areas have been assessed with remaining issues recorded, and relevant
regression tests and documentation reflect the supported behavior.
