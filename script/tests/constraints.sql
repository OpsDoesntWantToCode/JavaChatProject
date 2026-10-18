\set ON_ERROR_STOP on
BEGIN;
SET LOCAL search_path = public, pg_catalog;
DO $$
BEGIN
    IF current_database() !~ '^java_chat_test_[0-9a-f]{32}$' THEN
        RAISE EXCEPTION 'Refusing non-test database';
    END IF;
END;
$$;

-- Helpers exist only in this connection. No permanent test tables/functions.
CREATE FUNCTION pg_temp.assert_true(ok BOOLEAN, label TEXT) RETURNS void
LANGUAGE plpgsql AS $$
BEGIN
    IF ok IS DISTINCT FROM TRUE THEN RAISE EXCEPTION 'FAIL: %',label; END IF;
    RAISE NOTICE 'PASS: %',label;
END;
$$;

CREATE FUNCTION pg_temp.expect_error(stmt TEXT, wanted_state TEXT, label TEXT,
                                     wanted_constraint TEXT DEFAULT NULL) RETURNS void
LANGUAGE plpgsql AS $$
DECLARE actual_state TEXT; actual_constraint TEXT;
BEGIN
    BEGIN
        EXECUTE stmt;
    EXCEPTION WHEN OTHERS THEN
        GET STACKED DIAGNOSTICS actual_state=RETURNED_SQLSTATE, actual_constraint=CONSTRAINT_NAME;
        IF actual_state=wanted_state AND
           (wanted_constraint IS NULL OR actual_constraint=wanted_constraint) THEN
            RAISE NOTICE 'PASS: % [% / %]',label,actual_state,actual_constraint;
            RETURN;
        END IF;
        RAISE EXCEPTION 'FAIL: %, wanted % / %, got % / %',
            label,wanted_state,wanted_constraint,actual_state,actual_constraint;
    END;
    RAISE EXCEPTION 'FAIL: %, statement unexpectedly succeeded',label;
END;
$$;

-- Independent catalog expectations, not only a CREATE statement count.
SELECT pg_temp.assert_true((SELECT array_agg(relname::text ORDER BY relname) = ARRAY[
    'activity_logs','chat_groups','conversation_clears','direct_messages','friend_requests',
    'friendships','group_members','group_messages','login_history','message_hidden','spam_reports',
    'user_blocks','users'] FROM pg_class WHERE relnamespace='public'::regnamespace AND relkind='r'), 'exactly 13 expected tables');
SELECT pg_temp.assert_true((SELECT count(*)=65 FROM information_schema.columns WHERE table_schema='public'), '65 columns');
SELECT pg_temp.assert_true((SELECT count(*)=58 FROM information_schema.columns WHERE table_schema='public' AND is_nullable='NO'), '58 required columns');
SELECT pg_temp.assert_true((SELECT count(*)=13 FROM pg_constraint WHERE connamespace='public'::regnamespace AND contype='p'), '13 primary keys');
SELECT pg_temp.assert_true((SELECT count(*)=21 FROM pg_constraint WHERE connamespace='public'::regnamespace AND contype='f'), '21 foreign keys');
SELECT pg_temp.assert_true((SELECT count(*)=4 FROM pg_constraint WHERE connamespace='public'::regnamespace AND contype='u'), '4 unique constraints');
SELECT pg_temp.assert_true((SELECT count(*)=31 FROM pg_constraint WHERE connamespace='public'::regnamespace AND contype='c'), '31 CHECK constraints');
SELECT pg_temp.assert_true((SELECT count(*)=33 FROM pg_indexes WHERE schemaname='public'), '33 indexes including PK/UNIQUE backing indexes');
SELECT pg_temp.assert_true((SELECT count(*)=2 FROM pg_trigger WHERE NOT tgisinternal AND tgrelid IN
    (SELECT oid FROM pg_class WHERE relnamespace='public'::regnamespace)), '2 application triggers');
SELECT pg_temp.assert_true((SELECT data_type='uuid' FROM information_schema.columns
    WHERE table_schema='public' AND table_name='activity_logs' AND column_name='session_id'), 'session stored as UUID');
SELECT pg_temp.assert_true(NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conrelid='conversation_clears'::regclass
    AND contype='f' AND conkey @> ARRAY[(SELECT attnum FROM pg_attribute WHERE attrelid='conversation_clears'::regclass
    AND attname='last_cleared_message_id')]::smallint[]), 'watermark deliberately has no FK');
