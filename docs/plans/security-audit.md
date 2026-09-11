# Security and supply-chain audit

Open security, dependency, and build-pipeline tasks. Remove findings when resolved.
Rendering fixes are tracked in the [quality audit](quality-audit.md); release
configuration is documented in [release publishing](../specs/release-publishing.md).

### S5. Caller-supplied text can abort or exhaust generation

Priority: Medium. Denial of service when content is user controlled.

Two quality findings have a security angle:

- Quality finding 6: characters outside `WinAnsiEncoding` throw an unchecked
  PDFBox exception. An application that renders end-user text without filtering
  fails on the first unsupported character.
- Quality finding 2: a table header whose text wraps to more lines than fit on a
  page causes unbounded page creation and `StackOverflowError`.

Applications should validate or sanitize end-user text until the library offers
a replacement strategy and a bounded header failure.

### Dependency update automation

Priority: Low.

Configure Dependabot for the `github-actions` and `maven` ecosystems so dependency
and workflow updates arrive as pull requests.

### PDFBox migration planning

Priority: Low.

Assess migration to PDFBox 3.x, including changes to `PDType1Font` constants,
document loading, and PDF regression output.

### Sample dependency version expression

Priority: Low.

Replace `${version}` with `${project.version}` in `samples/pom.xml` to remove the
Maven warning about the deprecated expression.

### Local signing configuration

Priority: Low.

Migrate local signing from `settings.xml` passphrase properties to the environment,
then enable the GPG plugin's `bestPractices` option.
