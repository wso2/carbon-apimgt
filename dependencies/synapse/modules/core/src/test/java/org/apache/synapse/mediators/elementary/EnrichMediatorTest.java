/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.mediators.elementary;

import junit.framework.TestCase;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.mediators.transform.PayloadFactoryMediator;
import java.util.ArrayList;

public class EnrichMediatorTest extends TestCase {

	public void testEnrichingEnvelopeUsingClonedProperty() throws Exception {

		String xml1 =
		              "<p:echoInt xmlns:p=\"http://echo.services.core.carbon.wso2.org\">"
		                      + "      <!--0 to 1 occurrence-->" + "<in>35</in>" + "</p:echoInt>";

		String format =
		                "<p:echoInt xmlns:p=\"http://echo.services.core.carbon.wso2.org\">"
		                        + "<in>1</in></p:echoInt>";

		// name of the property
		String key = "envelope";

		// create a message context from xml1
		MessageContext msgCtxt1 = TestUtils.createLightweightSynapseMessageContext(xml1);

		// enrich the envelope to a property
		EnrichMediator enrichMediator1 = createEnvelopeToPropertyEnrichMediator(key, true);
		enrichMediator1.mediate(msgCtxt1);

		String expectedPropVal =
		                         "<?xml version='1.0' encoding='utf-8'?>" +
		                                 "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
		                                 "<soapenv:Body>" + xml1 +
		                                 "</soapenv:Body></soapenv:Envelope>";

		// assert the property
		assertEquals(expectedPropVal, ((ArrayList) msgCtxt1.getProperty(key)).get(0).toString());

		PayloadFactoryMediator payloadFacMediator = new PayloadFactoryMediator();
		payloadFacMediator.setType("xml");
		payloadFacMediator.setFormat(format);
		payloadFacMediator.mediate(msgCtxt1);

		String expectedPayload =
		                         "<soapenv:Body xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
		                                 format + "</soapenv:Body>";
		// assert the new payload
		assertEquals(expectedPayload, msgCtxt1.getEnvelope().getBody().toString());

		// assert the property again - since the property was created using a
		// cloned message, it should not change
		assertEquals(expectedPropVal, ((ArrayList) msgCtxt1.getProperty(key)).get(0).toString());

	}

	public void testEnrichingEnvelopeUsingUnclonedProperty() throws Exception {

		String xml1 =
		              "<p:echoInt xmlns:p=\"http://echo.services.core.carbon.wso2.org\">"
		                      + "      <!--0 to 1 occurrence-->" + "<in>35</in>" + "</p:echoInt>";

		String format =
		                "<p:echoInt xmlns:p=\"http://echo.services.core.carbon.wso2.org\">"
		                        + "<in>1</in></p:echoInt>";

		// name of the property
		String key = "envelope";

		// create a message context from xml1
		MessageContext msgCtxt1 = TestUtils.createLightweightSynapseMessageContext(xml1);

		// enrich the envelope to a property
		EnrichMediator enrichMediator1 = createEnvelopeToPropertyEnrichMediator(key, false);
		enrichMediator1.mediate(msgCtxt1);

		String expectedPropVal =
		                         "<?xml version='1.0' encoding='utf-8'?>" +
		                                 "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
		                                 "<soapenv:Body>" + xml1 +
		                                 "</soapenv:Body></soapenv:Envelope>";

		// assert the property
		assertEquals(expectedPropVal, ((ArrayList) msgCtxt1.getProperty(key)).get(0).toString());

		PayloadFactoryMediator payloadFacMediator = new PayloadFactoryMediator();
		payloadFacMediator.setType("xml");
		payloadFacMediator.setFormat(format);
		payloadFacMediator.mediate(msgCtxt1);

		String expectedPayload =
		                         "<soapenv:Body xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
		                                 format + "</soapenv:Body>";

		// assert the new payload
		assertEquals(expectedPayload, msgCtxt1.getEnvelope().getBody().toString());

		// expected property
		expectedPropVal = expectedPropVal.replaceFirst(xml1, format);

		// assert the property again - since the property was created using an
		// uncloned message, it should change
		assertEquals(expectedPropVal, ((ArrayList) msgCtxt1.getProperty(key)).get(0).toString());

	}

	private EnrichMediator createEnvelopeToPropertyEnrichMediator(String propertyName, boolean clone) {
		// source to be used by enrich mediator
		Source source = new Source();
		source.setClone(clone);
		source.setSourceType(EnrichMediator.ENVELOPE);

		// target that will be replaced by the enrich mediator
		Target target = new Target();
		target.setTargetType(EnrichMediator.PROPERTY);
		target.setProperty(propertyName);

		// instantiate enrich mediator
		EnrichMediator enrich = new EnrichMediator();
		enrich.setSource(source);
		enrich.setTarget(target);
		return enrich;
	}

}