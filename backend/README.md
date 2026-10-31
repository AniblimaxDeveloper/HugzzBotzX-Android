# HugzzBotzX V8 backend boundary (not deployed)

The Android client is local-first. The following production components are not included or live in this source ZIP:
- Google OAuth/OIDC verification and account recovery;
- server-side role/permission enforcement;
- cloud device enrollment and live multi-device sync;
- real-time remote assistance/screen sharing;
- official WhatsApp Business / Telegram message delivery;
- webhook workers, rate limiting, durable database, migrations and server backups.

A production deployment must use HTTPS, authenticate users server-side, issue short-lived revocable device/session tokens, record explicit target-device consent, validate webhook signatures, enforce opt-in/opt-out, throttle messaging and retain auditable logs. Never put provider secrets in the repository. Local encrypted token storage is not an active server integration.
