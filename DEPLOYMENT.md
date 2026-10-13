# Deployment configuration

The application requires secrets and host-specific settings from the deployment environment. Do not commit real values.

## Backend environment

- `DB_URL`: production MySQL JDBC URL, configured for TLS by the database provider.
- `DB_USERNAME` and `DB_PASSWORD`: a dedicated database account; do not use the local root account in production.
- `JWT_SECRET`: a randomly generated secret of at least 32 bytes. Rotate the previously committed database password and JWT secret before deployment.
- `JWT_EXPIRATION_MS`: JWT lifetime in milliseconds (default: 7 days).
- `CORS_ALLOWED_ORIGINS`: comma-separated exact frontend origins, such as `https://autocare.example.com`.
- `JPA_DDL_AUTO`: set to `validate` in production after applying schema migrations. The development default is `update`.
- `JPA_SHOW_SQL`: keep `false` in production.

To create the first administrator, set `BOOTSTRAP_ADMIN_USERNAME`, `BOOTSTRAP_ADMIN_EMAIL`, and `BOOTSTRAP_ADMIN_PASSWORD` (at least 12 characters) for the first backend startup. The account is created only if that username and email are unused. Remove those bootstrap variables after startup. Administrators can create mechanic accounts from the admin panel.

## Frontend environment

Set `VITE_API_URL` to the backend API base URL at frontend build time, for example `https://api.autocare.example.com/api`. For local development, it defaults to `http://localhost:8080/api`.

Serve the frontend and API over HTTPS in production. Never expose database or signing secrets as `VITE_*` variables; those are embedded in frontend assets.
