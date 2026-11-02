# MySQL to MongoDB migration (fresh collections)

This migration switches the application to MongoDB Atlas. It deliberately starts with **fresh collections**. It does not export, drop, truncate, or alter the existing MySQL database. Existing MySQL records remain in the original database and are not visible to the new application until separately migrated.

## Application changes

- Replaces Spring Data JPA/Hibernate with Spring Data MongoDB.
- Replaces the MySQL JDBC driver and H2 test database with MongoDB support.
- Stores users in the `users` collection and notices in the `notices` collection.
- Uses MongoDB ObjectId values exposed as Java `String` IDs.
- Creates a unique index on `users.username`.
- Stores `creatorId` and `creatorUsername` on notice documents instead of a JPA `ManyToOne` relationship.
- Replaces JPQL notice filters with MongoTemplate criteria queries.
- Keeps REST endpoint paths and request/response fields; notice `id` values are now strings.
- Adds Testcontainers integration tests against a temporary MongoDB 7 replica set.

## 1. Create a MongoDB Atlas Free cluster

1. Sign in at https://cloud.mongodb.com/.
2. Create a project for Digital Notice Board.
3. Choose **Create → M0 Free** and select a supported cloud provider/region. Prefer a region near the Render Singapore service when available.
4. Create a database user with a strong unique password. Give the user read/write access only to the application database.
5. In Network Access, add the Render service's outbound CIDR ranges. In Render, open the service's **Connect → Outbound** panel and copy the listed ranges. Render's ranges are shared by region and can rotate; avoid `0.0.0.0/0` unless there is no practical alternative.
6. In Atlas, click **Connect → Drivers → Java** and copy the connection URI. Replace the password placeholder. URL-encode any reserved characters in the password.
7. Use database name `digital_notice_board` in the URI, for example:

```text
mongodb+srv://<db-user>:<encoded-password>@<cluster-host>/digital_notice_board?retryWrites=true&w=majority&appName=DigitalNoticeBoard
```

Do not paste credentials into GitHub, source files, issue trackers, or this documentation.

## 2. Configure Render

1. Open https://dashboard.render.com/blueprints.
2. Select the `Digital-Notice-Board-System` repository and branch `main`.
3. Apply the updated Blueprint. The web service remains on Render's Free plan.
4. Set these environment variables when prompted or in the service's Environment tab:
   - `MONGODB_URI`: the full Atlas connection URI from the previous step.
   - `ADMIN_USERNAME`: a new administrator username.
   - `ADMIN_PASSWORD`: a strong, unique administrator password.
5. Save the variables and deploy. The Render Blueprint sets `autoDeployTrigger: checksPass`, so later commits to `main` deploy after the required GitHub Actions checks succeed.

The first application startup creates the `users` and `notices` collections as documents are saved and creates the unique username index because MongoDB index auto-creation is enabled.

## 3. Test and verify

After the service is Live:

1. Open the deployed application URL.
2. Register or sign in with the new administrator account.
3. Create a test notice and confirm it appears in the admin dashboard.
4. Register a test student with a department and academic year.
5. Sign in as the student and verify department/year filters.
6. Edit and delete the test notice.
7. Check Render logs for MongoDB authentication, DNS, or IP access-list errors.

GitHub Actions runs `mvn clean verify`, including the existing authentication unit tests and the MongoDB Testcontainers integration tests, then builds the production Docker image. Testcontainers tests use a temporary database and do not connect to Atlas.

## 4. Data safety and rollback

- The MySQL database and its existing rows are not modified by this code migration.
- The new Atlas database starts empty. Admin and student accounts must be registered again unless you separately choose to migrate records later.
- Do not delete the old MySQL database, its backups, or credentials during this switch.
- For rollback, redeploy the prior MySQL application commit and restore the old MySQL environment variables. Keep the old MySQL database available until MongoDB operation has been accepted and any required data migration/backup is complete.

## 5. Free-tier considerations

Atlas Free clusters have a 0.5 GB storage limit, up to 100 operations/second, and up to 500 connections. Atlas can automatically pause a Free cluster after 30 days with zero connections. Monitor usage and keep independent backups of important records.

Render Free web services spin down after 15 minutes without inbound traffic, use an ephemeral local filesystem, and have monthly usage limits. The application's persistent state must therefore be stored in Atlas, never in local files on Render.

## References

- MongoDB Atlas Free cluster limits: https://www.mongodb.com/docs/atlas/reference/free-shared-limitations/
- MongoDB Java driver connection: https://www.mongodb.com/docs/drivers/java/sync/current/connection/mongoclient/
- Render outbound IP addresses: https://render.com/docs/outbound-ip-addresses
- Render free service limits: https://render.com/docs/free
- Render Blueprint specification: https://render.com/docs/blueprint-spec
