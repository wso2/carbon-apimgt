/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.message.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

public class MessageStoreView implements MessageStoreViewMBean {

    private String messageStoreName;

    private MessageStore messageStore;

    private static final Log log = LogFactory.getLog(MessageStoreView.class);

    public MessageStoreView(String name, MessageStore messageStore){
        this.messageStoreName = name;
        this.messageStore = messageStore;
    }


    public void deleteAll() {
        messageStore.clear();
    }

    public List<String> getMessageIds() {

        List<String> returnList = new ArrayList<String>();
        //List<MessageContext> list = messageStore.getAll();

        //for(MessageContext m : list) {
        //    returnList.add(m.getMessageID());
        //}
        return returnList;
    }

    public void delete(String messageID) {
        if(messageID != null) {
           // MessageContext m =messageStore.remove(messageID);
            //if (m != null){
            //    log.info("Message with ID :" + messageID + " removed from the MessageStore");
            //}
        }
    }

    public String getEnvelope(String messageID) {
        if (messageID != null) {
            //MessageContext m = messageStore.get(messageID);

            //if (m != null) {
            //    return m.getEnvelope().toString();
            //}
        }
        return null;
    }

    public long getSize() {
        return ((AbstractMessageStore) messageStore).difference();
    }

}
