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

package org.apache.synapse.util;

import java.util.Iterator;

import javax.activation.DataHandler;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAP11Version;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPVersion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;

public class PayloadHelper {
	
	// this has to match org.apache.axis2.base.transport.BaseConstants 
	// at some future point we will merge this into Axiom as a common parent
	public final static String AXIOMPAYLOADNS = "http://ws.apache.org/commons/ns/payload";

	public final static QName BINARYELT = new QName(AXIOMPAYLOADNS, "binary",
			"ax");

	public final static QName TEXTELT = new QName(AXIOMPAYLOADNS, "text", "ax");

	public final static QName MAPELT = new QName(AXIOMPAYLOADNS, "map", "ax");

	public final static int XMLPAYLOADTYPE = 0, BINARYPAYLOADTYPE = 1,
			TEXTPAYLOADTYPE = 2, MAPPAYLOADTYPE = 3;

	public static final Log log = LogFactory.getLog(PayloadHelper.class);

	// gets a indication of the payload type. Default is XML
	// You cannot set the payload type. Instead, it is set automatically when
	// the payload is set
	public static int getPayloadType(SOAPEnvelope envelope) {
		OMElement el = getXMLPayload(envelope);
		if (el.getQName().equals(BINARYELT)) {
			return BINARYPAYLOADTYPE;
        } else if (el.getQName().equals(TEXTELT)) {
			return TEXTPAYLOADTYPE;
        } else if (el.getQName().equals(MAPELT)) {
			return MAPPAYLOADTYPE;
        } else {
			return XMLPAYLOADTYPE; // default XML
        }
	}

	public static int getPayloadType(MessageContext mc) {
		if (mc.getEnvelope() == null)
			return 0;
		return getPayloadType(mc.getEnvelope());
	}

	// XML Payload is carried as the first (and only) child of the body
	public static OMElement getXMLPayload(SOAPEnvelope envelope) {
		SOAPBody body = envelope.getBody();
		if (body == null) {
			log.error("No body found");
			return null;
		}
		OMElement bodyEl = body.getFirstElement();
		if (bodyEl == null) {
			log.error("No body child found");
			return null;
		}
		return bodyEl;
	}

	public static void setXMLPayload(SOAPEnvelope envelope, OMElement element) {
		SOAPBody body = envelope.getBody();
		if (body == null) {

			SOAPVersion version = envelope.getVersion();
			if (version.getEnvelopeURI().equals(
					SOAP11Version.SOAP_ENVELOPE_NAMESPACE_URI)) {
				body = OMAbstractFactory.getSOAP11Factory().createSOAPBody();
			} else {
				body = OMAbstractFactory.getSOAP12Factory().createSOAPBody();
			}
			if (envelope.getHeader() != null) {
				envelope.getHeader().insertSiblingAfter(body);
			} else {
				envelope.addChild(body);
			}
		} else {
			for (Iterator it = body.getChildren(); it.hasNext();) {
				OMNode node = (OMNode) it.next();
				node.discard();
			}
		}
		body.addChild(element);
	}

	public static void setXMLPayload(MessageContext mc, OMElement element) {
		if (mc.getEnvelope() == null) {
			try {
				mc.setEnvelope(OMAbstractFactory.getSOAP12Factory()
						.createSOAPEnvelope());
			} catch (Exception e) {
				throw new SynapseException(e);
			}
		}
		setXMLPayload(mc.getEnvelope(), element);
	}

	// Binary Payload is carried in a wrapper element with QName BINARYELT
	public static DataHandler getBinaryPayload(SOAPEnvelope envelope) {
		OMElement el = getXMLPayload(envelope);
		if (el == null)
			return null;
		if (!el.getQName().equals(BINARYELT)) {
			log.error("Wrong QName" + el.getQName());
			return null;
		}
		OMNode textNode = el.getFirstOMChild();
		if (textNode.getType() != OMNode.TEXT_NODE) {
			log.error("Text Node not found");
			return null;
		}
		OMText text = (OMText) textNode;
        try {
            return (DataHandler) text.getDataHandler();
        } catch (ClassCastException ce) {
            log.error("cannot get DataHandler" + ce.getMessage());
            return null;
        }
	}

	public static DataHandler getBinaryPayload(MessageContext mc) {
		if (mc.getEnvelope() == null) {
			log.error("null envelope");
			return null;
		}
		return getBinaryPayload(mc.getEnvelope());
	}

	public static void setBinaryPayload(SOAPEnvelope envelope, DataHandler dh) {
		OMFactory fac = envelope.getOMFactory();
		OMElement binaryElt = envelope.getOMFactory()
				.createOMElement(BINARYELT);
		OMText text = fac.createOMText(dh, true);
		binaryElt.addChild(text);
		setXMLPayload(envelope, binaryElt);
	}

