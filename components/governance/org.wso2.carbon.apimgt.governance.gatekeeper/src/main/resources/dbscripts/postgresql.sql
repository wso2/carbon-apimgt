-- ================================================================================
-- PostgreSQL Migration Script for API MinHash Table (AM_API_MINHASH)
-- Part of Project 547: Intelligent API Sprawl Prevention & Automated Lifecycle Governance
-- Phase 1: Local Gatekeeper for API Deduplication
-- ================================================================================

CREATE TABLE IF NOT EXISTS AM_API_MINHASH (
    API_UUID VARCHAR(36) NOT NULL,
    SIGNATURE_BLOB BYTEA NOT NULL,
    ORGANIZATION VARCHAR(128) NOT NULL,
    CREATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (API_UUID, ORGANIZATION)
);

CREATE INDEX IF NOT EXISTS IDX_AM_API_MINHASH_ORG ON AM_API_MINHASH(ORGANIZATION);

-- Function to update UPDATED_TIME on row update
CREATE OR REPLACE FUNCTION update_am_api_minhash_updated_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.UPDATED_TIME = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update UPDATED_TIME
DROP TRIGGER IF EXISTS trg_am_api_minhash_updated_time ON AM_API_MINHASH;
CREATE TRIGGER trg_am_api_minhash_updated_time
    BEFORE UPDATE ON AM_API_MINHASH
    FOR EACH ROW
    EXECUTE FUNCTION update_am_api_minhash_updated_time();
