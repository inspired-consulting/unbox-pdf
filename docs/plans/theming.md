# Theming

Status: idea, not specified.

## Goal

Let users define the design of a document in one place. Today they define many
constants for fonts, colours, paddings, strokes, and margins, and pass them to
each element. A theme should hold design tokens and settings, and derive or
cascade values where possible.

## Current state

- `UnboxTheme` is a class with public static, non-final colour constants
  (a palette and a gray scale). It holds no fonts, sizes, or spacing.
- Defaults are hard-coded in several places, for example the table stroke
  (`GRAY_700`, 0.4 pt) in `AbstractTable` and `Stroke`, and
  `TableCell.DEFAULT_CELL_PADDING`.
- Elements receive fonts, colours, and spacing explicitly at construction or
  through fluent `with(...)` calls.

- Elements do not know their parent. `Container.add` keeps a child list,
  tables create rows and cells internally, and decorators wrap an element.
- `PdfElement.innerHeight(Bounds)` has no `Document` parameter; only
  `render(Document, Bounds)` has one. `Document.render` measures an element
  before rendering it, so a theme must be available during measurement.
- Values are fixed at construction. `Paragraph` stores a final `font` and
  creates its `TextWriter` in the constructor; `new Paragraph(text)` uses
  `helvetica(8)`.

## Requirements so far

- First version: colours and fonts, plus spacing, strokes, and borders.
  Component defaults, such as table header style or paragraph style, may follow later.
- A theme can be set on the `Document` and on individual elements, at least
  tables and paragraphs. An element uses its own theme if set, otherwise the
  theme inherited from its parents or the document.

## Scope ideas

- A `Theme` object holding global tokens: colours, font faces and sizes,
  spacing, strokes, and borders.
- Component themes such as `TableTheme` or `ParagraphTheme`. `Theme` provides
  their defaults; an element can override them, for example
  `table.with(tableTheme)`.
- Derived values, for example heading sizes from a base size and scale, or
  muted text colour from the text colour.
- Cascading, for example document theme → table → header row, so a user
  overrides one value without redefining the rest.
- Element defaults that come from the active theme instead of hard-coded
  constants, while explicit `with(...)` values still win.

## Theme resolution

Two options were considered for giving nested elements access to the theme.

**A) Parent links (preferred).** `Container.add`, `TableRow.addCell`,
`Decorator.wrap`, and document headers and footers set the child's parent.
`Document.render(element)` makes the document the parent of the root element.
An element resolves its theme from itself, then its parents, then the document,
then a built-in default.

- Keeps the `render` and `innerHeight` signatures. Measurement works because an
  element can walk up the tree itself.
- An element can have only one parent. Today an element may be added to several
  parents; the rule could be "last parent wins" or adding it again fails.
- Decorators become part of the parent chain.
- `PdfElement` needs `parent()` and `setParent(...)`. Default methods are needed
  for user classes that implement `PdfElement` directly.

**B) Context passed down.** Add a context parameter to `innerHeight` as well, or
keep a theme stack in `Document` during rendering. There is no problem with shared
elements, but it breaks the public `PdfElement` interface or depends on mutable
shared state. `innerHeight` is also called where no document is available.

### Consequence for existing elements

Fonts and other themed values must be optional at construction and resolved at
measurement or rendering time. `paragraph("text")` uses the theme font;
`paragraph("text", font)` keeps the explicit font. Without a theme, the built-in
default must match today's values, such as `helvetica(8)`, so reference PDFs stay
byte-identical. This affects at least `Paragraph`, `TextCell`, `TableRow.addCell`,
and the table stroke and padding defaults. This refactoring is likely larger than
the theme classes themselves.

## Open questions

- **Typed or string keys.** Which tokens are fixed, typed properties (for
  example `theme.textColor()`, `theme.spacing().medium()`), and which need
  flexible string keys (for example `"brand.accent"`) that users can add?
  Typed properties give compile-time safety and IDE support; string keys allow
  user-defined tokens but fail only at runtime. A mix is possible: typed core
  tokens plus a map for custom ones.
- Confirm parent links (option A) and the rule for elements added to several
  parents.
- Which elements besides tables and paragraphs accept their own theme?
- Is a theme immutable with a builder, or mutable like other fluent objects?
- How do fonts fit in, given that `FontFace` loaded with `document.loadFont`
  is valid for one document only?
- What happens to `UnboxTheme` and its public static fields? Existing users
  import them, so removal needs a migration path.
- Charts ([charts plan](charts.md)) will need series colour palettes from the theme.

## Out of scope

- CSS parsing or external style files.
