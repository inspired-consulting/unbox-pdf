# Test plan: paragraph truncation ellipsis

Historical test plan for the original API. The implemented unified API and current
coverage are documented in [the overflow contract](paragraph-truncation-ellipsis.md).

Status: Proposed. Covers testing for
[paragraph-truncation-ellipsis.md](paragraph-truncation-ellipsis.md), which
tracks [issue #16](https://github.com/inspired-consulting/unbox-pdf/issues/16).

This plan expands the spec's [Testing](paragraph-truncation-ellipsis.md#testing)
section into individually checkable test cases. It does not implement the
`Overflow.ELLIPSIS` feature; write these tests once `Paragraph.limit(int,
Overflow)` and the `TextWriter` changes described in the spec exist.

## Scope

- Unit-level coverage of `withEllipsis(line, maxWidth)` and the truncation
  branch in `TextWriter.write(..., lineLimit, overflow)`, using
  `SimpleFont.helvetica(n)` for deterministic widths, following the pattern in
  `TextWriterTest` and `TextTokenizerTest`.
- No `DocumentTest` byte-for-byte reference is added for this feature (per the
  spec's non-goals and testing guidance) unless a reviewer explicitly asks for
  one.

## Test cases

### TC1 — Truncating line limit appends an ellipsis

- Setup: text that wraps to more lines than `lineLimit` when rendered with
  `Overflow.ELLIPSIS`.
- Expect: the last rendered line ends with `…`, and
  `font.width(lastLine) <= bounds.width()`.
- Guards: spec Goals — "ellipsis inside `bounds.width()`".

### TC2 — Non-truncating line limit leaves output unchanged

- Setup: `lineLimit >= wrapped line count` (e.g. text that already fits within
  the limit) with `Overflow.ELLIPSIS` set.
- Expect: no `…` is appended anywhere; output is identical to the same input
  with `Overflow.CLIP`.
- Guards: spec Behavior — "the ellipsis is a truncation marker, not a
  decoration applied whenever `limit(...)` is set".

### TC3 — Truncation mid-word trims further than a plain clip

- Setup: text containing a single token wider than `maxWidth`, forcing
  `TextTokenizer.breakUp()` to split it, with `lineLimit` cutting inside that
  split token and `Overflow.ELLIPSIS` set.
- Expect: the last line still ends with `…`, still fits `maxWidth`, and is
  strictly shorter than the corresponding `Overflow.CLIP` cut of the same
  input (i.e. trailing characters were dropped to make room for `…`).
- Guards: spec Behavior step 2 — drop trailing characters one at a time,
  trimming a trailing space left behind, until the appended form fits.

### TC4 — Default `limit(n)` keeps today's output byte-for-byte

- Setup: any text/width combination already covered by existing
  `TextWriterTest`/`TextTokenizerTest`/`DocumentTest` cases, called via
  `limit(n)` (no `Overflow` argument).
- Expect: output is bit-for-bit identical to pre-feature behavior; no `…` is
  ever appended.
- Guards: spec Goals — "Preserve current output ... for every `Paragraph` that
  does not opt in" and Interaction — "no `DocumentTest` reference PDFs need
  regeneration for this change alone".

### TC5 — Pathological width: bare ellipsis still doesn't fit

- Setup: `maxWidth` narrower than `font.width("…")`.
- Expect: `withEllipsis(...)` returns `"…"` rather than an empty string (bounds
  may be exceeded, content is not silently dropped).
- Guards: spec Behavior step 3, consistent with existing `withOverflow`/
  `enoughSpace` best-effort behavior.

### TC6 — Ellipsis fits without trimming

- Setup: last kept line short enough that `font.width(line + "…") <= maxWidth`
  without dropping any characters.
- Expect: `withEllipsis(...)` returns `line + "…"` unchanged (step 1 of the
  algorithm, no trimming loop entered).
- Guards: spec Behavior step 1; complements TC3, which exercises the trimming
  loop.

### TC7 — Only the last kept line is touched

- Setup: `lineLimit` truncating a wrap of 3+ lines with `Overflow.ELLIPSIS`.
- Expect: all lines before the last kept one are unchanged from the
  `Overflow.CLIP` output; only the final line differs.
- Guards: spec Behavior — "Only the last *kept* line is touched."

## Out of scope for this plan

- `VerticalParagraph` (excluded by the spec's non-goals).
- A font-glyph fallback from `…` to `...` (explicit non-goal; follow-up only).
- `calculateHeight(...)` behavior changes (explicit non-goal — no test needed
  beyond confirming, if convenient, that line count is unaffected).

## Optional: visual sample

If useful for reviewer sign-off, add a `samples/` example rendering a table or
paragraph list with `limit(1, Overflow.ELLIPSIS)` next to `limit(1)` for
side-by-side comparison, per the spec's testing guidance. This is not a
`DocumentTest` reference and does not require a plan of its own.
