/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.spec.parser.definitions;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link OASParserUtil#dedupKey(String)} — the visited-set normalization that decides whether the
 * remote $ref crawl treats two URLs as the same node (skip the second) or distinct nodes (validate + crawl both).
 *
 * <p>The key must: (a) case-fold ONLY scheme + authority so case-flips on the host can't mint endless "new" nodes;
 * (b) preserve path AND query verbatim so two distinct documents are never collapsed (which would leave the
 * second's nested $refs unscanned — an under-validation vector); (c) fall back to the exact string on an
 * unparseable URL so it never under-dedups.</p>
 */
public class OASRefDedupKeyTest {

    /**
     * Regression: a PATH-LESS URL with a query must keep the query in the key. Before the fix the authority was
     * split on the first '/' only, so a query-only URL (no path) dropped the query and two distinct documents
     * collapsed to one key — the second was never fetched and its nested $refs went unvalidated.
     */
    @Test
    public void pathLessQueryOnlyUrlsStayDistinct() {
        String a = OASParserUtil.dedupKey("https://api.example.com?doc=public");
        String b = OASParserUtil.dedupKey("https://api.example.com?doc=internal");
        assertNotEquals("path-less URLs differing only by query must not collapse", a, b);
        assertEquals("https://api.example.com?doc=public", a);
        assertEquals("https://api.example.com?doc=internal", b);
    }

    /** Scheme and authority (host[:port], userinfo) are case-folded, so trivial case-flips collapse (intended). */
    @Test
    public void schemeAndAuthorityAreCaseFolded() {
        assertEquals(OASParserUtil.dedupKey("http://host.com/A"),
                OASParserUtil.dedupKey("HTTP://Host.com/A"));
        assertEquals(OASParserUtil.dedupKey("https://HOST:8080/p"),
                OASParserUtil.dedupKey("https://host:8080/p"));
        assertEquals("https://host:8080/p", OASParserUtil.dedupKey("https://HOST:8080/p"));
    }

    /** Path case is preserved: /A and /a are different resources on the origin and must NOT collapse. */
    @Test
    public void pathCaseIsPreserved() {
        assertNotEquals(OASParserUtil.dedupKey("http://h/A"),
                OASParserUtil.dedupKey("http://h/a"));
    }

    /** Query case (and query value) is preserved when a path is present — unchanged by the fix. */
    @Test
    public void queryIsPreservedWithPath() {
        assertNotEquals(OASParserUtil.dedupKey("http://h/p?X=1"),
                OASParserUtil.dedupKey("http://h/p?x=1"));
        assertEquals("http://h/p?token=abc", OASParserUtil.dedupKey("http://h/p?token=abc"));
    }

    /** A URL with neither path nor query is normalized to scheme://authority (no spurious trailing chars). */
    @Test
    public void bareAuthorityNormalizes() {
        assertEquals("http://host.com", OASParserUtil.dedupKey("HTTP://Host.com"));
        assertEquals(OASParserUtil.dedupKey("http://host.com"),
                OASParserUtil.dedupKey("HTTP://HOST.COM"));
    }

    /** A fragment (if one ever reaches dedupKey) does not cause the query to be dropped on a path-less URL. */
    @Test
    public void fragmentDoesNotDropQuery() {
        // authority ends at '?', query kept
        assertNotEquals(OASParserUtil.dedupKey("http://h?q=a#frag"),
                OASParserUtil.dedupKey("http://h?q=b#frag"));
    }

    /** Unparseable URL: fall back to the exact (case-sensitive) string, so we never under-dedup. */
    @Test
    public void unparseableUrlFallsBackToExactString() {
        String raw = "http://exa mple.com/a";   // space → unparseable
        assertEquals(raw, OASParserUtil.dedupKey(raw));
        assertNotEquals(OASParserUtil.dedupKey("http://exa mple.com/A"),
                OASParserUtil.dedupKey("http://exa mple.com/a"));
    }
}
