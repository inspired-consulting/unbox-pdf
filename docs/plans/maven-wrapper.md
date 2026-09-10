# Introduce Maven Wrapper

Status: Planned; not implemented.

## Objective

Provide a consistent Maven version for contributors and CI through repository
wrapper scripts. Until this plan is implemented, continue using installed `mvn`.

## Implementation steps

1. Select and pin a Maven version compatible with the Java 17 target and existing
   build plugins. Check official Maven Wrapper documentation at implementation time.
2. Generate `mvnw`, `mvnw.cmd`, and the required `.mvn/wrapper/` files using the
   official wrapper tooling. Preserve the executable permission on `mvnw` and
   configure distribution checksum verification where supported.
3. Verify the root wrapper can also build the separate samples project using
   `./mvnw -f samples/pom.xml package` after installing the root library.
4. Update commands in `AGENTS.md`, the library specification, and relevant README
   instructions to use `./mvnw` (or `mvnw.cmd` on Windows). Keep the existing
   local `-Dgpg.skip` guidance. `CLAUDE.md` continues to import `AGENTS.md`.
5. Update the release workflow to invoke the wrapper while preserving the current
   release trigger and deployment configuration.
6. Document the required JDK and the first-run download of the pinned Maven
   distribution. Commit wrapper bootstrap files; keep downloaded distributions
   and generated build output out of version control.

## Validation and completion criteria

- `./mvnw --version` reports the pinned Maven version.
- `./mvnw test` passes, including existing PDF reference comparisons.
- `./mvnw -Dgpg.skip install` succeeds, followed by the samples build above.
- Verify startup without an installed Maven, and verify the Windows launcher where
  a Windows environment is available; report any platform checks not performed.
- Review the workflow change without publishing a release as a validation step.
- Documentation consistently uses the wrapper after it has been added.

This plan does not include changing library dependencies, PDF output, Java target,
or the release publishing configuration beyond the Maven invocation.
