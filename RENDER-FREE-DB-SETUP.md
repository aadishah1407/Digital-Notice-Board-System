# Free Render + TiDB deployment

This configuration keeps the Render web service on the Free plan and uses TiDB Cloud Starter as the external MySQL-protocol-compatible database. No Render MySQL private service or persistent disk is created.

## 1. Create the free TiDB cluster

1. Open https://tidbcloud.com/ and sign in.
2. Create a **TiDB Cloud Starter** instance using the MySQL-compatible option.
3. Create a database named `digital_notice_board` (or use the database name you choose in the connection string).
4. In the cluster's **Connect** dialog, choose **Public**, branch **main**, and **General** connection.
5. Generate a database password and copy the host, port, username, and database name. Starter uses port `4000` and requires TLS.
6. Keep the password private. Do not commit it to GitHub or paste it into source files.

TiDB Cloud Starter's free allowance is quota-based. When the free quota is reached, operations can be throttled or new connections denied until the monthly reset or an explicit spending-limit change. Leave the spending limit unset if you want to avoid usage-based charges.

## 2. Deploy the Render Blueprint

1. Open https://dashboard.render.com/blueprints.
2. Create a Blueprint Instance and select `aadishah1407/Digital-Notice-Board-System`, branch `main`.
3. Render reads the root `render.yaml` and creates the `digital-notice-board-system` web service on the Free plan.
4. When prompted for environment variables, enter:
   - `DB_URL`: `jdbc:mysql://<TI​​DB_HOST>:4000/digital_notice_board?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3`
   - `DB_USERNAME`: the username shown in TiDB's Connect dialog
   - `DB_PASSWORD`: the generated TiDB password
   - `ADMIN_USERNAME`: a new administrator username
   - `ADMIN_PASSWORD`: a strong unique administrator password

Replace `<TIDB_HOST>` with the exact host from the TiDB Connect dialog. The JDBC URL uses TLS certificate and hostname verification. Keep all credentials in Render's environment-variable form, not in the repository.

## 3. Continuous deployment

The Render Blueprint sets `autoDeployTrigger: checksPass`. After the service is connected to GitHub, Render waits for GitHub Actions checks on a commit to `main`. It deploys only when the checks pass. The workflow runs:

- Maven `clean verify` (compile, JUnit tests, package)
- Docker image build
- JAR artifact upload

No Render deploy-hook secret is required for this Blueprint-based auto-deploy path.

## 4. Free-tier limitations

- Render Free web services spin down after 15 minutes without inbound traffic and can take about a minute to start again.
- The Render Free filesystem is ephemeral. This app must store user and notice data in TiDB, not local files.
- Render may suspend a Free web service that makes unusually high volumes of outbound requests, including database traffic.
- TiDB Starter is MySQL-protocol compatible, not a self-hosted MySQL server. The app currently uses Spring Data JPA and MySQL Connector/J; test the app's schema and CRUD flows against the cluster before relying on it.

## References

- TiDB Java/JDBC: https://docs.pingcap.com/developer/dev-guide-sample-application-java-jdbc/
- TiDB Starter quotas: https://docs.pingcap.com/tidbcloud/serverless-limitations/
- Render free services: https://render.com/docs/free
- Render Blueprint spec: https://render.com/docs/blueprint-spec
