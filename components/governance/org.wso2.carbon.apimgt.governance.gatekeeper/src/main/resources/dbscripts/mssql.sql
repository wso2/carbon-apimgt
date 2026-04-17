-- ================================================================================
-- Microsoft SQL Server Migration Script for API MinHash Table (AM_API_MINHASH)
-- Part of Project 547: Intelligent API Sprawl Prevention & Automated Lifecycle Governance
-- Phase 1: Local Gatekeeper for API Deduplication
-- ================================================================================

IF NOT EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[dbo].[AM_API_MINHASH]') AND type in (N'U'))
BEGIN
    CREATE TABLE AM_API_MINHASH (
        API_UUID VARCHAR(36) NOT NULL,
        SIGNATURE_BLOB VARBINARY(MAX) NOT NULL,
        ORGANIZATION VARCHAR(128) NOT NULL,
        CREATED_TIME DATETIME2 NOT NULL DEFAULT GETDATE(),
        UPDATED_TIME DATETIME2 NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_AM_API_MINHASH PRIMARY KEY (API_UUID, ORGANIZATION)
    );

    CREATE INDEX IDX_AM_API_MINHASH_ORG ON AM_API_MINHASH(ORGANIZATION);
END
GO

-- Trigger to update UPDATED_TIME on row modification
IF EXISTS (SELECT * FROM sys.triggers WHERE object_id = OBJECT_ID(N'[dbo].[TRG_AM_API_MINHASH_UPD]'))
    DROP TRIGGER [dbo].[TRG_AM_API_MINHASH_UPD]
GO

CREATE TRIGGER TRG_AM_API_MINHASH_UPD
ON AM_API_MINHASH
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    UPDATE AM_API_MINHASH
    SET UPDATED_TIME = GETDATE()
    FROM AM_API_MINHASH t
    INNER JOIN inserted i ON t.API_UUID = i.API_UUID AND t.ORGANIZATION = i.ORGANIZATION;
END
GO
