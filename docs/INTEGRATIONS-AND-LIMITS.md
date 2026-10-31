# External integration status

This Android project is a local-first control and diagnostic client. It includes opt-in message draft composition, a local device registry, ADB command helper, local FAQ templates, local admin-role labels, encrypted API token storage and HTTPS webhook URL configuration.

The following need separately provisioned services and credentials before they can be live: Google OAuth / passkeys, Telegram Bot API, WhatsApp Business Cloud API, email campaign provider, cloud database, multi-device sync, remote screen sharing and remote assistance. No service keys are embedded in the source. The app does not falsely mark these as connected.

To enable production integrations, deploy an authenticated HTTPS backend, implement token-scoped endpoints, verify consent and unsubscribe state server-side, add rate limiting, audit logs, data retention and revocation. Never put provider tokens in a public repository.
