# Repository guidance

These instructions apply to both Codex and Claude Code. Maintain shared guidance
here; the root `CLAUDE.md` imports this file.

## Project and references

Unbox PDF is a Java 17 library built with Maven on Apache PDFBox 2.x, published as
`consulting.inspired:unbox-pdf`. It provides a fluent API for top-to-bottom PDF
creation. There is no CLI or application server.

- Read [library principles](docs/specs/library-principles.md) before changing
  rendering, layout, pagination, or element contracts.
- Production code lives in `src/main/java/inspired/pdf/unbox/`; tests and reference
  PDFs live in `src/test/java/` and `src/test/resources/`.
- `samples/` contains executable Java examples in a separate Maven project.
- Keep README examples aligned with current source signatures when changing the API.

## Build and test commands

Run these from the repository root:

```bash
./mvnw test                                    # all tests
./mvnw -Dtest=TextTokenizerTest test           # one test class
./mvnw '-Dtest=DocumentTest                    #createTable' test     # one test method
./mvnw package                                 # build JAR, sources, and Javadoc
./mvnw -Dgpg.skip install                      # test and install for local samples
```

The POM binds GPG signing to `verify`. Use `-Dgpg.skip` for local `verify` or
`install` when no signing key is configured. `test` and `package` do not reach
that phase. Use the checked-in Maven Wrapper, which pins Maven 3.9.16, and a JDK
compatible with the Java 17 target. CI validates with JDK 21 while the published
artifact remains Java 17-compatible. On Windows use `mvnw.cmd` in place of `./mvnw`.

Install the library before building `samples/`; it depends on the installed
artifact of the matching version and is not a root reactor module. Run a sample's
`main` method, such as `samples.SimplePdf`, to generate PDFs in `samples/out/`.
Create that directory first and use the repository root as the working directory.

## Change and validation workflow

- Keep changes focused on the requested behavior and preserve unrelated work.
- Ask before adding any new production or test dependency.
- Follow existing architecture and naming conventions.
- Follow `.editorconfig`: four-space indentation, LF, UTF-8, and a final newline.
- Preserve Java 17 compatibility and existing public APIs unless the task calls
  for an API change.
- Add focused regression tests for bug fixes and meaningful tests for new behavior.
- Run `./mvnw test` after Java changes. Report any checks that could not run and why.
  Documentation-only changes need link and consistency checks, not a Java build.
- Do not manually edit or commit generated build output. Regenerate it through
  the build. Update PDF test references only for intentional output changes,
  following the validation guidance below.
- Summarize behavior changes and validation results for the reviewer.

## PDF regression tests

`DocumentTest` fixes the document ID with `pdf.setDocumentId(1L)` and compares saved
PDFs byte-for-byte against `src/test/resources/` using `Files.mismatch`. Changes to
rendering or PDFBox may fail these tests even when output looks equivalent.

- Investigate mismatches before replacing references.
- Update reference PDFs only for intentional output changes; visually inspect the
  generated PDFs before accepting replacements and describe the expected change.
- The commented `Files.copy` line in `assertDocumentMatchesReference` can regenerate
  references. If used temporarily, restore it to a comment before finishing.
- Keep generated build output in `target/` and sample output in `samples/out/`;
  these directories are ignored by Git.

## Implementation conventions and pitfalls

- Use fluent methods such as `with(...)`, `align(...)`, and `limit(...)`; narrow
  return types in subclasses to preserve fluent access. Add convenience element
  factories to `Unbox` where appropriate.
- Keep document lifecycle, layout, content drawing, and decoration separate. Prefer
  the existing `PdfElement`, `ContainerLayout`, decorator, and table-cell extension
  points.
- PDF Y coordinates increase upward. `Bounds` is anchored at its top edge;
  `bottom() = top - height`, and moving down decreases `top`.
- Measure and draw with consistent effective widths. `innerHeight` excludes margin;
  `outerHeight` adds vertical margin. Rendering returns remaining cursor advancement.
- Tables advance rows internally and return the bottom margin. Do not advance the
  same content twice or assume tables report their full height before rendering.
- `AbstractDecoratable` sorts decorators by level; `Container` uses insertion order.
- Stretch layouts mutate rendering hints. Horizontal stretching pads shorter
  children; vertical stretching passes extra padding to the last child. Do not
  assume element rendering is stateless.
- The caller saves and closes the `PDDocument` returned by `Document.finish()`.

## Specifications and plans

- Keep architecture and behavioral specifications in `docs/specs/` and update the
  relevant specification when changing a documented contract.
- Use `docs/plans/` for implementation steps for substantial changes. Small,
  localized fixes do not require a separate plan.
- Keep detailed architecture in the specification rather than duplicating it here.
  Keep this file concise and update it when commands or working conventions change.
