/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dao.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiKeyMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.namespace.QName;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ApiKeyMgtDAO#getAllAPIKeys(String)}.
 *
 * Regression coverage for issue #4952: on Oracle 19c the original
 * GET_ALL_API_KEYS_SQL projected the BLOB column API_KEY_PROPERTIES inside three
 * UNION ALL branches, which Oracle rejects with ORA-00932. The rewritten query
 * pulls the BLOB out of the union and joins it back via API_KEY_UUID. These
 * tests exercise the same three-branch shape (API-only, App-only, Both), with
 * non-null and large BLOBs, and confirm tenant isolation and INACTIVE filtering.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({KeyManagerHolder.class})
@PowerMockIgnore("javax.management.*")
public class ApiKeyMgtDAOTest {

    private static final String TENANT = "issue4952tenant";
    private static final String OTHER_TENANT = "issue4952tenant.other";

    private static final String API_UUID_A = "issue4952-api-a";
    private static final String API_UUID_B = "issue4952-api-b";
    private static final String API_UUID_OTHER = "issue4952-api-other";

    private static final String APP_UUID_A = "issue4952-app-a";
    private static final String APP_UUID_OTHER = "issue4952-app-other";

    private static final String KEY_UUID_API_ONLY = "issue4952-key-api-only";
    private static final String KEY_UUID_APP_ONLY = "issue4952-key-app-only";
    private static final String KEY_UUID_BOTH = "issue4952-key-both";
    private static final String KEY_UUID_NULL_PROPS = "issue4952-key-null-props";
    private static final String KEY_UUID_INACTIVE = "issue4952-key-inactive";
    private static final String KEY_UUID_LARGE = "issue4952-key-large";
    private static final String KEY_UUID_OTHER_TENANT = "issue4952-key-other-tenant";

    private ApiKeyMgtDAO apiKeyMgtDAO;