SELECT pg_temp.assert_true((SELECT count(*)=8 AND bool_and(seqcache=1 AND seqincrement=1 AND NOT seqcycle AND seqstart=1)
    FROM pg_sequence s JOIN pg_class c ON c.oid=s.seqrelid WHERE c.relnamespace='public'::regnamespace), '8 sequences: CACHE 1, INCREMENT 1, START 1, NO CYCLE');
SELECT pg_temp.assert_true((SELECT count(*)=8 AND bool_and(attidentity='a') FROM pg_attribute
    WHERE attrelid IN (SELECT oid FROM pg_class WHERE relnamespace='public'::regnamespace AND relkind='r')
    AND attname='id'), '8 GENERATED ALWAYS identity columns');

DO $$
DECLARE r RECORD; matched BOOLEAN;
BEGIN
    FOR r IN SELECT * FROM (VALUES
      ('login_history','user_id','users','r'), ('activity_logs','user_id','users','r'),
      ('friend_requests','sender_id','users','c'), ('friend_requests','receiver_id','users','c'),
      ('friendships','user_id_1','users','c'), ('friendships','user_id_2','users','c'),
      ('user_blocks','blocker_id','users','c'), ('user_blocks','blocked_id','users','c'),
      ('direct_messages','sender_id','users','r'), ('direct_messages','receiver_id','users','r'),
      ('message_hidden','message_id','direct_messages','c'), ('message_hidden','user_id','users','c'),
      ('conversation_clears','user_id','users','c'), ('conversation_clears','partner_id','users','c'),
      ('chat_groups','created_by','users','r'), ('group_members','group_id','chat_groups','c'),
      ('group_members','user_id','users','r'), ('group_messages','group_id','chat_groups','c'),
      ('group_messages','sender_id','users','r'), ('spam_reports','reporter_id','users','r'),
      ('spam_reports','reported_id','users','r')) AS expected(child,col,parent,del)
    LOOP
      SELECT EXISTS (SELECT 1 FROM pg_constraint c JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=c.conkey[1]
        JOIN pg_attribute p ON p.attrelid=c.confrelid AND p.attnum=c.confkey[1]
        WHERE c.contype='f' AND c.conrelid=to_regclass('public.'||r.child)
          AND c.confrelid=to_regclass('public.'||r.parent) AND a.attname=r.col AND p.attname='id'
          AND cardinality(c.conkey)=1 AND c.confdeltype::text=r.del AND c.confupdtype='a'
          AND NOT c.condeferrable AND c.convalidated) INTO matched;
      PERFORM pg_temp.assert_true(matched,format('FK %s.%s -> %s.id / %s',r.child,r.col,r.parent,r.del));
    END LOOP;
END;
$$;

-- Verify all 16 explicit index keys, order, uniqueness and predicates.
DO $$
DECLARE r RECORD; actual TEXT; is_unique BOOLEAN; pred TEXT; keys TEXT;
BEGIN
    FOR r IN SELECT * FROM (VALUES
      ('idx_login_history_time','login_history','login_time DESC',false,NULL),
      ('idx_login_history_user_time','login_history','user_id,login_time DESC',false,NULL),
      ('idx_activity_time_user','activity_logs','created_at,user_id',false,NULL),
      ('idx_activity_user_time','activity_logs','user_id,created_at',false,NULL),
      ('uq_pending_friend_pair','friend_requests','LEAST(sender_id,receiver_id),GREATEST(sender_id,receiver_id)',true,'PENDING'),
      ('idx_friend_requests_inbox','friend_requests','receiver_id,created_at DESC',false,'PENDING'),
      ('idx_friendships_second','friendships','user_id_2,user_id_1',false,NULL),
      ('idx_dm_pair_id','direct_messages','LEAST(sender_id,receiver_id),GREATEST(sender_id,receiver_id),id',false,NULL),
      ('idx_dm_sender_time','direct_messages','sender_id,created_at,receiver_id',false,NULL),
      ('idx_dm_receiver_id','direct_messages','receiver_id,id',false,NULL),
      ('idx_group_members_user','group_members','user_id,group_id',false,NULL),
      ('idx_group_messages_time','group_messages','group_id,created_at,id',false,NULL),
      ('idx_group_messages_sender_time','group_messages','sender_id,created_at,group_id',false,NULL),
      ('idx_spam_reports_time','spam_reports','created_at DESC',false,NULL),
      ('idx_users_created_at','users','created_at',false,NULL),
      ('uq_group_message_iv','group_messages','group_id,iv',true,'iv IS NOT NULL')) AS expected(name,tbl,cols,uniq,predicate)
    LOOP
      -- pg_get_indexdef(index, column, pretty) omits sort direction. Read
      -- indoption (zero-based) as well; never ignore DESC/NULLS semantics.
      SELECT string_agg(pg_get_indexdef(i.indexrelid,k,true) ||
               CASE i.indoption[k-1] WHEN 0 THEN '' WHEN 3 THEN ' DESC'
                 WHEN 1 THEN ' DESC NULLS LAST' WHEN 2 THEN ' NULLS FIRST'
                 ELSE ' UNSUPPORTED_OPTION' END,',' ORDER BY k),
             bool_and(i.indisunique), max(pg_get_expr(i.indpred,i.indrelid))
        INTO keys,is_unique,pred
        FROM pg_index i CROSS JOIN LATERAL generate_series(1,i.indnkeyatts) k
        WHERE i.indexrelid=to_regclass('public.'||r.name) AND i.indrelid=to_regclass('public.'||r.tbl)
          AND i.indisvalid AND i.indisready;
      PERFORM pg_temp.assert_true(lower(regexp_replace(keys,'\s','','g'))=lower(regexp_replace(r.cols,'\s','','g'))
         AND is_unique=r.uniq AND ((r.predicate IS NULL AND pred IS NULL)
           OR (r.predicate IS NOT NULL AND position(r.predicate IN pred)>0)), 'index '||r.name);
    END LOOP;
    PERFORM pg_temp.assert_true(to_regclass('public.idx_dm_pair_time') IS NULL,'old DM timestamp index absent');
