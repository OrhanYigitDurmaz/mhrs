# MHRS Clone

Patients book doctor appointments.

## Stack
- Quarkus
- Svelte + TypeScript + shadcn-svelte
- MariaDB
- RabbitMQ/Kafka
- Valkey
- Elasticsearch
- JWT
- Jenkins

## Core
- Roles: patient, doctor, admin
- Doctor schedules: working hours, breaks, leave
- Slot engine: slot generation and double-booking prevention
- Appointment lifecycle: create, confirm, cancel, reschedule, complete, no-show
- Patient profile and appointment history
- Clinic/departments/specialties management
- Notifications: confirmation, reminder, cancellation
- Search doctors and patients (Elasticsearch)
- Waitlist for freed slots
- Admin panel, audit logs, and reporting
