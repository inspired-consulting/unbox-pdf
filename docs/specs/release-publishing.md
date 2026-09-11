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

## Publishing

Creating a GitHub release runs the publishing workflow, which publishes to Maven
Central through the Central Portal. The namespace is `consulting.inspired`.

For local publishing, use a Maven `settings.xml` server entry named `central`
and the corresponding signing configuration. `./mvnw --batch-mode deploy
-DautoPublish=false` uploads a release bundle for validation without automatically
publishing it; the deployment can then be dropped in the Portal.

Snapshots are enabled for the namespace. Start the workflow manually while the
version ends with `-SNAPSHOT` to publish a snapshot. Consumers must configure
`https://central.sonatype.com/repository/maven-snapshots/` with snapshots enabled.