END;
$$;

-- Fixture has one valid row in every table. Hash is a non-login test marker;
-- it is not a real password or seeded product account. Everything rolls back.
INSERT INTO users(username,password_hash,full_name,email)
VALUES ('test_alice','test-only-hash-not-for-login','Alice','alice@example.test'),
       ('test_bob','test-only-hash-not-for-login','Bob','bob@example.test'),
       ('test_carol','test-only-hash-not-for-login','Carol','carol@example.test');
INSERT INTO login_history(user_id) SELECT id FROM users WHERE username='test_alice';
INSERT INTO activity_logs(user_id,session_id) SELECT id,'11111111-1111-4111-8111-111111111111'::uuid FROM users WHERE username='test_alice';
INSERT INTO friend_requests(sender_id,receiver_id) SELECT a.id,b.id FROM users a,users b WHERE a.username='test_alice' AND b.username='test_bob';
INSERT INTO friendships(user_id_1,user_id_2) SELECT a.id,b.id FROM users a,users b WHERE a.username='test_alice' AND b.username='test_bob';
INSERT INTO user_blocks(blocker_id,blocked_id) SELECT a.id,b.id FROM users a,users b WHERE a.username='test_alice' AND b.username='test_carol';
INSERT INTO direct_messages(sender_id,receiver_id,content) SELECT a.id,b.id,'hello' FROM users a,users b WHERE a.username='test_alice' AND b.username='test_bob';
INSERT INTO message_hidden(message_id,user_id) SELECT id,sender_id FROM direct_messages;
INSERT INTO conversation_clears(user_id,partner_id,cleared_at) SELECT sender_id,receiver_id,clock_timestamp() FROM direct_messages;
INSERT INTO chat_groups(group_name,created_by) SELECT 'Plain group',id FROM users WHERE username='test_alice';
INSERT INTO group_members(group_id,user_id,is_admin) SELECT id,created_by,true FROM chat_groups;
INSERT INTO group_messages(group_id,sender_id,content) SELECT id,created_by,'group hello' FROM chat_groups;
INSERT INTO spam_reports(reporter_id,reported_id,reason) SELECT receiver_id,sender_id,'test' FROM direct_messages;
SELECT pg_temp.assert_true((SELECT bool_and(status='ACTIVE' AND role='USER' AND created_at IS NOT NULL) FROM users), 'user defaults');
SELECT pg_temp.assert_true((SELECT bool_and(action_type='OPEN_APP' AND created_at IS NOT NULL) FROM activity_logs), 'OPEN_APP defaults');
SELECT pg_temp.assert_true((SELECT bool_and(NOT is_read AND created_at IS NOT NULL) FROM direct_messages), 'DM defaults');
SELECT pg_temp.assert_true((SELECT bool_and(last_cleared_message_id=0) FROM conversation_clears), 'zero watermark default');
SELECT pg_temp.assert_true((SELECT bool_and(NOT is_encrypted AND wrapped_encryption_key IS NULL) FROM chat_groups), 'plaintext group defaults');

