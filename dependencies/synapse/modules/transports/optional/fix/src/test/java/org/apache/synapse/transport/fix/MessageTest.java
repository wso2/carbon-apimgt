/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
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

import quickfix.Message;
import quickfix.Group;
import quickfix.fix41.NewOrderSingle;
import quickfix.field.*;
import org.apache.axis2.context.MessageContext;
import org.apache.axiom.om.util.AXIOMUtil;
import org.custommonkey.xmlunit.XMLTestCase;

import java.util.Date;

public class MessageTest extends XMLTestCase {

    private static final String BEGIN_STRING    = "FIX.4.1";
    private static final String SENDER_ID       = "BANZAI";
    private static final String TARGET_ID = "SYNAPSE";
    private static final int SEQ_NUM            = 5;
    private static final String SYMBOL          = "APACHE";
    private static final String CLORD_ID        = "12345";
    private static final String CHECKSUM        = "67890";
    private static final String TX_DATE         = new Date().toString();
    private static final String SESSION_ID      = "FIX.4.1:BANZAI->SYNAPSE";

    public void testSimpleFIXMessage() throws Exception {
        Message message = new NewOrderSingle();
        message.getHeader().setField(new BeginString(BEGIN_STRING));
        message.getHeader().setField(new SenderCompID(SENDER_ID));
        message.getHeader().setField(new TargetCompID(TARGET_ID));
        message.getHeader().setField(new MsgSeqNum(SEQ_NUM));

        message.setField(new Symbol(SYMBOL));
        message.setField(new ClOrdID(CLORD_ID));
        message.setField(new TradeOriginationDate(TX_DATE));

        message.getTrailer().setField(new CheckSum(CHECKSUM));

        MessageContext msgCtx = new MessageContext();
        FIXUtils.getInstance().setSOAPEnvelope(message, SEQ_NUM, SESSION_ID, msgCtx);
        String result = msgCtx.getEnvelope().getBody().getFirstElement().toString();
        String expected =
                "<message inSession=\"" + SESSION_ID + "\" counter=\"" + SEQ_NUM + "\">" +
                    "<header>" +
                        "<field id=\"" + BeginString.FIELD + "\">" + BEGIN_STRING + "</field>" +
                        "<field id=\"" + MsgSeqNum.FIELD + "\">" + SEQ_NUM + "</field>" +
                        "<field id=\"" + MsgType.FIELD + "\">" + NewOrderSingle.MSGTYPE + "</field>" +
                        "<field id=\"" + SenderCompID.FIELD + "\">" + SENDER_ID + "</field>" +
                        "<field id=\"" + TargetCompID.FIELD + "\">" + TARGET_ID + "</field>" +
                    "</header>" +
                    "<body>" +
                        "<field id=\"" + ClOrdID.FIELD + "\">" + CLORD_ID + "</field>" +
                        "<field id=\"" + Symbol.FIELD + "\">" + SYMBOL + "</field>" +
                        "<field id=\"" + TradeOriginationDate.FIELD + "\">" + TX_DATE + "</field>" +
                    "</body>" +
                    "<trailer>" +
                        "<field id=\"" + CheckSum.FIELD + "\">" + CHECKSUM + "</field>" +
                    "</trailer>" +
                "</message>";

        assertXMLEqual(expected, AXIOMUtil.stringToOM(result).toString());        
    }

    public void testAdvancedFIXMessage() throws Exception {
        Message message = new NewOrderSingle();
        message.getHeader().setField(new BeginString(BEGIN_STRING));
        message.getHeader().setField(new SenderCompID(SENDER_ID));
        message.getHeader().setField(new TargetCompID(TARGET_ID));
        message.getHeader().setField(new MsgSeqNum(SEQ_NUM));

        message.setField(new Symbol(SYMBOL));
        message.setField(new ClOrdID(CLORD_ID));
        message.setField(new TradeOriginationDate(TX_DATE));

        Group g1 = new Group(NoAllocs.FIELD, AllocAccount.FIELD);
        g1.setField(new AllocAccount("ABC"));
        g1.setField(new IndividualAllocID("PQR"));
        message.addGroup(g1);
        Group g2 = new Group(NoAllocs.FIELD, AllocAccount.FIELD);
        g2.setField(new AllocAccount("MNO"));
        g2.setField(new IndividualAllocID("XYZ"));
        message.addGroup(g2);

        message.getTrailer().setField(new CheckSum(CHECKSUM));

        MessageContext msgCtx = new MessageContext();
        FIXUtils.getInstance().setSOAPEnvelope(message, SEQ_NUM, SESSION_ID, msgCtx);
        String result = msgCtx.getEnvelope().getBody().getFirstElement().toString();
        String expected =
                "<message inSession=\"" + SESSION_ID + "\" counter=\"" + SEQ_NUM + "\">" +
                    "<header>" +
                        "<field id=\"" + BeginString.FIELD + "\">" + BEGIN_STRING + "</field>" +
                        "<field id=\"" + MsgSeqNum.FIELD + "\">" + SEQ_NUM + "</field>" +
                        "<field id=\"" + MsgType.FIELD + "\">" + NewOrderSingle.MSGTYPE + "</field>" +
                        "<field id=\"" + SenderCompID.FIELD + "\">" + SENDER_ID + "</field>" +
                        "<field id=\"" + TargetCompID.FIELD + "\">" + TARGET_ID + "</field>" +
                    "</header>" +
                    "<body>" +
                        "<field id=\"" + ClOrdID.FIELD + "\">" + CLORD_ID + "</field>" +
                        "<field id=\"" + Symbol.FIELD + "\">" + SYMBOL + "</field>" +
                        "<field id=\"" + NoAllocs.FIELD + "\">2</field>" +
                        "<field id=\"" + TradeOriginationDate.FIELD + "\">" + TX_DATE + "</field>" +
                        "<groups id=\"" + NoAllocs.FIELD +"\">" +
                            "<group>" +
                                "<field id=\"" + AllocAccount.FIELD + "\">ABC</field>" +
                                "<field id=\"" + IndividualAllocID.FIELD + "\">PQR</field>" +
                            "</group>" +
                            "<group>" +
                                "<field id=\"" + AllocAccount.FIELD + "\">MNO</field>" +
                                "<field id=\"" + IndividualAllocID.FIELD + "\">XYZ</field>" +
                            "</group>" +
                        "</groups>" +
                    "</body>" +
                    "<trailer>" +
                        "<field id=\"" + CheckSum.FIELD + "\">" + CHECKSUM + "</field>" +
                    "</trailer>" +
                "</message>";

        assertXMLEqual(expected, AXIOMUtil.stringToOM(result).toString());
    }
}
