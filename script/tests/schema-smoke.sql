-- Run after database.sql in the same disposable database accepted by email-uniqueness.sql.
-- All fixtures roll back. Content/envelope markers are not real credentials or encryption.
-- Checks SQL guards only; does not verify Server permissions, concurrency or cryptography.
\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path = public, pg_catalog;
DO $$
DECLARE
    first_user bigint;
    second_user bigint;
    plain_group bigint;
    encrypted_group bigint;
    message_id bigint;
    passed integer := 0;
BEGIN
    IF current_database() !~ '^java_chat_email_test_[0-9a-f]{12}$' THEN
        RAISE EXCEPTION 'Wrong smoke-test database';
    END IF;
    IF EXISTS (SELECT 1 FROM users) THEN
        RAISE EXCEPTION 'Email test did not roll back its rows';
    END IF;
    passed := passed + 1;
    INSERT INTO users(username,password_hash,full_name,email)
    VALUES('smoke.first','test-only-not-for-login','Smoke first','first@example.test') RETURNING id INTO first_user;
    INSERT INTO users(username,password_hash,full_name,email)
    VALUES('smoke.second','test-only-not-for-login','Smoke second','second@example.test') RETURNING id INTO second_user;
    INSERT INTO chat_groups(group_name,created_by) VALUES('Plain smoke',first_user) RETURNING id INTO plain_group;
    INSERT INTO chat_groups(group_name,created_by,is_encrypted,wrapped_encryption_key)
    VALUES('Encrypted mode smoke',first_user,true,'test-only-envelope-not-a-key') RETURNING id INTO encrypted_group;
    BEGIN
        UPDATE chat_groups SET is_encrypted=true, wrapped_encryption_key='test-only-envelope-not-a-key' WHERE id=plain_group;
        RAISE EXCEPTION 'Mode change unexpectedly allowed';
    EXCEPTION WHEN check_violation THEN
        IF SQLERRM <> 'Group encryption mode is immutable' THEN RAISE; END IF;
        passed := passed + 1;
    END;
    BEGIN
        INSERT INTO group_messages(group_id,sender_id,content,iv) VALUES(plain_group,first_user,'test-only-content','AAAAAAAAAAAAAAAA');
        RAISE EXCEPTION 'Plain group accepted IV';
    EXCEPTION WHEN check_violation THEN
        IF SQLERRM <> 'Message IV does not match group mode' THEN RAISE; END IF;
        passed := passed + 1;
    END;
    BEGIN
        INSERT INTO group_messages(group_id,sender_id,content) VALUES(encrypted_group,first_user,'test-only-content');
        RAISE EXCEPTION 'Encrypted group accepted missing IV';
    EXCEPTION WHEN check_violation THEN
        IF SQLERRM <> 'Message IV does not match group mode' THEN RAISE; END IF;
        passed := passed + 1;
    END;
    INSERT INTO group_messages(group_id,sender_id,content,iv)
    VALUES(encrypted_group,first_user,'test-only-content','AAAAAAAAAAAAAAAA') RETURNING id INTO message_id;
    passed := passed + 1;
    BEGIN
        INSERT INTO group_messages(group_id,sender_id,content,iv)
        VALUES(encrypted_group,first_user,'test-only-content','AAAAAAAAAAAAAAAA');
        RAISE EXCEPTION 'Duplicate IV accepted';
    EXCEPTION WHEN unique_violation THEN passed := passed + 1;
    END;
    BEGIN
        UPDATE group_messages SET sender_id=second_user WHERE id=message_id;
        RAISE EXCEPTION 'Message sender change accepted';
    EXCEPTION WHEN check_violation THEN
        IF SQLERRM <> 'Message group and sender are immutable' THEN RAISE; END IF;
        passed := passed + 1;
    END;
    INSERT INTO login_history(user_id) VALUES(first_user);
    BEGIN
        DELETE FROM users WHERE id=first_user;
        RAISE EXCEPTION 'Historical identity physically deleted';
    EXCEPTION WHEN foreign_key_violation THEN passed := passed + 1;
    END;
    RAISE NOTICE 'PASS: % rollback/trigger/history smoke checks; no cryptography or Server verification.', passed;
END;
$$;
ROLLBACK;
SELECT count(*) AS remaining_users FROM users;
SELECT count(*) AS remaining_messages FROM group_messages;
SELECT count(*) AS public_tables FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';
