# Backend integration scaffold (not deployed)

The V7 Android client keeps local data on-device. This directory documents the production backend boundary. A future backend should provide: authenticated user sessions, device enrollment with approval on both sides, revocable short-lived session tokens, consent receipts, message provider adapters, webhook signatures, rate limits, audit logs, database migrations and backups.

No server URL, API key, OAuth client secret or database credential is included. The Android app only stores a user-supplied HTTPS webhook URL and encrypts optional provider tokens locally; it does not call the webhook in this build.
