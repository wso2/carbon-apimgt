package org.apache.synapse.config.xml;


import org.apache.axiom.om.OMElement;
import org.apache.synapse.Mediator;
import org.apache.synapse.mediators.builtin.RespondMediator;

public class RespondMediatorSerializer extends AbstractMediatorSerializer{
    @Override
    protected OMElement serializeSpecificMediator(Mediator m) {
        if (!(m instanceof RespondMediator)) {
            handleException("Unsupported mediator passed in for serialization : " + m.getType());
        }
        RespondMediator mediator = (RespondMediator) m;
        OMElement respond = fac.createOMElement("respond", synNS);
        saveTracingState(respond, mediator);
        return respond;
    }

    public String getMediatorClassName() {
        return RespondMediator.class.getName();
    }
}
