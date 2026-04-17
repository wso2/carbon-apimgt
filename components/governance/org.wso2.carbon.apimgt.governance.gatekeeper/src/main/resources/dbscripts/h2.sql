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

CREATE INDEX IF NOT EXISTS IDX_AM_API_MINHASH_ORG ON AM_API_MINHASH(ORGANIZATION);

-- ================================================================================
-- Deduplication Alerts Table
-- Stores alerts generated when duplicate APIs are detected.
-- ================================================================================
CREATE TABLE IF NOT EXISTS AM_DEDUP_ALERT (
    ALERT_ID VARCHAR(36) NOT NULL,
    NEW_API_UUID VARCHAR(36) NOT NULL,
    NEW_API_NAME VARCHAR(255) NOT NULL,
    NEW_API_VERSION VARCHAR(50),
    HIGHEST_SIMILARITY DOUBLE NOT NULL,
    SEVERITY VARCHAR(20) NOT NULL,
    STATUS VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    ORGANIZATION VARCHAR(128) NOT NULL,
    MESSAGE TEXT,
    RECOMMENDATION TEXT,
    SIMILAR_APIS_JSON TEXT NOT NULL,
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    RESOLVED_AT TIMESTAMP,
    RESOLVED_BY VARCHAR(255),
    PRIMARY KEY (ALERT_ID)
);

CREATE INDEX IF NOT EXISTS IDX_AM_DEDUP_ALERT_API ON AM_DEDUP_ALERT(NEW_API_UUID);
CREATE INDEX IF NOT EXISTS IDX_AM_DEDUP_ALERT_ORG ON AM_DEDUP_ALERT(ORGANIZATION);
CREATE INDEX IF NOT EXISTS IDX_AM_DEDUP_ALERT_STATUS ON AM_DEDUP_ALERT(STATUS);

-- ================================================================================
-- Deduplication Decisions Table
-- Stores user decisions on deduplication alerts.
-- ================================================================================
CREATE TABLE IF NOT EXISTS AM_DEDUP_DECISION (
    DECISION_ID VARCHAR(36) NOT NULL,
    ALERT_ID VARCHAR(36) NOT NULL,
    NEW_API_UUID VARCHAR(36) NOT NULL,
    EXISTING_API_UUID VARCHAR(36) NOT NULL,
    SIMILARITY_SCORE DOUBLE NOT NULL,
    ACTION VARCHAR(30) NOT NULL,
    JUSTIFICATION TEXT,
    DECIDED_BY VARCHAR(255) NOT NULL,
    DECIDED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ORGANIZATION VARCHAR(128) NOT NULL,
    STATUS VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    PRIMARY KEY (DECISION_ID),
    FOREIGN KEY (ALERT_ID) REFERENCES AM_DEDUP_ALERT(ALERT_ID) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS IDX_AM_DEDUP_DECISION_ALERT ON AM_DEDUP_DECISION(ALERT_ID);
CREATE INDEX IF NOT EXISTS IDX_AM_DEDUP_DECISION_API ON AM_DEDUP_DECISION(NEW_API_UUID);
CREATE INDEX IF NOT EXISTS IDX_AM_DEDUP_DECISION_ORG ON AM_DEDUP_DECISION(ORGANIZATION);