-- Every PK rejects an exact duplicate; every required column rejects NULL.
-- Each FK is tested with a fresh PK and a missing parent, not only introspected.
DO $$
DECLARE r RECORD; row_data JSONB; bad JSONB; stmt TEXT; wrong_id BIGINT := 9000000000000000000;
BEGIN
    FOR r IN SELECT conrelid::regclass AS tbl,conname FROM pg_constraint
             WHERE connamespace='public'::regnamespace AND contype='p'
    LOOP
      PERFORM pg_temp.expect_error(format('INSERT INTO %s OVERRIDING SYSTEM VALUE SELECT * FROM %s LIMIT 1',r.tbl,r.tbl),
                                  '23505','PK '||r.tbl,r.conname);
    END LOOP;
    FOR r IN SELECT table_name,column_name FROM information_schema.columns WHERE table_schema='public' AND is_nullable='NO'
    LOOP
      EXECUTE format('SELECT to_jsonb(t) FROM public.%I t LIMIT 1',r.table_name) INTO row_data;
      bad := row_data;
      IF row_data ? 'id' THEN bad := bad || jsonb_build_object('id',wrong_id); END IF;
      bad := bad || jsonb_build_object(r.column_name,NULL);
      PERFORM pg_temp.expect_error(format(
          'INSERT INTO public.%I OVERRIDING SYSTEM VALUE SELECT * FROM jsonb_populate_record(NULL::public.%I,%L::jsonb)',
          r.table_name,r.table_name,bad::text),'23502','NOT NULL '||r.table_name||'.'||r.column_name);
    END LOOP;
    FOR r IN SELECT c.conrelid::regclass AS tbl,a.attname,c.conname FROM pg_constraint c
             JOIN pg_attribute a ON a.attrelid=c.conrelid AND a.attnum=c.conkey[1]
             WHERE c.connamespace='public'::regnamespace AND c.contype='f'
    LOOP
      EXECUTE format('SELECT to_jsonb(t) FROM %s t LIMIT 1',r.tbl) INTO row_data;
      bad := row_data || jsonb_build_object(r.attname,CASE WHEN r.attname='user_id_1' THEN -1 ELSE wrong_id END);
      IF row_data ? 'id' THEN bad := bad || jsonb_build_object('id',wrong_id); END IF;
      -- A missing-parent probe must not reuse the existing UNIQUE session.
      IF row_data ? 'session_id' THEN
        bad := bad || jsonb_build_object('session_id','99999999-9999-4999-8999-999999999999');
      END IF;
      stmt := format('INSERT INTO %s OVERRIDING SYSTEM VALUE SELECT * FROM jsonb_populate_record(NULL::%s,%L::jsonb)',r.tbl,r.tbl,bad::text);
      -- group_messages group guard also reports 23503 before declarative FK.
      PERFORM pg_temp.expect_error(stmt,'23503','FK rejects missing parent '||r.conname);
    END LOOP;
END;
$$;

SELECT pg_temp.expect_error($q$INSERT INTO users(username,password_hash,full_name,email) VALUES('test_alice','x','X','other@example.test')$q$,'23505','duplicate username','uq_users_username');
SELECT pg_temp.expect_error($q$INSERT INTO users(username,password_hash,full_name,email) VALUES('test_other','x','X','alice@example.test')$q$,'23505','duplicate email','uq_users_email');
SELECT pg_temp.expect_error($q$INSERT INTO activity_logs(user_id,session_id) SELECT id,'11111111-1111-4111-8111-111111111111'::uuid FROM users WHERE username='test_bob'$q$,'23505','UUID unique even across owners','uq_activity_session');
SELECT pg_temp.expect_error($q$INSERT INTO activity_logs(user_id,session_id) SELECT id,'not-a-uuid'::uuid FROM users LIMIT 1$q$,'22P02','UUID syntax');
SELECT pg_temp.expect_error($q$INSERT INTO friend_requests(sender_id,receiver_id) SELECT sender_id,receiver_id FROM friend_requests LIMIT 1$q$,'23505','PENDING same direction','uq_pending_friend_pair');
SELECT pg_temp.expect_error($q$INSERT INTO friend_requests(sender_id,receiver_id) SELECT receiver_id,sender_id FROM friend_requests LIMIT 1$q$,'23505','PENDING reversed direction','uq_pending_friend_pair');
INSERT INTO friend_requests(sender_id,receiver_id,status) SELECT sender_id,receiver_id,'REJECTED' FROM friend_requests LIMIT 1;
SELECT pg_temp.assert_true((SELECT count(*)=2 FROM friend_requests),'non-PENDING historical request allowed');
SELECT pg_temp.expect_error($q$INSERT INTO spam_reports(reporter_id,reported_id) SELECT reporter_id,reported_id FROM spam_reports LIMIT 1$q$,'23505','one lifetime report per directed pair','uq_spam_report_pair');

