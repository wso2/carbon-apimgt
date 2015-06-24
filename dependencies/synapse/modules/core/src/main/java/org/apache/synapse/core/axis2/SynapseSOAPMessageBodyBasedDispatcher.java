package org.apache.synapse.core.axis2;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.dispatchers.SOAPMessageBodyBasedDispatcher;
import org.apache.synapse.transport.passthru.util.RelayUtils;

public class SynapseSOAPMessageBodyBasedDispatcher extends
		SOAPMessageBodyBasedDispatcher {

	@Override
	public AxisOperation findOperation(AxisService axisService,
			MessageContext mc) throws AxisFault {
		checkPTMessageAndBuild(mc);
		return super.findOperation(axisService, mc);
	}

	@Override
	public AxisService findService(MessageContext mc) throws AxisFault {
		checkPTMessageAndBuild(mc);
		return super.findService(mc);
	}

	private void checkPTMessageAndBuild(MessageContext mc) {
		OMElement body = mc.getEnvelope().getBody();
		if (body.getFirstElement() == null) {
			// Can be a pass-through case try to build and see.
			try {
				RelayUtils.buildMessage(mc, false);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}

		}

	}
}