    @Before
    public void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        initializeDatabase(dbConfigPath);
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(
                new APIManagerConfigurationServiceImpl(config));
        APIMgtDBUtil.initialize();
        apiKeyMgtDAO = ApiKeyMgtDAO.getInstance();
        cleanFixtures();
    }

    @After
    public void tearDown() throws Exception {
        cleanFixtures();
    }

    /**
     * Bug scenario (#4952): three union branches each return a key with a
     * populated BLOB. On Oracle 19c the old query failed with ORA-00932 here.
     */
    @Test
    public void getAllAPIKeys_returnsAllThreeBranchesWithProperties() throws Exception {
        seedApi(API_UUID_A, "Issue4952ApiA", TENANT);
        seedApi(API_UUID_B, "Issue4952ApiB", TENANT);
        seedApplication(APP_UUID_A, "Issue4952AppA", TENANT);

        Map<String, String> propsApi = new LinkedHashMap<>();
        propsApi.put("permittedReferer", "");
        propsApi.put("permittedIP", "");
        seedApiKey(KEY_UUID_API_ONLY, "key-api-only", "PRODUCTION", propsApi, "ACTIVE");
        seedApiKeyApiMapping(KEY_UUID_API_ONLY, API_UUID_A);

        Map<String, String> propsApp = new LinkedHashMap<>();
        propsApp.put("foo", "bar");
        seedApiKey(KEY_UUID_APP_ONLY, "key-app-only", "SANDBOX", propsApp, "ACTIVE");
        seedApiKeyAppMapping(KEY_UUID_APP_ONLY, APP_UUID_A);

        Map<String, String> propsBoth = new LinkedHashMap<>();
        propsBoth.put("a", "1");
        propsBoth.put("b", "2");
        seedApiKey(KEY_UUID_BOTH, "key-both", "PRODUCTION", propsBoth, "ACTIVE");
        seedApiKeyApiMapping(KEY_UUID_BOTH, API_UUID_B);
        seedApiKeyAppMapping(KEY_UUID_BOTH, APP_UUID_A);

        List<APIKeyInfo> result = apiKeyMgtDAO.getAllAPIKeys(TENANT);
        Map<String, APIKeyInfo> byUuid = indexByUuid(result);

        // All three branches should be represented exactly once.
        assertNotNull("Branch 1 (API-only) row missing", byUuid.get(KEY_UUID_API_ONLY));
        assertNotNull("Branch 2 (App-only) row missing", byUuid.get(KEY_UUID_APP_ONLY));
        assertNotNull("Branch 3 (Both) row missing", byUuid.get(KEY_UUID_BOTH));
        assertEquals("Should be exactly three rows for tenant '" + TENANT + "'", 3,
                countForTenant(result));

        APIKeyInfo apiOnly = byUuid.get(KEY_UUID_API_ONLY);
        assertEquals("key-api-only", apiOnly.getKeyName());
        assertEquals("PRODUCTION", apiOnly.getKeyType());
        assertEquals("ACTIVE", apiOnly.getStatus());
        assertNull("API-only row should have no APPLICATION_UUID", apiOnly.getApplicationId());
        assertEquals(API_UUID_A, apiOnly.getApiUUId());
        assertEquals(propsApi, apiOnly.getProperties());

        APIKeyInfo appOnly = byUuid.get(KEY_UUID_APP_ONLY);
        assertEquals("SANDBOX", appOnly.getKeyType());
        assertNull("App-only row should have no API_UUID", appOnly.getApiUUId());
        assertEquals(APP_UUID_A, appOnly.getApplicationId());
        assertEquals(propsApp, appOnly.getProperties());

        APIKeyInfo both = byUuid.get(KEY_UUID_BOTH);
        assertEquals(API_UUID_B, both.getApiUUId());
        assertEquals(APP_UUID_A, both.getApplicationId());
        assertEquals(propsBoth, both.getProperties());
    }

    /**
     * Edge case: API_KEY_PROPERTIES is NULL. The DAO's BLOB stream handler must
     * not NPE and APIKeyInfo.properties should remain null.
     */
    @Test
    public void getAllAPIKeys_nullProperties() throws Exception {
        seedApi(API_UUID_A, "Issue4952ApiA", TENANT);
        seedApiKey(KEY_UUID_NULL_PROPS, "key-null-props", "PRODUCTION", null, "ACTIVE");
        seedApiKeyApiMapping(KEY_UUID_NULL_PROPS, API_UUID_A);

        List<APIKeyInfo> result = apiKeyMgtDAO.getAllAPIKeys(TENANT);
        APIKeyInfo info = indexByUuid(result).get(KEY_UUID_NULL_PROPS);

        assertNotNull("Key with NULL properties was not returned", info);
        assertNull("APIKeyInfo.properties should be null when BLOB is NULL", info.getProperties());
        assertEquals("ACTIVE", info.getStatus());
    }

    /**
     * Negative case: an INACTIVE key must be excluded from results (the SQL
     * applies STATUS = 'ACTIVE' both inside the union and in the outer
     * SELECT after the rewrite — both filters must hold).
     */
    @Test
    public void getAllAPIKeys_excludesInactiveKeys() throws Exception {
        seedApi(API_UUID_A, "Issue4952ApiA", TENANT);
        seedApiKey(KEY_UUID_INACTIVE, "key-inactive", "PRODUCTION",
                singleEntryProps("k", "v"), "INACTIVE");
        seedApiKeyApiMapping(KEY_UUID_INACTIVE, API_UUID_A);

        // Plus one active key so we know the query returns rows for this tenant.
        seedApiKey(KEY_UUID_API_ONLY, "key-active", "PRODUCTION",
                singleEntryProps("k", "v"), "ACTIVE");
        seedApiKeyApiMapping(KEY_UUID_API_ONLY, API_UUID_A);

        List<APIKeyInfo> result = apiKeyMgtDAO.getAllAPIKeys(TENANT);
        Map<String, APIKeyInfo> byUuid = indexByUuid(result);

        assertNotNull("Active key should be returned", byUuid.get(KEY_UUID_API_ONLY));
        assertNull("INACTIVE keys must not be returned", byUuid.get(KEY_UUID_INACTIVE));
    }

    /**
     * Negative case: keys belonging to another tenant must not leak into the
     * caller's result. The rewritten outer SELECT joins back to AM_API_KEY by
     * UUID, so a missed tenant filter in any branch would be visible here.
     */
    @Test
    public void getAllAPIKeys_isolatesOtherTenants() throws Exception {
        seedApi(API_UUID_A, "Issue4952ApiA", TENANT);
        seedApi(API_UUID_OTHER, "Issue4952ApiOther", OTHER_TENANT);
        seedApplication(APP_UUID_OTHER, "Issue4952AppOther", OTHER_TENANT);

        seedApiKey(KEY_UUID_API_ONLY, "key-mine", "PRODUCTION",
                singleEntryProps("k", "v"), "ACTIVE");
        seedApiKeyApiMapping(KEY_UUID_API_ONLY, API_UUID_A);

        seedApiKey(KEY_UUID_OTHER_TENANT, "key-theirs", "PRODUCTION",
                singleEntryProps("k", "v"), "ACTIVE");
        seedApiKeyApiMapping(KEY_UUID_OTHER_TENANT, API_UUID_OTHER);
        seedApiKeyAppMapping(KEY_UUID_OTHER_TENANT, APP_UUID_OTHER);

        List<APIKeyInfo> mine = apiKeyMgtDAO.getAllAPIKeys(TENANT);
        Map<String, APIKeyInfo> mineByUuid = indexByUuid(mine);
        assertNotNull("Tenant's own key should be returned", mineByUuid.get(KEY_UUID_API_ONLY));
        assertNull("Other tenant's key must not leak across tenants",
                mineByUuid.get(KEY_UUID_OTHER_TENANT));

        List<APIKeyInfo> theirs = apiKeyMgtDAO.getAllAPIKeys(OTHER_TENANT);
        Map<String, APIKeyInfo> theirsByUuid = indexByUuid(theirs);
        assertNotNull("Other tenant should see its own key",
                theirsByUuid.get(KEY_UUID_OTHER_TENANT));
        assertNull("Tenant boundaries should be symmetric",
                theirsByUuid.get(KEY_UUID_API_ONLY));
    }

    /**
     * Edge case: a large API_KEY_PROPERTIES BLOB (> 8KB). On Oracle this would
     * push the LOB out of row; the rewritten query must still round-trip the
     * value through the outer JOIN.
     */
    @Test
    public void getAllAPIKeys_largeProperties() throws Exception {
        seedApi(API_UUID_A, "Issue4952ApiA", TENANT);

        Map<String, String> large = new LinkedHashMap<>();
        StringBuilder sb = new StringBuilder(9000);
        for (int i = 0; i < 9000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        large.put("blob", sb.toString());

        seedApiKey(KEY_UUID_LARGE, "key-large", "PRODUCTION", large, "ACTIVE");
        seedApiKeyApiMapping(KEY_UUID_LARGE, API_UUID_A);

        List<APIKeyInfo> result = apiKeyMgtDAO.getAllAPIKeys(TENANT);
        APIKeyInfo info = indexByUuid(result).get(KEY_UUID_LARGE);
        assertNotNull("Key with large properties was not returned", info);
        assertNotNull(info.getProperties());
        assertEquals(sb.toString(), info.getProperties().get("blob"));
    }

    // ---------- helpers ----------

    private static Map<String, String> singleEntryProps(String k, String v) {
        Map<String, String> m = new HashMap<>();
        m.put(k, v);
        return m;
    }

    private static Map<String, APIKeyInfo> indexByUuid(List<APIKeyInfo> rows) {
        Map<String, APIKeyInfo> out = new HashMap<>();
        for (APIKeyInfo info : rows) {
            out.put(info.getKeyUUID(), info);
        }
        return out;
    }

    private static int countForTenant(List<APIKeyInfo> rows) {
        // The result is already scoped to the tenant by the SQL; this is a
        // convenience to count rows we asserted on.
        int n = 0;
        for (APIKeyInfo info : rows) {
            if (info.getKeyUUID() != null && info.getKeyUUID().startsWith("issue4952-key-")) {
                n++;
            }
        }
        return n;
    }

    private static void seedApi(String apiUuid, String apiName, String organization)
            throws Exception {
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO AM_API (API_PROVIDER, API_NAME, API_VERSION, CONTEXT, " +
                             "API_UUID, ORGANIZATION) VALUES (?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, "issue4952-provider");
            ps.setString(2, apiName);
            ps.setString(3, "1.0.0");
            ps.setString(4, "/" + apiName);
            ps.setString(5, apiUuid);
            ps.setString(6, organization);
            ps.executeUpdate();
        }
    }

    private static void seedApplication(String appUuid, String appName, String organization)
            throws Exception {
        // Borrow SUBSCRIBER_ID 1 from h2-sample-data.sql (SUMEDHA).
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO AM_APPLICATION (NAME, SUBSCRIBER_ID, UUID, ORGANIZATION) " +
                             "VALUES (?, ?, ?, ?)")) {
            ps.setString(1, appName);
            ps.setInt(2, 1);
            ps.setString(3, appUuid);
            ps.setString(4, organization);
            ps.executeUpdate();
        }
    }

    private static void seedApiKey(String keyUuid, String name, String keyType,
                                   Map<String, String> properties, String status) throws Exception {
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO AM_API_KEY (API_KEY_UUID, NAME, API_KEY_HASH, KEY_TYPE, " +
                             "API_KEY_PROPERTIES, AUTHZ_USER, TIME_CREATED, VALIDITY_PERIOD, " +
                             "STATUS) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, keyUuid);
            ps.setString(2, name);
            ps.setString(3, "hash-" + keyUuid);
            ps.setString(4, keyType);
            if (properties == null) {
                ps.setNull(5, Types.BLOB);
            } else {
                byte[] bytes = new ObjectMapper().writeValueAsBytes(properties);
                ps.setBinaryStream(5, new ByteArrayInputStream(bytes), bytes.length);
            }
            ps.setString(6, "admin");
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            ps.setLong(8, 3600L);
            ps.setString(9, status);
            ps.executeUpdate();
        }
    }

    private static void seedApiKeyApiMapping(String keyUuid, String apiUuid) throws Exception {
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO AM_API_KEY_API_MAPPING (API_KEY_UUID, API_UUID) VALUES (?, ?)")) {
            ps.setString(1, keyUuid);
            ps.setString(2, apiUuid);
            ps.executeUpdate();
        }
    }

    private static void seedApiKeyAppMapping(String keyUuid, String appUuid) throws Exception {
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO AM_API_KEY_APPLICATION_MAPPING (API_KEY_UUID, APPLICATION_UUID) " +
                             "VALUES (?, ?)")) {
            ps.setString(1, keyUuid);
            ps.setString(2, appUuid);
            ps.executeUpdate();
        }
    }

    /**
     * Removes any rows seeded by this test class so tests stay independent of
     * one another and of execution order against the shared H2 instance.
     */
    private static void cleanFixtures() {
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            execIgnore(conn, "DELETE FROM AM_API_KEY_API_MAPPING WHERE API_KEY_UUID LIKE 'issue4952-%'");
            execIgnore(conn, "DELETE FROM AM_API_KEY_APPLICATION_MAPPING WHERE API_KEY_UUID LIKE 'issue4952-%'");
            execIgnore(conn, "DELETE FROM AM_API_KEY WHERE API_KEY_UUID LIKE 'issue4952-%'");
            execIgnore(conn, "DELETE FROM AM_APPLICATION WHERE UUID LIKE 'issue4952-%'");
            execIgnore(conn, "DELETE FROM AM_API WHERE API_UUID LIKE 'issue4952-%'");
        } catch (Exception e) {
            fail("Failed to clean test fixtures: " + e.getMessage());
        }
    }

    private static void execIgnore(Connection conn, String sql) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception ignored) {
            // First-run cleanup may hit constraints if a prior aborted test left
            // partial state — re-run order matters more than first-time errors.
        }
    }

    private static void initializeDatabase(String configFilePath) {
        try {
            InputStream in = FileUtils.openInputStream(new File(configFilePath));
            StAXOMBuilder builder = new StAXOMBuilder(in);
            OMElement databaseElement = builder.getDocumentElement()
                    .getFirstChildWithName(new QName("Database"));
            String databaseURL = databaseElement.getFirstChildWithName(new QName("URL")).getText();
            String databaseUser = databaseElement.getFirstChildWithName(new QName("Username")).getText();
            String databasePass = databaseElement.getFirstChildWithName(new QName("Password")).getText();
            String databaseDriver = databaseElement.getFirstChildWithName(new QName("Driver")).getText();

            BasicDataSource basicDataSource = new BasicDataSource();
            basicDataSource.setDriverClassName(databaseDriver);
            basicDataSource.setUrl(databaseURL);
            basicDataSource.setUsername(databaseUser);
            basicDataSource.setPassword(databasePass);

            System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                    "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
            try {
                InitialContext.doLookup("java:/comp/env/jdbc/WSO2AM_DB");
            } catch (NamingException e) {
                InitialContext ic = new InitialContext();
                ic.createSubcontext("java:");
                ic.createSubcontext("java:/comp");
                ic.createSubcontext("java:/comp/env");
                ic.createSubcontext("java:/comp/env/jdbc");
                ic.bind("java:/comp/env/jdbc/WSO2AM_DB", basicDataSource);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialise test H2 datasource", e);
        }
    }
}
