CREATE EXTENSION IF NOT EXISTS postgres_fdw;

DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_foreign_server WHERE srvname='order_db_srv') THEN
            CREATE SERVER order_db_srv
                FOREIGN DATA WRAPPER postgres_fdw
                OPTIONS (host 'order-db', dbname 'order_db', port '5432'); -- 환경에 맞게
        END IF;
    END $$;

DO $$
    DECLARE u text := current_user;
    BEGIN
        IF EXISTS (
            SELECT 1 FROM pg_user_mappings WHERE srvname='order_db_srv'
                                             AND umuser=(SELECT usesysid FROM pg_user WHERE usename=u)
        ) THEN
            EXECUTE 'DROP USER MAPPING FOR ' || quote_ident(u) || ' SERVER order_db_srv';
        END IF;

        EXECUTE '
    CREATE USER MAPPING FOR ' || quote_ident(u) || '
    SERVER order_db_srv
    OPTIONS (user ''postgres'', password ''postgres'')  -- 환경에 맞게
  ';
    END $$;

CREATE SCHEMA IF NOT EXISTS ext_order;

-- 필요한 테이블만 IMPORT
IMPORT FOREIGN SCHEMA public
    LIMIT TO (p_orders, p_reviews)
    FROM SERVER order_db_srv
    INTO ext_order;
