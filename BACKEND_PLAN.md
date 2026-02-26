# Backend Plan (Early Development)

## Scope
This document defines the backend service architecture for the MHRS clone and the implementation order for early-stage development.

## Early Development Policy
- We are in early development.
- We will use auto migration (`hibernate-orm.database.generation=update`) to move fast while domain models are still changing.
- This is temporary.
- Before beta/staging, we will switch to versioned migrations (Flyway/Liquibase) and set production schema mode to `validate`.

## Architecture Strategy
Start as a modular monolith in Quarkus, with clear service boundaries that can be split into microservices later.

Rationale:
- Faster MVP delivery.
- Lower operational complexity.
- Easier cross-domain transactions while appointment rules are still evolving.

## Database Boundary Model (Schema per Module)
Use one database instance with separate schemas per domain from day one:
- `identity_schema`
- `scheduling_schema`
- `appointment_schema`
- `directory_schema`
- `patient_schema`
- `notification_schema` (delivery state, retries, dead letters, templates, consumer checkpoints)
- `search_schema` (index sync state, reconciliation jobs, search projection control tables)
- `admin_schema`
- `waitlist_schema`

Boundary rule:
- No direct cross-schema joins in domain repositories.
- Cross-domain reads must happen through internal APIs, domain services, or domain events.

Cross-schema transaction policy:
- Default: disallow a single local transaction that mutates more than one domain schema.
- New cross-domain write workflows must use outbox-driven eventual consistency and Saga-style orchestration.
- Exception path: temporary boundary exceptions require a written ADR, explicit owner, and expiry date.
- Each exception must include a removal milestone before service extraction.

Migration implication:
- This keeps data ownership clear and reduces entanglement when extracting microservices later.

## Target Microservices (Logical Boundaries)

### 1. Identity & Access Service
Responsibilities:
- Registration, login, logout, token refresh.
- JWT issuance and validation.
- Role management (`PATIENT`, `DOCTOR`, `ADMIN`).
- Account status controls (active/blocked).
- Refresh token rotation and revocation enforcement.

Core data:
- `users`
- `roles`
- `user_roles`
- `refresh_tokens`

