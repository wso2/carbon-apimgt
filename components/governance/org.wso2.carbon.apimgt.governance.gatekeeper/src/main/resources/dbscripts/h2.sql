-- ================================================================================
-- SQL Migration Script for API MinHash Table (AM_API_MINHASH)
-- Part of Project 547: Intelligent API Sprawl Prevention & Automated Lifecycle Governance
-- Phase 1: Local Gatekeeper for API Deduplication
-- ================================================================================
-- 
-- Purpose: Creates the AM_API_MINHASH table to persist MinHash signatures for APIs.
-- The signatures are used for efficient similarity detection using LSH (Locality
-- Sensitive Hashing) to prevent API sprawl by detecting duplicate APIs.
--
-- Table Design:
-- - API_UUID: Foreign key reference to the API
-- - SIGNATURE_BLOB: Binary storage of the MinHash signature (128 x 4 = 512 bytes default)
-- - ORGANIZATION: Multi-tenancy support
-- - CREATED_TIME: When the signature was first generated
-- - UPDATED_TIME: When the signature was last updated (for API updates)
--
-- ================================================================================

-- H2 Database
CREATE TABLE IF NOT EXISTS AM_API_MINHASH (
    API_UUID VARCHAR(36) NOT NULL,
    SIGNATURE_BLOB BLOB NOT NULL,
    ORGANIZATION VARCHAR(128) NOT NULL,
    CREATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (API_UUID, ORGANIZATION)
);

CREATE INDEX IDX_AM_API_MINHASH_ORG ON AM_API_MINHASH(ORGANIZATION);
