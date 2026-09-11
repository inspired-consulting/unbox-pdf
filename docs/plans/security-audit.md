# Security and supply-chain audit

Status: Completed on 2026-09-11 against commit `4cb41e6`. No fix has been
implemented yet. Rendering defects are tracked in the
[quality audit](quality-audit.md).

## Scope and threat model

Unbox PDF is a library without network access, file access, configuration
parsing, or deserialization. Its inputs are strings, numbers, and objects supplied
by the calling application; its output is a PDFBox `PDDocument` that the caller
saves. The relevant risks are therefore:

- Crashes or unbounded resource use triggered by caller-supplied content, which
  becomes a denial-of-service vector when that content comes from end users.
- Vulnerable or unmaintained dependencies shipped transitively to consumers.
- Integrity of the build and release pipeline.

Content injection into the PDF is not a risk: PDFBox escapes string operands when
it writes content streams, and the library never builds PDF syntax from text.

## Findings

### S1. Publishing target no longer exists

Priority: High. Releases cannot be published.

`pom.xml` deploys to `https://s01.oss.sonatype.org` with the
`nexus-staging-maven-plugin`. Sonatype shut down OSSRH on 30 June 2025. The
documented migration is the `central-publishing-maven-plugin` with a Central
Portal user token, or the compatibility endpoint
`https://ossrh-staging-api.central.sonatype.com` as an interim step. The
`OSSRH_USERNAME` and `OSSRH_TOKEN` secrets must be replaced by a Portal token.

### S2. Release workflow cannot sign artifacts

Priority: High. Same effect as S1.

`.github/workflows/maven-publish.yml` runs `./mvnw --batch-mode deploy`, which
reaches the `verify` phase and the bound `maven-gpg-plugin`, but the workflow
imports no GPG key (no `gpg-private-key` input on `setup-java` and no `gpg`
step). Signing fails before deployment. Store the key and passphrase as
repository secrets and pass them to `setup-java`, or sign through the Central
Portal plugin's own mechanism.

### S3. GitHub Actions on deprecated majors and unpinned

Priority: Medium. Supply-chain hygiene.

The workflow uses `actions/checkout@v3` and `actions/setup-java@v3`. Both run on
Node.js 16, which GitHub has deprecated; the current majors are considerably
newer. Actions are referenced by mutable major tags instead of commit SHAs. The
job also requests `packages: write`, which it does not use.

Recommendation: move to the current majors, pin each action to a commit SHA with
a version comment, and reduce permissions to `contents: read`. Consider
Dependabot for `github-actions` and `maven` ecosystems so that action and
dependency updates arrive as pull requests.

### S4. No continuous test workflow

Priority: Medium. Regressions and vulnerable dependency updates are not caught
before release.

The only workflow triggers on release creation. Add a workflow that runs
`./mvnw --batch-mode test` on pushes and pull requests with JDK 21 and, since the
published artifact must stay Java 17 compatible, optionally a second job on
JDK 17.

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

### S6. Dependency status

Priority: Low. No known vulnerability affects the shipped artifact.

| Dependency | Version | Scope | Assessment |
| --- | --- | --- | --- |
| `org.apache.pdfbox:pdfbox` | 2.0.37 | compile | Latest 2.0.x release. CVE-2026-23907 and CVE-2026-33929 affect only the separate PDFBox examples module, which is not a dependency. |
| `org.apache.pdfbox:fontbox` | 2.0.37 | compile (transitive) | Matches PDFBox. |
| `commons-logging:commons-logging` | 1.2 | compile (transitive) | Released 2014; no known CVE. Newer 1.3.x exists; consumers may manage the version. |
| `org.junit.jupiter:junit-jupiter-engine` | 5.9.3 | test | Not shipped. Newer 5.x releases exist. |

PDFBox 3.x is the actively developed line. Staying on 2.0.x is acceptable while
2.0.x receives releases, but the migration should be planned; the API changes
affect `PDType1Font` constants and `PDDocument` loading.

### S7. Minor build metadata issues

Priority: Low.

- `samples/pom.xml` references the library with `${version}`. Maven warns about
  this deprecated expression; use `${project.version}`.
- `pom.xml` `<scm><url>` points to `tree/master`, but the default branch is
  `main`. The license URL uses `http://`.
- The Maven Wrapper pins Maven 3.9.16 with a SHA-256 checksum, and all plugin
  versions are pinned. This is good practice and should be kept.

## Checks performed

- Repository search for credentials, tokens, and private keys: none found. The
  workflow reads secrets only through `${{ secrets.* }}`.
- `git ls-files`: no build output, sample output, or `.DS_Store` files are
  tracked; `.gitignore` covers them.
- Dependency tree via `./mvnw dependency:tree`: four artifacts in total, listed
  above.
- Probe program for caller-supplied text (finding S5).
- Manual review of `Document`, `DocumentFinisher`, and the content-stream
  handling for resource leaks: streams are closed on the normal path; on an
  exception the caller must close `Document.getDocument()`.

## Recommended order

1. S1 and S2 together, because the next release depends on them. Verify with a
   dry run against the Central Portal before tagging.
2. S3 and S4 in one workflow change.
3. S5 through the quality audit fixes.
4. S6 and S7 as routine maintenance.
