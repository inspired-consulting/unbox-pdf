# Release publishing

The release workflow uses the GitHub environment `Maven Release`. Its protection
rules require approval from `devs` and disallow self-review.

## Required secrets

| Secret | Content |
| --- | --- |
| `CENTRAL_USERNAME` | Username from a Central Portal user token. |
| `CENTRAL_TOKEN` | Password from that token. |
| `GPG_PRIVATE_KEY` | ASCII-armored private signing key. |
| `GPG_PASSPHRASE` | Passphrase for the signing key. |

## Release procedure

A release includes a versioned commit, an annotated Git tag, a published GitHub
release with release notes, and publication to Maven Central. Creating the GitHub
release triggers the [publishing workflow](../../.github/workflows/maven-publish.yml),
which publishes through the Central Portal under `consulting.inspired`. Pushing a
tag alone does not trigger that workflow.

Run the following steps from the repository root. The commands assume a release
from `main`, an `origin` remote pointing to this repository, and an authenticated
GitHub CLI (`gh`).

1. Choose the version and included changes. Review the commits since the previous
   release and the working tree; preserve unrelated work. Normally the release
   includes the current `main` branch.
2. Set the same release version, without `-SNAPSHOT`, in the root `pom.xml`,
   `samples/pom.xml`, and README version references. Turn the unreleased changelog
   section into a dated release entry covering all included changes.
3. Validate and build the release artifacts, then compile the samples against the
   installed release:

   ```bash
   ./mvnw -B -ntp -Dgpg.skip install
   ./mvnw -B -ntp -f samples/pom.xml clean package
   git diff --check
   ```

   The library build runs the tests, including byte-for-byte PDF regression
   comparisons, and creates the library, source, and Javadoc JARs. Signing is
   skipped locally; the publishing workflow signs the artifacts.
4. Commit the release metadata and create an annotated tag using the bare version
   number, such as `0.10.1`, without a `v` prefix. Push the commit and tag together.
   Replace the example version below with the version being released:

   ```bash
   release_version=0.10.1
   git add pom.xml samples/pom.xml README.md CHANGELOG.md
   git commit -m "Release $release_version"
   git tag -a "$release_version" -m "Release $release_version"
   git push --atomic origin main "refs/tags/$release_version"
   ```

5. Wait for [CI](../../.github/workflows/ci.yml) on that release commit to pass on
   Java 17 and 21 before creating the GitHub release.
6. Prepare `target/release-notes.md` from the release's changelog entry, including
   the Maven coordinates and a comparison link to the previous tag. Create the
   GitHub release using the existing tag and those notes:

   ```bash
   gh release create "$release_version" \
       --repo inspired-consulting/unbox-pdf \
       --verify-tag \
       --title "Unbox $release_version" \
       --notes-file target/release-notes.md
   ```

   This creates the published GitHub release, rather than a draft, and starts the
   Maven publishing workflow. Verify that the release links to the intended tag
   and commit.
7. Monitor the publishing workflow. A different member of `devs` must approve the
   `Maven Release` environment because self-review is disabled. Report the GitHub
   release URL and publication status separately: an existing GitHub release does
   not mean the package is already on Maven Central. If approval is pending,
   provide the workflow URL and identify that remaining step. After approval,
   check that publication succeeds before reporting Maven Central availability.

Keep published release tags unchanged. Any subsequent snapshot version bump is a
separate commit after the release tag.

## Local publishing and snapshots

For local publishing, use a Maven `settings.xml` server entry named `central`
and the corresponding signing configuration. `./mvnw --batch-mode deploy
-DautoPublish=false` uploads a release bundle for validation without automatically
publishing it; the deployment can then be dropped in the Portal.

Snapshots are enabled for the namespace. Start the workflow manually while the
version ends with `-SNAPSHOT` to publish a snapshot. Consumers must configure
`https://central.sonatype.com/repository/maven-snapshots/` with snapshots enabled.
