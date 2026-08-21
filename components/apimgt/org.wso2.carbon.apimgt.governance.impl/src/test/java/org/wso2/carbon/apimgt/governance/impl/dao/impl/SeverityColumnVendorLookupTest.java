/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.dao.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.governance.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.APIMGovernanceConfigDTO;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Tests that the optional compliance affecting severity column is looked for correctly on every database the
 * product supports.
 * <p>
 * The column is found through {@link DatabaseMetaData#getColumns}, and the three arguments that decide whether it
 * is found at all behave differently on each vendor: the catalog, the schema, and the case an unquoted table name
 * is stored in. Standing up six databases to check three arguments is neither necessary nor something a build can
 * be asked to do, so each vendor is represented here by the metadata contract its driver documents. Getting one of
 * these wrong does not fail loudly, it silently reports the feature as unavailable on that vendor and every policy
 * quietly keeps counting every severity.
 * <p>
 * The same assertions can be run against a real database of any vendor with
 * {@link SeverityColumnLiveDatabaseTest}, which is how the contracts asserted here were confirmed for H2 and
 * PostgreSQL.
 */
public class SeverityColumnVendorLookupTest {

    private static final String COLUMN = "COMPLIANCE_AFFECTING_SEVERITIES";
    private static final String TABLE = "GOV_POLICY";

    /**
     * The metadata contract of one deployment, as its JDBC driver reports it.
     */
    private static final class Vendor {

        private final String name;
        private final boolean storesLowerCaseIdentifiers;
        private final String catalog;
        private final String schema;

        /** Spelling the driver reports the column under, which follows the case it stores identifiers in. */
        private final String reportedColumnName;

        /** Spelling the table name has to be asked for, which is what this test is really checking. */
        private final String expectedTableLookup;

        private Vendor(String name, boolean storesLowerCaseIdentifiers, String catalog, String schema,
                       String reportedColumnName, String expectedTableLookup) {

            this.name = name;
            this.storesLowerCaseIdentifiers = storesLowerCaseIdentifiers;
            this.catalog = catalog;
            this.schema = schema;
            this.reportedColumnName = reportedColumnName;
            this.expectedTableLookup = expectedTableLookup;
        }
    }

    /**
     * Every database the product supports, plus the three MySQL identifier modes, which behave differently enough
     * to count as separate vendors for this purpose.
     */
    private static final Vendor[] VENDORS = {

            // H2, Oracle and DB2 fold unquoted identifiers to upper case, which is what the SQL standard asks for.
            new Vendor("H2", false, "WSO2AM_DB", "PUBLIC", COLUMN, TABLE),

            // Oracle reports no catalog at all. A null catalog with a real schema is still narrowed enough, which
            // is why the schema is what matters most on the vendors that hold many of them in one instance.
            new Vendor("Oracle", false, null, "APIMGMT", COLUMN, TABLE),
            new Vendor("DB2", false, "APIMDB", "APIMGMT", COLUMN, TABLE),

            // PostgreSQL folds to lower case, so both the table asked for and the column reported come back in
            // lower case. A case sensitive comparison would find nothing here while working everywhere else.
            new Vendor("PostgreSQL", true, "apimdb", "public", "compliance_affecting_severities",
                    "gov_policy"),

            // MySQL has no schemas, only catalogs, and its identifier handling depends on lower_case_table_names.
            // 0 stores names as created, 1 folds them to lower case, and 2 stores as created but compares
            // insensitively, which is the default on macOS.
            new Vendor("MySQL lower_case_table_names=0", false, "apimdb", null, COLUMN, TABLE),
            new Vendor("MySQL lower_case_table_names=1", true, "apimdb", null,
                    "compliance_affecting_severities", "gov_policy"),
            new Vendor("MySQL lower_case_table_names=2", false, "apimdb", null, COLUMN, TABLE),

            // SQL Server preserves the case identifiers were created in and compares them by the collation of the
            // database, so the table created in upper case has to be asked for in upper case.
            new Vendor("SQL Server", false, "apimdb", "dbo", COLUMN, TABLE),
    };

    /**
     * Build a connection whose metadata behaves the way the given vendor's driver does
     *
     * @param vendor          Vendor to imitate
     * @param reportedColumns Columns the driver should report for GOV_POLICY
     * @return Mocked connection
     * @throws SQLException Never, declared because the mocked methods do
     */
    private Connection connectionFor(Vendor vendor, String... reportedColumns) throws SQLException {

        ResultSet columns = Mockito.mock(ResultSet.class);
        Boolean[] remaining = new Boolean[reportedColumns.length];
        for (int i = 0; i < reportedColumns.length; i++) {
            remaining[i] = true;
        }
        if (reportedColumns.length == 0) {
            Mockito.when(columns.next()).thenReturn(false);
        } else {
            Mockito.when(columns.next()).thenReturn(true, remaining).thenReturn(false);
            String first = reportedColumns[0];
            String[] rest = new String[reportedColumns.length - 1];
            System.arraycopy(reportedColumns, 1, rest, 0, rest.length);
            Mockito.when(columns.getString("COLUMN_NAME")).thenReturn(first, rest);
        }

        DatabaseMetaData metaData = Mockito.mock(DatabaseMetaData.class);
        Mockito.when(metaData.storesLowerCaseIdentifiers()).thenReturn(vendor.storesLowerCaseIdentifiers);
        Mockito.when(metaData.getColumns(Mockito.any(), Mockito.any(), Mockito.anyString(), Mockito.any()))
                .thenReturn(columns);

        Connection connection = Mockito.mock(Connection.class);
        Mockito.when(connection.getMetaData()).thenReturn(metaData);
        Mockito.when(connection.getCatalog()).thenReturn(vendor.catalog);
        Mockito.when(connection.getSchema()).thenReturn(vendor.schema);
        return connection;
    }

    /**
     * Point the governance component at a configuration which enables per policy severity filtering
     */
    private void enableFeature() {

        APIMGovernanceConfigDTO governanceConfig = new APIMGovernanceConfigDTO();
        governanceConfig.setPerPolicySeverityFilteringEnabled(true);

        APIManagerConfiguration configuration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(configuration.getAPIMGovernanceConfigurationDto()).thenReturn(governanceConfig);

        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(configuration);

        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(configurationService);
    }

    /**
     * Forget the cached answer, so each vendor is decided on its own metadata
     */
    private void clearCache() throws Exception {

        Field cached = GovernancePolicyMgtDAOImpl.class.getDeclaredField("policySeverityColumnPresent");
        cached.setAccessible(true);
        cached.set(null, null);
    }

    @After
    public void tearDown() throws Exception {

        clearCache();
        ServiceReferenceHolder.getInstance().setAPIMConfigurationService(null);
    }

    @Test
    public void testTheColumnIsFoundOnEveryVendor() throws Exception {

        enableFeature();
        for (Vendor vendor : VENDORS) {
            clearCache();
            Connection connection = connectionFor(vendor, vendor.reportedColumnName);

            Assert.assertTrue("The column must be detected on " + vendor.name,
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testTheTableIsAskedForInTheCaseTheVendorStoresIt() throws Exception {

        enableFeature();
        for (Vendor vendor : VENDORS) {
            clearCache();
            Connection connection = connectionFor(vendor, vendor.reportedColumnName);
            GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection);

            ArgumentCaptor<String> table = ArgumentCaptor.forClass(String.class);
            Mockito.verify(connection.getMetaData()).getColumns(Mockito.any(), Mockito.any(), table.capture(),
                    Mockito.any());

            Assert.assertEquals("The table name must be spelled the way " + vendor.name + " stores it",
                    vendor.expectedTableLookup, table.getValue());
        }
    }

    @Test
    public void testTheLookupIsNarrowedToTheCatalogAndSchemaOfTheConnection() throws Exception {

        // A null catalog and schema pair searches every schema the login can see, so a second deployment or a
        // copied schema on the same Oracle or DB2 instance would be reported as this one having the column. The
        // queries would then fail at runtime rather than falling back to counting every severity.
        enableFeature();
        for (Vendor vendor : VENDORS) {
            clearCache();
            Connection connection = connectionFor(vendor, vendor.reportedColumnName);
            GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection);

            ArgumentCaptor<String> catalog = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> schema = ArgumentCaptor.forClass(String.class);
            Mockito.verify(connection.getMetaData()).getColumns(catalog.capture(), schema.capture(),
                    Mockito.anyString(), Mockito.any());

            Assert.assertEquals("The lookup on " + vendor.name + " must be narrowed to the catalog of the "
                    + "connection", vendor.catalog, catalog.getValue());
            Assert.assertEquals("The lookup on " + vendor.name + " must be narrowed to the schema of the "
                    + "connection", vendor.schema, schema.getValue());
        }
    }

    @Test
    public void testTheColumnIsNotReportedOnAnyVendorWhenItHasNotBeenAdded() throws Exception {

        // The other columns of GOV_POLICY are reported instead, which is what a deployment that has not run the
        // ALTER TABLE looks like. Reporting the feature as available here would make every write fail.
        enableFeature();
        for (Vendor vendor : VENDORS) {
            clearCache();
            Connection connection = connectionFor(vendor, "POLICY_ID", "NAME", "ORGANIZATION");

            Assert.assertFalse("A deployment which has not added the column must report the feature as "
                            + "unavailable on " + vendor.name,
                    GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
        }
    }

    @Test
    public void testTheColumnIsFoundAmongTheOtherColumnsOfTheTable() throws Exception {

        // Drivers report every column of the table, and the one being looked for is rarely the first row.
        enableFeature();
        Connection connection = connectionFor(VENDORS[0], "POLICY_ID", "NAME", "ORGANIZATION", COLUMN);

        Assert.assertTrue("The column must be found wherever the driver reports it in the row order",
                GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
    }

    @Test
    public void testADriverWhichCannotReportItsCatalogFallsBackToSearchingEveryCatalog() throws Exception {

        // Narrowing is an improvement, not a requirement. A driver which refuses the call must leave the feature
        // detectable rather than break it, so the fallback is the old unnarrowed search.
        enableFeature();
        Connection connection = connectionFor(VENDORS[0], COLUMN);
        Mockito.when(connection.getCatalog()).thenThrow(new SQLException("not supported by this driver"));

        Assert.assertTrue(GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));

        ArgumentCaptor<String> catalog = ArgumentCaptor.forClass(String.class);
        Mockito.verify(connection.getMetaData()).getColumns(catalog.capture(), Mockito.any(), Mockito.anyString(),
                Mockito.any());
        Assert.assertNull("A driver which cannot answer must leave the catalog unnarrowed", catalog.getValue());
    }

    @Test
    public void testADriverCompiledAgainstAnOlderJdbcFallsBackRatherThanFailing() throws Exception {

        // getSchema arrived in JDBC 4.1. A driver built against an earlier interface raises AbstractMethodError,
        // which is an Error rather than an Exception and would otherwise escape uncaught.
        enableFeature();
        Connection connection = connectionFor(VENDORS[0], COLUMN);
        Mockito.when(connection.getSchema()).thenThrow(new AbstractMethodError("getSchema"));

        Assert.assertTrue("An old driver must not stop the feature being detected",
                GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));

        ArgumentCaptor<String> schema = ArgumentCaptor.forClass(String.class);
        Mockito.verify(connection.getMetaData()).getColumns(Mockito.any(), schema.capture(), Mockito.anyString(),
                Mockito.any());
        Assert.assertNull("A driver which cannot answer must leave the schema unnarrowed", schema.getValue());
    }

    @Test
    public void testADriverWhichRefusesToReportItsSchemaFallsBackRatherThanFailing() throws Exception {

        // Some drivers implement getSchema only to throw, which is permitted for optional features.
        enableFeature();
        Connection connection = connectionFor(VENDORS[0], COLUMN);
        Mockito.when(connection.getSchema()).thenThrow(new UnsupportedOperationException("getSchema"));

        Assert.assertTrue(GovernancePolicyMgtDAOImpl.isComplianceAffectingSeverityColumnPresent(connection));
    }
}
