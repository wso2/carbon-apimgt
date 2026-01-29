-- ================================================================================
-- MySQL Migration Script for API MinHash Table (AM_API_MINHASH)
-- Part of Project 547: Intelligent API Sprawl Prevention & Automated Lifecycle Governance
-- Phase 1: Local Gatekeeper for API Deduplication
-- ================================================================================

CREATE TABLE IF NOT EXISTS AM_API_MINHASH (
    API_UUID VARCHAR(36) NOT NULL,
    SIGNATURE_BLOB BLOB NOT NULL,
    ORGANIZATION VARCHAR(128) NOT NULL,
    CREATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UPDATED_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (API_UUID, ORGANIZATION),
    INDEX IDX_AM_API_MINHASH_ORG (ORGANIZATION)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