-- CHECK boundary tests; empty email violates both ASCII and nonempty checks,
-- so that case accepts either CHECK name (SQLSTATE must still be 23514).
DO $$
DECLARE r RECORD;
BEGIN
    FOR r IN SELECT * FROM (VALUES
      ('users','username','''ABC''','chk_users_username'),
      ('users','full_name','''   ''','chk_users_full_name'),
      ('users','gender','''INVALID''','chk_users_gender'),
      ('users','email','''UPPER@example.test''','chk_users_email_canonical'),
      ('users','email','''a b@example.test''','chk_users_email_ascii'),
      ('users','status','''INVALID''','chk_users_status'),
      ('users','role','''ROOT''','chk_users_role'),
      ('activity_logs','action_type','''RECONNECT''','chk_activity_action'),
      ('friend_requests','status','''CANCELLED''','chk_fr_status'),
      ('friend_requests','receiver_id','sender_id','chk_fr_no_self'),
      ('friendships','user_id_2','user_id_1','chk_friendship_order'),
      ('user_blocks','blocked_id','blocker_id','chk_block_no_self'),
      ('direct_messages','receiver_id','sender_id','chk_dm_no_self'),
      ('direct_messages','content','''''','chk_dm_content'),
      ('conversation_clears','partner_id','user_id','chk_clear_no_self'),
      ('conversation_clears','last_cleared_message_id','-1','chk_clear_watermark_nonnegative'),
      ('chat_groups','group_name','''   ''','chk_group_name'),
      ('chat_groups','wrapped_encryption_key','''forbidden''','chk_group_key_mode'),
      ('group_messages','content','''''','chk_group_message_content'),
      ('spam_reports','reported_id','reporter_id','chk_spam_no_self'),
      ('spam_reports','reason','repeat(''x'',2001)','chk_spam_reason')) AS expected(tbl,col,bad,con)
    LOOP
      PERFORM pg_temp.expect_error(format('UPDATE public.%I SET %I=%s',r.tbl,r.col,r.bad),
                                  '23514','CHECK '||r.con,r.con);
    END LOOP;
    FOR r IN SELECT table_name FROM information_schema.columns WHERE table_schema='public' AND column_name='id'
    LOOP
      -- Identity ALWAYS disallows UPDATE id=0 before CHECK. Insert an overridden
      -- copy instead to explicitly test the positive-ID CHECK.
      PERFORM pg_temp.expect_error(format(
        'INSERT INTO public.%I OVERRIDING SYSTEM VALUE SELECT * FROM jsonb_populate_record(NULL::public.%I,(SELECT to_jsonb(t)||''{"id":0}''::jsonb FROM public.%I t LIMIT 1))',
        r.table_name,r.table_name,r.table_name),'23514','positive identity '||r.table_name,'chk_'||r.table_name||'_id_positive');
    END LOOP;
END;
$$;
SELECT pg_temp.expect_error($q$UPDATE users SET email=''$q$,'23514','empty email rejected');
SELECT pg_temp.expect_error($q$INSERT INTO friendships(user_id_1,user_id_2) SELECT user_id_2,user_id_1 FROM friendships LIMIT 1$q$,'23514','reversed friendship','chk_friendship_order');
SELECT pg_temp.expect_error($q$UPDATE direct_messages SET content=repeat('x',65537)$q$,'23514','DM size bound','chk_dm_content');
SELECT pg_temp.expect_error($q$UPDATE group_messages SET content=repeat('x',65537)$q$,'23514','group content size bound','chk_group_message_content');

