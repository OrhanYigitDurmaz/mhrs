create table auth_users (
    id uuid primary key,
    email varchar(320) not null,
    password_hash varchar(255) not null,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    role varchar(20) not null,
    email_verified boolean not null,
    created_at timestamp not null,
    updated_at timestamp not null
);

create unique index uk_auth_users_email on auth_users(email);

create table auth_sessions (
    id uuid primary key,
    user_id uuid not null references auth_users(id),
    device varchar(30) not null,
    ip_address varchar(64),
    user_agent varchar(255),
    created_at timestamp not null,
    last_seen_at timestamp not null,
    revoked_at timestamp
);

create index idx_auth_sessions_user_id on auth_sessions(user_id);

create table auth_refresh_tokens (
    id uuid primary key,
    session_id uuid not null references auth_sessions(id),
    token_hash varchar(128) not null,
    expires_at timestamp not null,
    revoked_at timestamp,
    rotated_at timestamp,
    created_at timestamp not null
);

create unique index uk_auth_refresh_token_hash on auth_refresh_tokens(token_hash);
create index idx_auth_refresh_tokens_session_id on auth_refresh_tokens(session_id);

create table auth_email_verification_tokens (
    id uuid primary key,
    user_id uuid not null references auth_users(id),
    token_hash varchar(128) not null,
    expires_at timestamp not null,
    used_at timestamp,
    created_at timestamp not null
);

create unique index uk_auth_verification_token_hash on auth_email_verification_tokens(token_hash);
create index idx_auth_verification_tokens_user_id on auth_email_verification_tokens(user_id);

create table auth_password_reset_tokens (
    id uuid primary key,
    user_id uuid not null references auth_users(id),
    token_hash varchar(128) not null,
    expires_at timestamp not null,
    used_at timestamp,
    created_at timestamp not null
);

create unique index uk_auth_reset_token_hash on auth_password_reset_tokens(token_hash);
create index idx_auth_reset_tokens_user_id on auth_password_reset_tokens(user_id);
