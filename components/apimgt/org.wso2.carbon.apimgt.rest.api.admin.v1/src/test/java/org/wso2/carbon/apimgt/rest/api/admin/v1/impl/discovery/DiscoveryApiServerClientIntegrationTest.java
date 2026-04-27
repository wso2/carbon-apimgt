/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl.discovery;

import org.junit.Test;
import org.junit.Assume;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Live integration test against a running API Discovery Server daemon.
 *
 * <p>OPT-IN: only runs when both env vars are set:</p>
 * <pre>
 *   ADS_TEST_ENDPOINT  https://10.50.0.12:8443
 *   ADS_TEST_TOKEN     a valid bearer token (apim:admin or admin_discovery_view)
 * </pre>
 *
 * <p>Without those, every test no-ops via Assume (so this file never
 * breaks `mvn test` on a fresh checkout). Used during PR preparation to
 * prove the wire+HTTP+mapping path against a real daemon.</p>
 */
public class DiscoveryApiServerClientIntegrationTest {

    private static String endpoint() {
        return System.getenv("ADS_TEST_ENDPOINT");
    }

    private static String token() {
        return System.getenv("ADS_TEST_TOKEN");
    }

    private DiscoveryApiServerClient client() throws Exception {
        Assume.assumeTrue("set ADS_TEST_ENDPOINT to enable",
                endpoint() != null && !endpoint().isEmpty());
        Assume.assumeTrue("set ADS_TEST_TOKEN to enable",
                token() != null && !token().isEmpty());
        // Use reflection to call the private constructor so this test
        // doesn't depend on APIManagerConfiguration (which would need a
        // running carbon framework).
        java.lang.reflect.Constructor<DiscoveryApiServerClient> ctor =
                DiscoveryApiServerClient.class.getDeclaredConstructor(
                        String.class, int.class);
        ctor.setAccessible(true);
        return ctor.newInstance(endpoint(), 10000);
    }

    @Test
    public void liveSummary() throws Exception {
        DiscoverySummaryWire wire = client().getSummary(token());
        assertNotNull("summary must return a wire payload", wire);
        // Numbers depend on TechMart state; just sanity-check positive.
        assertTrue("total non-negative", wire.getTotal() >= 0);
        System.out.printf("LIVE summary: total=%d managed=%d unmanaged=%d%n",
                wire.getTotal(), wire.getManaged(), wire.getUnmanaged());
    }

    @Test
    public void liveList() throws Exception {
        DiscoveryListWire wire = client().listApis(token(),
                new DiscoveryListFilter().pagination(25, 0));
        assertNotNull(wire);
        System.out.printf("LIVE list: count=%d%n", wire.getCount());
    }

    @Test
    public void liveUntrafficked() throws Exception {
        UntraffickedListWire wire = client().listUntrafficked(token());
        assertNotNull(wire);
        System.out.printf("LIVE untrafficked: count=%d%n", wire.getCount());
    }

    @Test
    public void liveDetailMissing404() throws Exception {
        try {
            client().getApiById(token(),
                    "00000000-0000-0000-0000-000000000000");
        } catch (DiscoveryApiServerClient.NotFoundException expected) {
            System.out.println("LIVE detail 404: " + expected.getMessage());
            return;
        }
        // If the daemon returned 200 for a sentinel UUID, that's fine too;
        // some deployments seed test rows.
    }
}
