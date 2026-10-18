-- Java Chat System -- PostgreSQL, fresh database only (not a migration).
-- Run: psql -X -v ON_ERROR_STOP=1 -d <new_database> -f script/database.sql
-- Source: docs/database/Database_Decisions.md, sections 2-8.
-- No DROP, credentials, seeded passwords, keys, extensions or extra tables.
-- Owner runs DDL. Provision a separate non-owner Server runtime role separately;
-- never give clients a DB credential. This script grants no runtime privileges.
BEGIN;
SET LOCAL search_path = public, pg_catalog;

-- Fail instead of overwriting/reusing an existing application schema.
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_class c JOIN pg_namespace n ON n.oid=c.relnamespace
               WHERE n.nspname='public' AND c.relkind IN ('r','p','v','m','S','f')) THEN
        RAISE EXCEPTION 'Expected an empty public schema; use a new database';
    END IF;
END;
$$;

CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE),
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    address TEXT,
    dob DATE,
    gender VARCHAR(10),
    email VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_id_positive CHECK (id > 0),
    CONSTRAINT chk_users_username CHECK (username ~ '^[a-z0-9_.-]{3,50}$'),
    CONSTRAINT chk_users_full_name CHECK (btrim(full_name) <> ''),
    CONSTRAINT chk_users_gender CHECK (gender IN ('MALE','FEMALE','OTHER')),
    CONSTRAINT chk_users_email_canonical CHECK (email = lower(btrim(email))),
    CONSTRAINT chk_users_email_nonempty CHECK (email <> ''),
    CONSTRAINT chk_users_email_ascii CHECK (email ~ '^[!-~]+$'),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE','LOCKED','DELETED')),
    CONSTRAINT chk_users_role CHECK (role IN ('USER','ADMIN'))
);

