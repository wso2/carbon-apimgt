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

package org.apache.synapse.transport.fix;

import junit.framework.TestCase;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import quickfix.Group;
import quickfix.Message;
import quickfix.field.*;
import quickfix.fix44.QuoteRequest;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FieldOrderingTest extends TestCase {

    public void testRepeatingGroupOrdering() throws IOException {

        int order[] = new int[] {
                Symbol.FIELD, SecurityID.FIELD, SecurityIDSource.FIELD, Product.FIELD,
                QuoteType.FIELD, OrderQty.FIELD, SettlDate.FIELD, QuotePriceType.FIELD,
                ValidUntilTime.FIELD, ExpireTime.FIELD
        };

        Message message = new QuoteRequest(new QuoteReqID("20101110-2"));
        Group group = new Group(NoRelatedSym.FIELD, Symbol.FIELD, order);
        group.setField(new Symbol("TestSymbol"));
        group.setField(new SecurityID("SecurityID"));
        group.setField(new SecurityIDSource("SecurityIDSource"));
        group.setField(new Product(11));
        group.setField(new QuoteType(1));
        group.setField(new OrderQty(500));
        group.setField(new SettlDate("20151116"));
        group.setField(new QuotePriceType(1));
        group.setField(new ValidUntilTime(new Date()));
        group.setField(new ExpireTime(new Date()));
        message.addGroup(group);
        System.out.println("Original Message: " + message);

        MessageContext msgCtx = new MessageContext();
        FIXUtils.getInstance().setSOAPEnvelope(message, 1, "TestSession", msgCtx);
        OMElement msgElt = msgCtx.getEnvelope().getBody().getFirstElement();
        OMElement groupsElt = msgElt.getFirstChildWithName(new QName(FIXConstants.FIX_BODY)).
                getFirstChildWithName(new QName(FIXConstants.FIX_GROUPS));

        int groupId = Integer.parseInt(groupsElt.getAttributeValue(
                new QName(FIXConstants.FIX_FIELD_ID)));
        assertEquals(groupId, group.getFieldTag());

        // Test whether the fileds in the SOAP infoset are in the correct order
        Iterator fields = groupsElt.getFirstElement().getChildrenWithName(
                new QName(FIXConstants.FIX_FIELD));
        List<Integer> fieldList = new ArrayList<Integer>();
        while (fields.hasNext()) {
            OMElement fieldElt = (OMElement) fields.next();
            fieldList.add(Integer.parseInt(fieldElt.getAttributeValue(
                    new QName(FIXConstants.FIX_FIELD_ID))));
        }
        assertEquals(order.length, fieldList.size());
        for (int i = 0; i < order.length; i++) {
            assertEquals(order[i], (int) fieldList.get(i));
        }

        // Test whether the reconstructed message preserves the group field order
        Message copy = FIXUtils.getInstance().createFIXMessage(msgCtx);
        System.out.println("Reconstructed Message: " + copy);
        List<Group> groups = copy.getGroups(NoRelatedSym.FIELD);
        assertEquals(1, groups.size());
        int[] copyOrder = groups.get(0).getFieldOrder();
        assertEquals(order.length, copyOrder.length);
        for (int i = 0; i < order.length; i++) {
            assertEquals(order[i], copyOrder[i]);
        }

        assertEquals(message.toString(), copy.toString());        
    }

}
