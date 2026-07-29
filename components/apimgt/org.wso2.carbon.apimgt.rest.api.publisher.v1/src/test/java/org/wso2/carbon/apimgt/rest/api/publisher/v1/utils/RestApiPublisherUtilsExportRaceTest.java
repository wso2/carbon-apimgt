/*
 *  Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils;

import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Regression test for the concurrent operation-policy export race, validating PER-INVOCATION CONTENT.
 * <p>
 * Many threads export the SAME policy identity ({@code name_version}) concurrently but each with DISTINCT
 * content (a unique marker embedded in the definition) via the request-scoped
 * {@link RestApiPublisherUtils#exportOperationPolicyData(OperationPolicyData, String, boolean)} with
 * {@code requestScoped = true}. Each invocation must get back its OWN,
 * complete archive carrying ITS marker.
 * <p>
 * With the per-request fix (each export in its own private working root) this passes. Against the shared,
 * identity-keyed publish (the old {@code exportOperationPolicyData} / pre-fix behaviour), concurrent exports
 * overwrite the one {@code ${tmpdir}/<name>_<version>.zip} file, so a thread reads a sibling's content — the
 * marker assertion then fails, which is exactly the cross-contamination this test guards against.
 */
public class RestApiPublisherUtilsExportRaceTest {

    private static final String POLICY_NAME = "RaceTestPolicy";
    private static final String POLICY_VERSION = "v1";
    private static final int THREADS = 16;
    private static final int ROUNDS = 6;

    @Test(timeout = 120000)
    public void concurrentSamePolicyExportsPreservePerInvocationContent() throws Exception {

        for (int round = 0; round < ROUNDS; round++) {
            ExecutorService pool = Executors.newFixedThreadPool(THREADS);
            CountDownLatch startGate = new CountDownLatch(1);
            List<Future<Export>> futures = new ArrayList<>();
            for (int i = 0; i < THREADS; i++) {
                final String marker = "PAYLOAD-MARKER-r" + round + "-t" + i + "-UNIQUE";
                futures.add(pool.submit((Callable<Export>) () -> {
                    startGate.await();
                    OperationPolicyData policyData = buildPolicyData(marker);
                    File archive = RestApiPublisherUtils.exportOperationPolicyData(policyData, "YAML", true);
                    return new Export(marker, archive);
                }));
            }
            // Release every thread at once so they contend for the same policy identity simultaneously.
            startGate.countDown();

            List<Export> exports = new ArrayList<>();
            for (Future<Export> future : futures) {
                try {
                    exports.add(future.get());
                } catch (Exception e) {
                    pool.shutdownNow();
                    fail("A concurrent export failed (round " + round + "): " + rootCause(e));
                }
            }
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);

            try {
                for (Export export : exports) {
                    File archive = export.archive;
                    assertTrue("Exported archive is missing or empty: " + archive,
                            archive != null && archive.isFile() && archive.length() > 0);
                    assertTrue("Exported archive is corrupt/incomplete - policy specification entry missing: "
                                    + archive,
                            zipContainsEntrySuffix(archive, ".yaml"));
                    // Per-invocation content: each archive must carry ITS OWN payload marker, never a sibling's.
                    assertTrue("Cross-contamination: archive " + archive + " does not contain its own payload marker "
                                    + export.marker,
                            zipContainsText(archive, export.marker));
                }
            } finally {
                // The per-request method hands back an archive under a caller-owned unique parent; delete it.
                for (Export export : exports) {
                    if (export.archive != null) {
                        deleteRecursively(export.archive.getParentFile());
                    }
                }
            }
        }
    }

    private OperationPolicyData buildPolicyData(String marker) {

        OperationPolicySpecification specification = new OperationPolicySpecification();
        specification.setName(POLICY_NAME);
        specification.setVersion(POLICY_VERSION);
        specification.setDisplayName(POLICY_NAME);
        specification.setDescription("policy used by the concurrent-export race regression test");

        // Distinct, sizeable definition per invocation: the unique marker plus bulk to widen the write/zip window.
        StringBuilder content = new StringBuilder();
        content.append("<marker>").append(marker).append("</marker>\n");
        for (int i = 0; i < 8000; i++) {
            content.append("<sequence>").append(marker).append("-line-").append(i).append("</sequence>\n");
        }
        OperationPolicyDefinition synapseDefinition = new OperationPolicyDefinition();
        synapseDefinition.setContent(content.toString());

        OperationPolicyData policyData = new OperationPolicyData();
        policyData.setSpecification(specification);
        policyData.setSynapsePolicyDefinition(synapseDefinition);
        return policyData;
    }

    private static boolean zipContainsEntrySuffix(File zip, String suffix) {
        try (ZipFile zipFile = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                if (entries.nextElement().getName().endsWith(suffix)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            // A ZipException here means the archive was truncated/overwritten by a concurrent export.
            return false;
        }
    }

    private static boolean zipContainsText(File zip, String text) {
        try (ZipFile zipFile = new ZipFile(zip)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                try (InputStream in = zipFile.getInputStream(entry)) {
                    String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
                    if (content.contains(text)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable cause = throwable;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }

    private static void deleteRecursively(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }

    /** Pairs an invocation's unique payload marker with the archive it produced. */
    private static final class Export {
        private final String marker;
        private final File archive;

        private Export(String marker, File archive) {
            this.marker = marker;
            this.archive = archive;
        }
    }
}
