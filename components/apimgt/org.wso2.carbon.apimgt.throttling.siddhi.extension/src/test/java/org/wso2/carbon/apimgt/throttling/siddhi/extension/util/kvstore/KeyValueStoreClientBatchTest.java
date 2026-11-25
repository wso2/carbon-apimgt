/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.throttling.siddhi.extension.util.kvstore;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Unit tests for the batch API of the key-value store client: the default methods of {@link KeyValueStoreClient}
 * (used by clients written before batching), {@link KeyValueStoreOperation}, and the parts of
 * {@link JedisKeyValueStoreClient} that do not need a Redis server.
 */
public class KeyValueStoreClientBatchTest {

    /** A client written against the interface before batching: only the original single-key methods. */
    private static class SingleKeyOnlyClient implements KeyValueStoreClient {
        final ConcurrentHashMap<String, String> store = new ConcurrentHashMap<>();
        final ConcurrentHashMap<String, Long> ttls = new ConcurrentHashMap<>();
        volatile String failingKey;

        private void check(String key) {
            if (key.equals(failingKey)) {
                throw new KeyValueStoreException("simulated failure for " + key);
            }
        }

        @Override
        public void disconnect() {
        }

        @Override
        public String get(String key) {
            return store.get(key);
        }

        @Override
        public void set(String key, String value) {
            store.put(key, value);
        }

        @Override
        public long incrementBy(String key, long increment) {
            check(key);
            // Long.parseLong throws NumberFormatException for a non-numeric value, like an unexpected runtime error
            return Long.parseLong(store.merge(key, String.valueOf(increment),
                    (a, b) -> String.valueOf(Long.parseLong(a) + Long.parseLong(b))));
        }

        @Override
        public long decrementBy(String key, long decrement) {
            return incrementBy(key, -decrement);
        }

        @Override
        public void setWithExpiry(String key, String value, long ttlMillis) {
            check(key);
            store.put(key, value);
            ttls.put(key, ttlMillis);
        }

        @Override
        public void expireMillis(String key, long ttlMillis) {
            check(key);
            ttls.put(key, ttlMillis);
        }

        @Override
        public void delete(String key) {
            store.remove(key);
        }
    }

    @Test
    public void testExistingClientsDoNotClaimBatchSupport() {
        Assert.assertFalse(new SingleKeyOnlyClient().supportsBatchOperations());
    }

    @Test
    public void testDefaultGetMultipleReadsEachKeyInOrder() {
        SingleKeyOnlyClient client = new SingleKeyOnlyClient();
        client.set("a", "1");
        client.set("c", "3");

        Assert.assertEquals(Arrays.asList("1", null, "3"), client.getMultiple(Arrays.asList("a", "b", "c")));
        Assert.assertEquals(Collections.emptyList(), client.getMultiple(Collections.<String>emptyList()));
    }

    @Test
    public void testDefaultExecuteBatchRunsEachOperationThroughTheSingleKeyMethods() {
        SingleKeyOnlyClient client = new SingleKeyOnlyClient();
        client.set("counter", "10");
        client.set("existing", "5");

        List<Object> replies = client.executeBatch(Arrays.asList(
                KeyValueStoreOperation.incrementBy("counter", 3),
                KeyValueStoreOperation.decrementBy("counter", 1),
                KeyValueStoreOperation.expireMillis("counter", 30000L),
                KeyValueStoreOperation.setWithExpiry("reset", "0", 20000L),
                KeyValueStoreOperation.setIfAbsentWithExpiry("seed", "0", 10000L),
                KeyValueStoreOperation.setIfAbsentWithExpiry("existing", "0", 10000L)));

        Assert.assertEquals(6, replies.size());
        Assert.assertEquals(13L, replies.get(0));
        Assert.assertEquals(12L, replies.get(1));
        for (Object reply : replies.subList(2, 6)) {
            Assert.assertFalse("success is any non-exception reply", reply instanceof Exception);
        }
        Assert.assertEquals("12", client.get("counter"));
        Assert.assertEquals(Long.valueOf(30000L), client.ttls.get("counter"));
        Assert.assertEquals("0", client.get("reset"));
        Assert.assertEquals(Long.valueOf(20000L), client.ttls.get("reset"));
        Assert.assertEquals("SET NX creates a missing key", "0", client.get("seed"));
        Assert.assertEquals("SET NX leaves an existing key alone", "5", client.get("existing"));
    }

    @Test
    public void testDefaultExecuteBatchReportsAFailedOperationInItsOwnReply() {
        SingleKeyOnlyClient client = new SingleKeyOnlyClient();
        client.failingKey = "bad";
        client.set("text", "not-a-number");

        List<Object> replies = client.executeBatch(Arrays.asList(
                KeyValueStoreOperation.incrementBy("bad", 1),
                KeyValueStoreOperation.incrementBy("text", 1),
                KeyValueStoreOperation.incrementBy("good", 2)));

        Assert.assertTrue(replies.get(0) instanceof KeyValueStoreException);
        Assert.assertTrue("an unexpected runtime error is wrapped", replies.get(1) instanceof KeyValueStoreException);
        Assert.assertTrue(((Exception) replies.get(1)).getCause() instanceof NumberFormatException);
        Assert.assertEquals("later operations still run", 2L, replies.get(2));
        Assert.assertEquals("2", client.get("good"));
    }

    @Test
    public void testOperationFactories() {
        KeyValueStoreOperation incr = KeyValueStoreOperation.incrementBy("k", 4);
        Assert.assertEquals(KeyValueStoreOperation.Type.INCRBY, incr.getType());
        Assert.assertEquals("k", incr.getKey());
        Assert.assertEquals(4L, incr.getAmount());

        KeyValueStoreOperation decr = KeyValueStoreOperation.decrementBy("k", 2);
        Assert.assertEquals(KeyValueStoreOperation.Type.DECRBY, decr.getType());
        Assert.assertEquals(2L, decr.getAmount());

        KeyValueStoreOperation expire = KeyValueStoreOperation.expireMillis("k", 1500L);
        Assert.assertEquals(KeyValueStoreOperation.Type.PEXPIRE, expire.getType());
        Assert.assertEquals(1500L, expire.getTtlMillis());

        KeyValueStoreOperation psetex = KeyValueStoreOperation.setWithExpiry("k", "0", 2500L);
        Assert.assertEquals(KeyValueStoreOperation.Type.PSETEX, psetex.getType());
        Assert.assertEquals("0", psetex.getValue());
        Assert.assertEquals(2500L, psetex.getTtlMillis());

        KeyValueStoreOperation seed = KeyValueStoreOperation.setIfAbsentWithExpiry("k", "0", 3500L);
        Assert.assertEquals(KeyValueStoreOperation.Type.SET_NX_PX, seed.getType());
        Assert.assertEquals("0", seed.getValue());
        Assert.assertEquals(3500L, seed.getTtlMillis());
        Assert.assertEquals("SET_NX_PX k", seed.toString());
    }

    @Test
    public void testJedisClientSupportsBatchesAndHandlesEmptyInputWithoutRedis() {
        JedisKeyValueStoreClient client = new JedisKeyValueStoreClient();
        Assert.assertTrue(client.supportsBatchOperations());
        Assert.assertEquals(Collections.emptyList(), client.getMultiple(Collections.<String>emptyList()));
        Assert.assertEquals(Collections.emptyList(), client.executeBatch(Collections.<KeyValueStoreOperation>emptyList()));
        Assert.assertEquals(Collections.emptyList(), client.executeBatch(null));
    }
}