-- Schema-only encrypted fixtures: dummy envelopes/payloads, NOT AEAD tests.
INSERT INTO chat_groups(group_name,created_by,is_encrypted,wrapped_encryption_key)
SELECT 'Encrypted test',id,true,'test-only-wrapped-envelope' FROM users WHERE username='test_alice';
INSERT INTO group_messages(group_id,sender_id,content,iv)
SELECT id,created_by,'test-only-ciphertext','AAAAAAAAAAAAAAAA' FROM chat_groups WHERE is_encrypted;
SELECT pg_temp.expect_error($q$INSERT INTO group_messages(group_id,sender_id,content,iv) SELECT group_id,sender_id,content,iv FROM group_messages WHERE iv IS NOT NULL$q$,'23505','duplicate IV per group','uq_group_message_iv');
SELECT pg_temp.expect_error($q$UPDATE group_messages SET iv='bad' WHERE iv IS NOT NULL$q$,'23514','IV format','chk_group_message_iv');
SELECT pg_temp.expect_error($q$INSERT INTO group_messages(group_id,sender_id,content) SELECT id,created_by,'bad' FROM chat_groups WHERE is_encrypted$q$,'23514','encrypted group requires IV');
SELECT pg_temp.expect_error($q$INSERT INTO group_messages(group_id,sender_id,content,iv) SELECT id,created_by,'bad','BBBBBBBBBBBBBBBB' FROM chat_groups WHERE NOT is_encrypted$q$,'23514','plain group rejects IV');
SELECT pg_temp.expect_error($q$UPDATE chat_groups SET is_encrypted=NOT is_encrypted$q$,'23514','group mode immutable');
SELECT pg_temp.expect_error($q$UPDATE group_messages SET sender_id=(SELECT id FROM users WHERE username='test_carol')$q$,'23514','sender immutable');
SELECT pg_temp.expect_error($q$UPDATE group_messages SET group_id=(SELECT id FROM chat_groups WHERE is_encrypted) WHERE iv IS NULL$q$,'23514','message group immutable');
INSERT INTO group_messages(group_id,sender_id,content) SELECT id,created_by,'second plain' FROM chat_groups WHERE NOT is_encrypted;
SELECT pg_temp.assert_true((SELECT count(*)=2 FROM group_messages WHERE iv IS NULL),'multiple plaintext NULL IV allowed');

-- Soft delete changes no historical row/relationship. This does not implement
-- Java account authorization, terminal-state policy or last-admin protection.
DO $$
DECLARE before_counts JSONB; after_counts JSONB; r RECORD;
BEGIN
    SELECT jsonb_build_object('dm',(SELECT count(*) FROM direct_messages),'login',(SELECT count(*) FROM login_history),
      'activity',(SELECT count(*) FROM activity_logs),'group_messages',(SELECT count(*) FROM group_messages),
      'members',(SELECT count(*) FROM group_members),'spam',(SELECT count(*) FROM spam_reports),
      'friends',(SELECT count(*) FROM friendships),'hidden',(SELECT count(*) FROM message_hidden),
      'clears',(SELECT count(*) FROM conversation_clears)) INTO before_counts;
    UPDATE users SET status='DELETED' WHERE username='test_alice';
    SELECT jsonb_build_object('dm',(SELECT count(*) FROM direct_messages),'login',(SELECT count(*) FROM login_history),
      'activity',(SELECT count(*) FROM activity_logs),'group_messages',(SELECT count(*) FROM group_messages),
      'members',(SELECT count(*) FROM group_members),'spam',(SELECT count(*) FROM spam_reports),
      'friends',(SELECT count(*) FROM friendships),'hidden',(SELECT count(*) FROM message_hidden),
      'clears',(SELECT count(*) FROM conversation_clears)) INTO after_counts;
    PERFORM pg_temp.assert_true(before_counts=after_counts,'soft delete preserves history/relationships');
END;
$$;
SELECT pg_temp.expect_error($q$INSERT INTO users(username,password_hash,full_name,email) VALUES('test_alice','x','X','new@example.test')$q$,'23505','DELETED username remains reserved','uq_users_username');
-- PostgreSQL 18 reports RESTRICT deletion as restrict_violation (23001),
-- distinct from missing-parent foreign_key_violation (23503).
SELECT pg_temp.expect_error($q$DELETE FROM users WHERE username='test_alice'$q$,'23001','hard delete historical user restricted');

-- Representative CASCADE execution; all 21 exact policies checked above.
DELETE FROM direct_messages;
SELECT pg_temp.assert_true(NOT EXISTS(SELECT FROM message_hidden),'message deletion cascades hidden markers');
DELETE FROM chat_groups;
SELECT pg_temp.assert_true(NOT EXISTS(SELECT FROM group_members) AND NOT EXISTS(SELECT FROM group_messages),'group deletion cascades dependent rows');

-- Fixture DML and helpers roll back. Sequences can advance even on rollback;
-- only run on a newly created disposable database, never a working database.
ROLLBACK;