Key endpoints:
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /auth/logout`

Token policy:
- Access token TTL: 10 minutes.
- Refresh token TTL: 30 days, rotated on each refresh.
- Logout revokes refresh token session/family immediately in `refresh_tokens`.
- Token family definition: per-device/per-login session lineage (`family_id`); reuse revokes only that family, not all user devices.
- Refresh token reuse detection revokes the whole token family and forces re-authentication.
- Access tokens rely on short TTL; emergency `jti` blocklist is optional for incident response.

### 2. Scheduling Service
Responsibilities:
- Doctor working hours, breaks, leave.
- Slot generation rules.
- Slot availability checks.
- Double-booking prevention.
- Schedule mutations that impact confirmed appointments must run a managed impact workflow.
- Validate doctor active status against Directory source of truth before creating/updating schedules.
- Consume doctor lifecycle events (deactivated/suspended) to close future open slots and block new reservations.
- Slot generation policy: pre-generate rolling `60` days, with nightly refill and immediate regeneration on schedule changes.
- Timezone policy: store slot timestamps in UTC, persist `clinic_timezone`, and render in clinic-local time.
- DST policy: generate by clinic-local wall clock and map to UTC at generation time with DST gap/overlap rules.

Core data:
- `doctor_schedules`
- `doctor_leaves`
- `slots`

Key endpoints:
- `POST /doctors/{id}/schedules/create`
- `POST /doctors/{id}/leaves/create`
- `GET /doctors/{id}/slots?date=...`

### 3. Appointment Service
Responsibilities:
- Appointment creation and lifecycle.
- Formal state-machine enforcement for transitions.
- State transitions:
  - `CREATED`
  - `CONFIRMED`
  - `CANCELLED`
  - `RESERVATION_EXPIRED`
  - `COMPLETED`
  - `NO_SHOW`
- Concurrency-safe booking and cancellation.
- Patient-side overlap prevention for active appointments.

Valid transitions (initial):
- `CREATED -> CONFIRMED`
- `CREATED -> CANCELLED`
- `CREATED -> RESERVATION_EXPIRED`
- `CONFIRMED -> CANCELLED`
- `CONFIRMED -> COMPLETED`
- `CONFIRMED -> NO_SHOW`

Invalid transitions must be rejected with a domain error (for example, `CREATED -> COMPLETED`).

Reschedule abuse policy:
- Reschedule is a command on a `CONFIRMED` appointment and keeps status as `CONFIRMED` after success.
- Maximum `3` patient/doctor-initiated reschedules per appointment.
- Patient-initiated reschedules enforce cooldown of `15` minutes between actions.
- `FORCED_RESCHEDULE` by admin/system does not consume patient reschedule quota and is tracked separately.
- Exceeding limits returns `409 Conflict`.

Cancellation policy:
- Patient cancellation allowed until `120` minutes before appointment start.
- Doctor/admin cancellation allowed at any time with mandatory reason code.
- Repeated late cancellations are tracked for abuse scoring and policy actions.

Transition race handling:
- Validate current state in application logic before attempting persistence.
- Use `@Version` and conditional updates to reject stale/concurrent transitions.
- Return `409 Conflict` for stale state, invalid state, or concurrent transition attempts.

Terminal and override policy:
- `CANCELLED`, `RESERVATION_EXPIRED`, `COMPLETED`, and `NO_SHOW` are terminal for patient/doctor flows.
- Standard appointment endpoints must reject transitions from terminal states with `409 Conflict`.
- Admin corrections require a dedicated override endpoint with mandatory reason code and immutable audit entry.
- No direct terminal-to-terminal transition in standard flow.

Reservation-expiry reconciliation policy:
- Scheduling is the source of truth for reservation-token expiry (`slot_reservations`).
- When a reservation transitions to `EXPIRED`, scheduling emits `slot.reservation.expired` with `token_id`, `slot_id`, `patient_id`, and `expired_at`.
- Appointment consumes the event idempotently and transitions only matching `CREATED` appointments to `RESERVATION_EXPIRED`.
- If the appointment is already `CONFIRMED`/terminal, the expiry event is ignored and recorded as a reconciled no-op.
- SLO: `CREATED` appointments tied to expired reservations must converge to `RESERVATION_EXPIRED` within `30s`.

Core data:
- `appointments`
- `appointment_history`

Key endpoints:
- `POST /appointments/create`
- `PATCH /appointments/{id}/confirm`
- `PATCH /appointments/{id}/cancel`
- `PATCH /appointments/{id}/reschedule`
- `PATCH /appointments/{id}/complete`
- `PATCH /appointments/{id}/no-show`
- `POST /appointments/{id}/admin-override`

Appointment history policy:
- `appointment_history` stores before/after snapshots for status, slot, actor, reason code, and source (`PATIENT`, `DOCTOR`, `ADMIN`, `SYSTEM`).
- Retention target: minimum `10` years for medical/legal traceability.
- Query access: patient sees own records, doctor sees assigned records, admin has scoped audited access.

### 4. Directory Service
Responsibilities:
- Clinic, department, specialty catalog.
- Doctor-to-clinic and doctor-to-specialty mappings.
- Administrative metadata.
- Doctor lifecycle status (`ACTIVE`, `INACTIVE`, `SUSPENDED`) as source of truth for scheduling eligibility.

Core data:
- `clinics`
- `departments`
- `specialties`
- `doctor_profiles`

Doctor lifecycle consistency policy:
- Directory emits `doctor.lifecycle.changed` domain event on deactivation/suspension.
- Scheduling consumes event, closes future unbooked slots, and blocks new reservations for that doctor.
- Appointment domain flags future confirmed appointments as conflict candidates and creates admin conflict cases.
- Final patient-facing status changes (`FORCED_CANCEL`/reschedule outcome) are applied only via Phase 4 admin workflow with audit + notifications.

### 5. Patient Service
Responsibilities:
- Patient profile management.
- Appointment history queries.
- Preference/contact metadata.
- Identity verification status management (including TCKN verification workflow).

Core data:
- `patient_profiles`
- `patient_verification_status`

### 6. Notification Service
Responsibilities:
- Appointment confirmation/reminder/cancellation notifications.
- Async processing from event bus.
- Retry and dead-letter handling.

Core data:
- `notification_deliveries`
- `notification_retries`
- `notification_dead_letters`
- `notification_templates`
- `notification_processing_checkpoints`

Integration:
- RabbitMQ exchanges/queues for MVP.
- Kafka is deferred and requires an ADR with replay/ordering migration plan.

### 7. Search Service
Responsibilities:
- Index doctors/patients for fast search.
- Sync index from domain events.

Core data:
- `search_sync_state`
- `reindex_jobs`

Integration:
- Elasticsearch indexes.

### 8. Admin & Audit Service
Responsibilities:
- Admin operations and reports.
- Immutable audit log trail.
- Conflict-case handling for schedule changes that impact confirmed appointments.

Core data:
- `audit_logs`
- reporting read models
- `schedule_conflict_cases`

### 9. Waitlist Service
Responsibilities:
- Waitlist registration for unavailable slots.
- Auto-notify when slot becomes available.
- Create temporary slot holds for waitlist offers and expire them on timeout.
- Promote next eligible waitlist entry when an offer expires or is declined.
- Issue actionable offers (accept/decline) bound to hold windows.
- Eligibility/fairness policy: FIFO within the same clinic/department/specialty queue by default, with explicit priority tiers only via audited admin rules.
- Patients who decline or expire can rejoin at queue tail.

Core data:
- `waitlist_entries`
- `waitlist_offers`

## Service Ownership in MVP (Single Quarkus App)
Implement as modules/packages inside one deployable first:
- `identity`
- `scheduling`
- `appointments`
- `directory`
- `patient`
- `notification`
- `search`
- `admin`
- `waitlist`

Each module should have:
- `resource` (HTTP API)
- `service` (business logic)
- `repository` (persistence)
- `model` (entities/DTOs)

## Suggested Build Phases

### Phase 1 (Foundation)
- Identity & Access.
- Directory.
- Scheduling base model.
- Add ArchUnit tests to enforce module boundaries in CI.
- Forbid direct repository/service access across domain packages.

### Phase 2 (Core Booking)
- Appointment Service with strict transactional integrity per domain and orchestrated cross-domain booking.
- Authoritative slot state machine in scheduling domain: `OPEN -> RESERVED -> BOOKED`.
- Booking coordination model:
- Step 1: in one scheduling-domain transaction, atomically transition slot `OPEN -> RESERVED` and create `scheduling_schema.slot_reservations` row (`PESSIMISTIC_WRITE`, lock timeout `300ms`).
- Step 2: create appointment in `appointment_schema` referencing reservation token, requiring token status `RESERVED` and `expires_at > now`.
- Step 3: finalize reservation `RESERVED -> BOOKED`; on failure, compensate to `OPEN`.
- Step 4: reconciliation path: if reservation expires before Step 3 succeeds, scheduling emits `slot.reservation.expired` and appointment transitions `CREATED -> RESERVATION_EXPIRED`.
- Step 1/2/3 execute as separate local transactions per domain; cross-schema single-transaction writes are not used.
- Reservation token contract:
- Token fields: `token_id`, `slot_id`, `patient_id`, `status`, `source`, `expires_at`, `created_at`.
- Token statuses: `RESERVED`, `BOOKED`, `RELEASED`, `EXPIRED`.
- `source`: `DIRECT_BOOKING` or `WAITLIST_OFFER`.
- Single hold primitive: both direct booking and waitlist use `slot_reservations`; no parallel slot-hold mechanism.
- Hold TTL: `120` seconds for `DIRECT_BOOKING`; `WAITLIST_OFFER` uses offer window duration.
- Active-hold scope rules:
- At most one active reservation token per `slot_id` (enforced by unique active-slot constraint).
- Optional anti-abuse cap: max concurrent active holds per patient is configurable (default `2`).
- Step 2 is idempotent on `(token_id, patient_id)` and cannot create more than one appointment.
- Expiry correctness is enforced inline on read/write paths (`expires_at <= now` invalidates token); sweeper is cleanup-only.
- Sweeper runs every `15s` for orphan cleanup, slot release, and `slot.reservation.expired` emission for reconciliation.
- Double-booking prevention with impact-based locking:
- Booking-critical slot inventory writes in scheduling domain use `PESSIMISTIC_WRITE`.
- Appointment aggregate concurrency uses `@Version` and transition precondition checks.
- Appointment creation/reschedule must reject patient-overlapping active appointments.
- Non-booking profile updates use `@Version` optimistic locking.
- Contention strategy for hot slots: fast-fail `409` + server-advised retry-after jitter; optional queue mode when contention threshold is exceeded.
- Schedule changes that conflict with confirmed appointments are rejected by default (`409 Conflict`) and must go through admin-managed reschedule/cancel workflow (defined in Phase 4).
- Basic patient profile.

### Phase 3 (Operational Features)
- Notifications via queue using the transactional outbox pattern.
- Persist domain events in a domain-owned outbox table (for example `appointment_schema.outbox_events`) within the same transaction as the domain write.
- Publish outbox events asynchronously via a Quarkus polling worker with retry/backoff and dead-letter handling.
- Polling baseline: every `2s`, batch size `200`, ordered by `created_at`, using `FOR UPDATE SKIP LOCKED`.
- Defer Debezium/CDC adoption to a later phase and gate it behind an ADR and operational readiness checks.
- Waitlist flow with explicit offer-hold lifecycle:
- On slot release, assign offer to next eligible waitlist entry and create `WAITLIST_OFFER` reservation token.
- Offer-hold duration is configurable per department/clinic with `15` minutes default.
- Notification service delivers actionable offer message (timed deep link + `offer_id` + `token_id` + expiry).
- If accepted within hold window, booking flow uses existing `token_id` and must not mint a second token.
- Booking API accepts `offer_id`/`token_id` and atomically books against that reservation.
- If expired/declined, release hold and promote next waitlist entry.
- If queue is empty, return slot to public booking inventory.
- Search indexing with Elasticsearch.
- Define search freshness target (`<= 60s` staleness for non-critical reads).
- Run periodic reconciliation/reindex job to repair drift between primary DB and Elasticsearch.
- Booking eligibility decisions must be validated against primary DB, not Elasticsearch.
- Elasticsearch outage mode: circuit-breaker protected fallback to DB-backed limited search and explicit degraded-mode response.

### Phase 4 (Governance)
- Admin panel APIs.
- Audit logs and basic reporting endpoints.
- Schedule conflict management workflow (forward reference from Phase 2):
- Standard doctor schedule mutation endpoint on conflict returns `409` with conflict summary (no auto-write).
- Admin conflict endpoint initiates case and emits `schedule.conflict.detected`.
- Trigger: doctor leave/working-hours change conflicts with confirmed appointments.
- Step 1: create `schedule_conflict_case` and link affected appointments.
- Step 2: admin selects action (`FORCED_RESCHEDULE`, `FORCED_CANCEL`, `MANUAL_INTERVENTION`).
- Step 3: action emits appointment domain events and notification intents.
- Step 4: each action writes immutable audit records with actor, reason code, and timestamp.
- SLA: conflict cases must be resolved within `24h`, with escalation to operations lead at `12h`.

## Cross-Cutting Requirements
- JWT-based auth with role checks + resource ownership checks.
- Idempotency keys for booking/cancel endpoints with explicit storage policy.
- Store keys in `appointment_schema.idempotency_keys` with unique scope `(actor_id, operation, key)`.
- Persist request hash, response metadata, created/expiry timestamps.
- Same key + same payload returns prior result; same key + different payload returns `409 Conflict`.
- Apply TTL cleanup (for example 24-48 hours) with retention metrics.
- Structured error model (`code`, `message`, `details`).
- Observability: request logs, metrics, tracing.
- Contract-first API docs (OpenAPI).
- Integration tests for booking race conditions.
- Rate limiting and abuse prevention on booking endpoints:
- Per patient: `6` booking attempts/minute, burst `3`.
- Per IP: `60` booking attempts/minute, burst `20`.
- Per patient-per-slot: `3` attempts/minute.
- Client retry guidance: exponential backoff with jitter and honor `Retry-After`.
- Booking window policy: minimum notice `15` minutes, maximum advance booking `60` days.
- Patient identity verification gate: unverified patients cannot confirm bookings.
- Event contract policy: all domain events use versioned envelope (`event_type`, `event_version`, `event_id`, `occurred_at`, `payload`) with backward-compatible evolution rules.
- Localization policy: notification templates, user-facing errors, and reason codes must support `tr-TR` and `en-US` at minimum.
- Data lifecycle policy: default to soft delete for regulated entities; hard delete only via controlled retention jobs.

Data privacy and compliance baseline (GDPR/KVKK-aware):
- Restrict cleartext PII storage to Identity and Patient domains.
- Other modules should reference surrogate identifiers and minimal required metadata.
- Define masking/redaction rules for logs, events, and admin responses.
- Enforce masking at query/projection layer (repository/view DTO) before objects leave the owning domain.
- Sensitive identifiers (for example TCKN) must use field-level encryption plus HMAC-SHA256 lookup digest with KMS-managed secret/pepper.
- Plain deterministic hashing without keyed HMAC is disallowed.
- Hash key rotation and rehash migration requires dual-read migration plan (old+new digest), rolling rehash job, and cutover ADR before production.
- Encrypt data at rest for all databases/backups and manage keys via KMS/HSM-backed rotation policy.
- GDPR/KVKK erasure handling: preserve immutable audit structure while anonymizing subject identifiers via irreversible pseudonymization/tombstoning.

## Data and Migration Strategy
Early stage:
- Use Quarkus auto migration for fast iteration.
- Keep schema naming stable and explicit to reduce later migration risk.

Migration hardening milestone (Phase 3 exit gate):
- Owner: Backend Lead + Platform/DB Owner.
- Freeze schema changes for a defined window and generate migration baseline.
- Apply Flyway/Liquibase baseline to staging from a production-like snapshot.
- Disable auto-migration in staging and run one full sprint with migration-only schema changes.
- Gate to Phase 4 requires zero unmanaged schema drift in staging.

Stabilization stage:
- Freeze schema changes per sprint.
- Introduce Flyway/Liquibase migration scripts.
- Backfill migration baseline.
- Disable auto migration in staging/production.

## Split-to-Microservices Triggers
Split only when at least one is true:
- Team ownership requires independent deploys.
- Notification/search load scales independently.
- Booking traffic creates resource contention.
- Release cadence differs significantly across domains.
- Regulatory or vendor isolation requires separate operational/compliance boundaries.

Recommended first extractions:
1. Notification Service.
2. Search Service.
3. Waitlist Service.

## Audit Integrity Model
- Treat audit logs as immutable append-only records.
- Prefer dedicated schema/table permissions separating write/read/admin duties.
- Block update/delete operations on audit rows at DB permission level.
- If required, add hash chaining/signatures for tamper evidence.
- Partition audit tables by month and archive closed partitions to cold storage per retention policy.
- Maintain query indexes for active partitions and lifecycle-manage old indexes to control cost.

## Initial Definition of Done
- End-to-end flow: patient login -> search doctor -> view slots -> create appointment -> notification emitted.
- No double booking under concurrent requests:
- Load test with `50` concurrent booking attempts for the same slot results in exactly `1` success and `49` `409 Conflict`.
- Booking API response target under slot contention: `p95 <= 800ms`.
- For the same test, server-side `5xx` rate must be `0%` and DB pool exhaustion alerts must remain at `0`.
- Audit entry for each state transition.
- Role and ownership checks enforced server-side.
- Outbox SLO: `>= 99%` of events published to queue within `30s` of DB commit.
- Search freshness: normal drift `<= 60s`, plus daily reconciliation job reporting zero unhandled drift.
