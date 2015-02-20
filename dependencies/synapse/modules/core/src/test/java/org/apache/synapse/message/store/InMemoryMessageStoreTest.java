/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.message.store;

import junit.framework.TestCase;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.store.impl.memory.InMemoryStore;

import java.util.List;
import java.util.NoSuchElementException;

public class InMemoryMessageStoreTest extends TestCase {
    
    public void testBasics() throws Exception {
        System.out.println("Testing Basic InMemoryStore operations...");
        MessageStore store = new InMemoryStore();
        populateStore(store, 10);
        
        // test size()
        assertEquals(10, store.size());


        // test get(index)
        for (int i = 0; i < 10; i++) {
            assertEquals("ID" + i, store.get(i).getMessageID());
        }


        // test get(messageId)
        for (int i = 0; i < 10; i++) {
            assertEquals("ID" + i, store.get("ID" + i).getMessageID());
        }


        // test getAll()
        List<MessageContext> list = store.getAll();
        assertEquals(10, list.size());


        for (int i = 0; i < 10; i++) {
            assertEquals("ID" + i, list.get(i).getMessageID());
        }

        // test receive()
        MessageConsumer consumer = store.getConsumer();
        for (int i = 0; i < 10; i++) {
            assertEquals("ID" + i, consumer.receive().getMessageID());
            consumer.ack();
        }


        populateStore(store, 10);

        // test remove()
        for (int i = 0; i < 10; i++) {
            assertEquals("ID" + i, store.remove().getMessageID());
        }

        try {
            store.remove();
            fail();
        } catch (NoSuchElementException expected) {}

        populateStore(store, 10);

        // test clear()
        assertEquals(10, store.size());
        store.clear();
        assertEquals(0, store.size());

    }
    
    public void testOrderedDelivery1() throws Exception {
        System.out.println("Testing InMemoryStore Ordered Delivery...");
        MessageStore store = new InMemoryStore();
        for (int i = 0; i < 100; i++) {
            store.getProducer().storeMessage(createMessageContext("ID" + i));
        }
        MessageConsumer consumer = store.getConsumer();
        for (int i = 0; i < 100; i++) {
            assertEquals("ID" + i, consumer.receive().getMessageID());
            consumer.ack();
        }
    }
    
    public void testOrderedDelivery2() throws  Exception {
        System.out.println("Testing InMemoryStore Guaranteed Delivery...");
        MessageStore store = new InMemoryStore();
        store.getProducer().storeMessage(createMessageContext("FOO"));
        MessageConsumer consumer = store.getConsumer();
        MessageContext msg = consumer.receive();
        assertEquals("FOO", msg.getMessageID());

        store.getProducer().storeMessage(createMessageContext("BAR"));
        msg = consumer.receive();
        assertEquals("FOO", msg.getMessageID());

        consumer.ack();
        msg = consumer.receive();
        assertEquals("BAR", msg.getMessageID());

    }
    
    private MessageContext createMessageContext(String identifier) throws Exception {
        MessageContext msg = TestUtils.createLightweightSynapseMessageContext("<test/>");
        msg.setMessageID(identifier);
        return msg;
    }
    
    private void populateStore(MessageStore store, int count) throws Exception {
        for (int i = 0; i < count; i++) {
            store.getProducer().storeMessage(createMessageContext("ID" + i));
        }
    }
}