CREATE TABLE login_history (
    id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE),
    user_id BIGINT NOT NULL,
    login_time TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_login_history PRIMARY KEY (id),
    CONSTRAINT chk_login_history_id_positive CHECK (id > 0),
    CONSTRAINT fk_login_history_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE activity_logs (
    id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE),
    user_id BIGINT NOT NULL,
    action_type VARCHAR(30) NOT NULL DEFAULT 'OPEN_APP',
    session_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_activity_logs PRIMARY KEY (id),
    CONSTRAINT uq_activity_session UNIQUE (session_id),
    CONSTRAINT chk_activity_logs_id_positive CHECK (id > 0),
    CONSTRAINT chk_activity_action CHECK (action_type IN ('OPEN_APP')),
    CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE friend_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE),
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_friend_requests PRIMARY KEY (id),
    CONSTRAINT chk_friend_requests_id_positive CHECK (id > 0),
    CONSTRAINT chk_fr_status CHECK (status IN ('PENDING','ACCEPTED','REJECTED')),
    CONSTRAINT chk_fr_no_self CHECK (sender_id <> receiver_id),
    CONSTRAINT fk_friend_requests_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_requests_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE friendships (
    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_friendships PRIMARY KEY (user_id_1,user_id_2),
    CONSTRAINT chk_friendship_order CHECK (user_id_1 < user_id_2),
    CONSTRAINT fk_friendships_first FOREIGN KEY (user_id_1) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_friendships_second FOREIGN KEY (user_id_2) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_blocks (
    blocker_id BIGINT NOT NULL,
    blocked_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_user_blocks PRIMARY KEY (blocker_id,blocked_id),
    CONSTRAINT chk_block_no_self CHECK (blocker_id <> blocked_id),
    CONSTRAINT fk_user_blocks_blocker FOREIGN KEY (blocker_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_blocks_blocked FOREIGN KEY (blocked_id) REFERENCES users(id) ON DELETE CASCADE
);

-- CACHE 1 is part of the watermark invariant. Every DM writer must take the
-- same pair advisory TRANSACTION lock BEFORE INSERT/identity allocation.
-- Identity alone does not imply commit order. Do not reserve IDs in Java.
CREATE TABLE direct_messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE),
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_direct_messages PRIMARY KEY (id),
    CONSTRAINT chk_direct_messages_id_positive CHECK (id > 0),
    CONSTRAINT chk_dm_no_self CHECK (sender_id <> receiver_id),
    CONSTRAINT chk_dm_content CHECK (content <> '' AND octet_length(content) <= 65536),
    CONSTRAINT fk_direct_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_direct_messages_receiver FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE message_hidden (
    message_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    hidden_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_message_hidden PRIMARY KEY (message_id,user_id),
    CONSTRAINT fk_message_hidden_message FOREIGN KEY (message_id) REFERENCES direct_messages(id) ON DELETE CASCADE,
    CONSTRAINT fk_message_hidden_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE conversation_clears (
    user_id BIGINT NOT NULL,
    partner_id BIGINT NOT NULL,
    last_cleared_message_id BIGINT NOT NULL DEFAULT 0,
    cleared_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT pk_conversation_clears PRIMARY KEY (user_id,partner_id),
    CONSTRAINT chk_clear_no_self CHECK (user_id <> partner_id),
    CONSTRAINT chk_clear_watermark_nonnegative CHECK (last_cleared_message_id >= 0),
    CONSTRAINT fk_conversation_clears_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_conversation_clears_partner FOREIGN KEY (partner_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE chat_groups (
    id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE),
    group_name VARCHAR(100) NOT NULL,
    created_by BIGINT NOT NULL,
    is_encrypted BOOLEAN NOT NULL DEFAULT FALSE,
    wrapped_encryption_key TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_chat_groups PRIMARY KEY (id),
    CONSTRAINT chk_chat_groups_id_positive CHECK (id > 0),
    CONSTRAINT chk_group_name CHECK (btrim(group_name) <> ''),
    CONSTRAINT chk_group_key_mode CHECK (
        (is_encrypted AND wrapped_encryption_key IS NOT NULL)
        OR (NOT is_encrypted AND wrapped_encryption_key IS NULL)),
    CONSTRAINT fk_chat_groups_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE group_members (
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_group_members PRIMARY KEY (group_id,user_id),
    CONSTRAINT fk_group_members_group FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE TABLE group_messages (
    id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE),
    group_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    iv VARCHAR(64),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_group_messages PRIMARY KEY (id),
    CONSTRAINT chk_group_messages_id_positive CHECK (id > 0),
    CONSTRAINT chk_group_message_content CHECK (content <> '' AND octet_length(content) <= 65536),
    CONSTRAINT chk_group_message_iv CHECK (iv IS NULL OR iv ~ '^[A-Za-z0-9+/]{16}$'),
    CONSTRAINT fk_group_messages_group FOREIGN KEY (group_id) REFERENCES chat_groups(id) ON DELETE CASCADE,
    CONSTRAINT fk_group_messages_sender FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Group policy: one reporter can report one reported account only once EVER.
-- Final Project.pdf does not require repeat reports or case status/history.
-- Server still must verify a committed DM from reported -> reporter exists.
CREATE TABLE spam_reports (
    id BIGINT GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 CACHE 1 NO CYCLE),
    reporter_id BIGINT NOT NULL,
    reported_id BIGINT NOT NULL,
    reason TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_spam_reports PRIMARY KEY (id),
    CONSTRAINT uq_spam_report_pair UNIQUE (reporter_id,reported_id),
    CONSTRAINT chk_spam_reports_id_positive CHECK (id > 0),
    CONSTRAINT chk_spam_no_self CHECK (reporter_id <> reported_id),
    CONSTRAINT chk_spam_reason CHECK (reason IS NULL OR char_length(reason) <= 2000),
    CONSTRAINT fk_spam_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_spam_reports_reported FOREIGN KEY (reported_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- 16 explicit indexes; 13 PK + 4 UNIQUE constraints create 17 more indexes.
-- Different leading keys support different queries; no duplicate PK indexes.
CREATE INDEX idx_login_history_time ON login_history (login_time DESC);
CREATE INDEX idx_login_history_user_time ON login_history (user_id,login_time DESC);
CREATE INDEX idx_activity_time_user ON activity_logs (created_at,user_id);
CREATE INDEX idx_activity_user_time ON activity_logs (user_id,created_at);
CREATE UNIQUE INDEX uq_pending_friend_pair ON friend_requests
    (LEAST(sender_id,receiver_id),GREATEST(sender_id,receiver_id)) WHERE status='PENDING';
CREATE INDEX idx_friend_requests_inbox ON friend_requests (receiver_id,created_at DESC) WHERE status='PENDING';
CREATE INDEX idx_friendships_second ON friendships (user_id_2,user_id_1);
CREATE INDEX idx_dm_pair_id ON direct_messages (LEAST(sender_id,receiver_id),GREATEST(sender_id,receiver_id),id);
CREATE INDEX idx_dm_sender_time ON direct_messages (sender_id,created_at,receiver_id);
CREATE INDEX idx_dm_receiver_id ON direct_messages (receiver_id,id);
CREATE INDEX idx_group_members_user ON group_members (user_id,group_id);
CREATE INDEX idx_group_messages_time ON group_messages (group_id,created_at,id);
CREATE INDEX idx_group_messages_sender_time ON group_messages (sender_id,created_at,group_id);
CREATE INDEX idx_spam_reports_time ON spam_reports (created_at DESC);
CREATE INDEX idx_users_created_at ON users (created_at);
CREATE UNIQUE INDEX uq_group_message_iv ON group_messages (group_id,iv) WHERE iv IS NOT NULL;

-- PROVISIONAL: Server-side AES-GCM, NOT E2EE. No plaintext DEK/KEK here.
-- One DEK per group, immutable mode; no per-member keys or hot key versions.
-- These guards enforce mode consistency, NOT actual encryption or membership.
CREATE FUNCTION guard_group_encryption_mode() RETURNS trigger
LANGUAGE plpgsql SET search_path = pg_catalog, public AS $$
BEGIN
    IF NEW.is_encrypted IS DISTINCT FROM OLD.is_encrypted THEN
        RAISE EXCEPTION USING ERRCODE='23514', MESSAGE='Group encryption mode is immutable';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_group_mode_immutable BEFORE UPDATE OF is_encrypted ON chat_groups
FOR EACH ROW EXECUTE FUNCTION guard_group_encryption_mode();

CREATE FUNCTION guard_group_message_mode() RETURNS trigger
LANGUAGE plpgsql SET search_path = pg_catalog, public AS $$
DECLARE encrypted BOOLEAN;
BEGIN
    -- Let the declarative NOT NULL constraint report a missing group_id.
    IF NEW.group_id IS NULL THEN RETURN NEW; END IF;
    IF TG_OP='UPDATE' THEN
        IF NEW.group_id IS DISTINCT FROM OLD.group_id OR NEW.sender_id IS DISTINCT FROM OLD.sender_id THEN
            RAISE EXCEPTION USING ERRCODE='23514', MESSAGE='Message group and sender are immutable';
        END IF;
    END IF;
    SELECT is_encrypted INTO encrypted FROM public.chat_groups WHERE id=NEW.group_id FOR UPDATE;
    IF NOT FOUND THEN
        RAISE EXCEPTION USING ERRCODE='23503', MESSAGE='Message group does not exist';
    END IF;
    IF encrypted <> (NEW.iv IS NOT NULL) THEN
        RAISE EXCEPTION USING ERRCODE='23514', MESSAGE='Message IV does not match group mode';
    END IF;
    RETURN NEW;
END;
$$;
CREATE TRIGGER trg_group_message_mode BEFORE INSERT OR UPDATE ON group_messages
FOR EACH ROW EXECUTE FUNCTION guard_group_message_mode();

COMMENT ON COLUMN activity_logs.session_id IS 'Unique OPEN_APP idempotency UUID; not an authentication token. Reconnect reuses it.';
COMMENT ON COLUMN users.role IS 'System role USER/ADMIN; independent of group_members.is_admin. Server must authorize every request.';
COMMENT ON COLUMN users.status IS 'Soft delete via DELETED. Terminal status and session revocation are enforced by Server, not this CHECK.';
COMMENT ON COLUMN conversation_clears.last_cleared_message_id IS 'One-sided visibility watermark; 0 allowed, deliberately NOT a FK. Pair lock before identity allocation is required.';
COMMENT ON COLUMN conversation_clears.cleared_at IS 'Audit/display only; not the visibility boundary. Server writes clock_timestamp().';
COMMENT ON COLUMN chat_groups.wrapped_encryption_key IS 'PROVISIONAL Server AES-GCM: wrapped DEK envelope only; KEK stays outside DB/repo/client.';
COMMENT ON TABLE spam_reports IS 'One report per directed reporter/reported pair for its entire lifetime. Server checks incoming DM evidence.';
COMMENT ON TABLE group_members IS 'Current membership. No message-to-membership FK: removing a member must preserve sent messages.';

-- Server work (not implemented by DDL): pair locks for block/friend/clear;
-- permission gate shared/exclusive, ADMIN bootstrap/last ACTIVE admin;
-- group membership/admin succession; authenticated sender/visibility/spam
-- evidence; BCrypt, canonicalization, AEAD validation and session revocation.
-- Soft delete never issues DELETE; keep history and username/email uniqueness.
COMMIT;