	public static void setBinaryPayload(MessageContext mc, DataHandler dh) {
		if (mc.getEnvelope() == null) {
			try {
				mc.setEnvelope(OMAbstractFactory.getSOAP12Factory()
						.createSOAPEnvelope());
			} catch (Exception e) {
				throw new SynapseException(e);
			}
		}
		setBinaryPayload(mc.getEnvelope(), dh);

	}

	// Text payload is carried in a wrapper element with QName TEXTELT
	public static String getTextPayload(SOAPEnvelope envelope) {
		OMElement el = getXMLPayload(envelope);
		if (el == null)
			return null;
		if (!el.getQName().equals(TEXTELT)) {
			log.error("Wrong QName " + el.getQName());
			return null;
		}
		OMNode textNode = el.getFirstOMChild();
		if (textNode.getType() != OMNode.TEXT_NODE) {
			log.error("Text Node not found");
			return null;
		}
		OMText text = (OMText) textNode;
		return text.getText();
	}

	public static String getTextPayload(MessageContext mc) {
		if (mc.getEnvelope() == null) {
			log.error("null envelope");
			return null;
		}
		return getTextPayload(mc.getEnvelope());
	}

	public static void setTextPayload(SOAPEnvelope envelope, String text) {
		OMFactory fac = envelope.getOMFactory();
		OMElement textElt = envelope.getOMFactory().createOMElement(TEXTELT);
		OMText textNode = fac.createOMText(text);
		textElt.addChild(textNode);
		setXMLPayload(envelope, textElt);
	}

	public static void setTextPayload(MessageContext mc, String text) {
		if (mc.getEnvelope() == null) {
			try {
				mc.setEnvelope(OMAbstractFactory.getSOAP12Factory()
						.createSOAPEnvelope());
			} catch (Exception e) {
				throw new SynapseException(e);
			}
		}
		setTextPayload(mc.getEnvelope(), text);
	}

	// Map payload must be a Map of String->int, boolean, float, double, char,
	// short, byte, byte[], long, String
	public static SimpleMap getMapPayload(SOAPEnvelope envelope) {
		OMElement el = getXMLPayload(envelope);
		if (el == null)
			return null;
		if (!el.getQName().equals(MAPELT)) {
			log.error("Wrong QName" + el.getQName());
			return null;
		}
        return new SimpleMapImpl(el);
	}

	public static SimpleMap getMapPayload(MessageContext mc) {
		if (mc.getEnvelope() == null) {
			log.error("null envelope");
			return null;
		}
		return getMapPayload(mc.getEnvelope());
	}

	public static void setMapPayload(SOAPEnvelope envelope, SimpleMap map) {

		if (map instanceof SimpleMapImpl) {
			SimpleMapImpl impl = (SimpleMapImpl) map;
			OMElement mapElt = impl.getOMElement(envelope.getOMFactory());
			if (mapElt == null) {
				log.debug("null map element returned");
				return;
			}
			setXMLPayload(envelope, mapElt);
		} else {
			throw new SynapseException("cannot handle any other instance of SimpleMap at this point TODO");
		}
	}

	public static void setMapPayload(MessageContext mc, SimpleMap map) {
		if (mc.getEnvelope() == null) {
			try {
				mc.setEnvelope(OMAbstractFactory.getSOAP12Factory()
						.createSOAPEnvelope());
			} catch (Exception e) {
				throw new SynapseException(e);
			}
		}
		setMapPayload(mc.getEnvelope(), map);
	}
	
	public static XMLStreamReader getStAXPayload(SOAPEnvelope envelope) {
		 
		OMElement el = getXMLPayload(envelope);
		if (el==null) {
			return null;
		}
		return el.getXMLStreamReader();
	}
    
	public static XMLStreamReader getStAXPayload(MessageContext mc) {
		if (mc.getEnvelope() == null) {
			log.error("null envelope");
			return null;
		}
		return getStAXPayload(mc.getEnvelope());
	}

	public static void setStAXPayload(SOAPEnvelope envelope, XMLStreamReader streamReader) {
		StAXOMBuilder builder = new StAXOMBuilder(envelope.getOMFactory(), streamReader);
		OMElement el = builder.getDocumentElement();
		setXMLPayload(envelope, el);
	}

	public static void setStAXPayload(MessageContext mc, XMLStreamReader streamReader) {
		if (mc.getEnvelope() == null) {
			try {
				mc.setEnvelope(OMAbstractFactory.getSOAP12Factory().createSOAPEnvelope());
			} catch (Exception e) {
				throw new SynapseException(e);
			}
			setStAXPayload(mc.getEnvelope(), streamReader);
		}
	}
}
