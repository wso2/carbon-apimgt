-- ================================================================================
-- Oracle Migration Script for API MinHash Table (AM_API_MINHASH)
-- Part of Project 547: Intelligent API Sprawl Prevention & Automated Lifecycle Governance
-- Phase 1: Local Gatekeeper for API Deduplication
-- ================================================================================

CREATE TABLE AM_API_MINHASH (
    API_UUID VARCHAR2(36) NOT NULL,
    SIGNATURE_BLOB BLOB NOT NULL,
    ORGANIZATION VARCHAR2(128) NOT NULL,
    CREATED_TIME TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    UPDATED_TIME TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,
    CONSTRAINT PK_AM_API_MINHASH PRIMARY KEY (API_UUID, ORGANIZATION)
)
/

CREATE INDEX IDX_AM_API_MINHASH_ORG ON AM_API_MINHASH(ORGANIZATION)
/

-- Trigger to update UPDATED_TIME on row modification
CREATE OR REPLACE TRIGGER TRG_AM_API_MINHASH_UPD
    BEFORE UPDATE ON AM_API_MINHASH
    FOR EACH ROW
BEGIN
    :NEW.UPDATED_TIME := SYSTIMESTAMP;
END;
/
