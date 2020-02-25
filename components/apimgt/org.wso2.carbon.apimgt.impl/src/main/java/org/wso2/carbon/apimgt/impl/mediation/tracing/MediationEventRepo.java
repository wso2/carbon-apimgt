package org.wso2.carbon.apimgt.gateway.messagetracing;

import org.apache.synapse.aspects.flow.statistics.data.raw.StatisticDataUnit;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.*;

public class MediationEventRepo {

    private static MediationEventRepo instance = new MediationEventRepo();
    private Map<String, List<MediatorEvent>> traceEvents = Collections.synchronizedMap(new LinkedHashMap<>());

    public static MediationEventRepo getInstance() {
        return instance;
    }

    private void saveMediationEvent(String messageId, StatisticDataUnit statisticDataUnit, Axis2MessageContext synCtx) {
        MediatorEvent mediatorEvent = new MediatorEvent();

        String componentName = statisticDataUnit.getComponentName();
        mediatorEvent.setComponentName(componentName);

        String componentType = statisticDataUnit.getComponentType().toString();
        mediatorEvent.setComponentType(componentType);

        String componentId = statisticDataUnit.getComponentId();
        mediatorEvent.setComponentId(componentId);

        Map<String, Object> transportHeaders = statisticDataUnit.getTransportPropertyMap();
        mediatorEvent.setTransportHeaders(transportHeaders);

        Map<String, Object> synMsg = statisticDataUnit.getContextPropertyMap();
        if (synMsg != null) {
            synMsg.remove("OPEN_API_OBJECT");
            synMsg.remove("OPEN_API_STRING");
        }
        mediatorEvent.setSynapseMessageContext(synMsg);

        Map<String, Object> propertyMap = new TreeMap<>();
        Iterator<String> propertyIterator = synCtx.getAxis2MessageContext().getPropertyNames();
        while (propertyIterator.hasNext()) {
            String propName = propertyIterator.next();
            propertyMap.put(propName, synCtx.getAxis2MessageContext().getProperty(propName));
        }
        mediatorEvent.setAxis2MessageContext(propertyMap);
        String payload = statisticDataUnit.getPayload();
        mediatorEvent.setPayload(payload);

        List<MediatorEvent> mediatorEventsForMsg = traceEvents.get(messageId);
        if (mediatorEventsForMsg == null) {
            mediatorEventsForMsg = new ArrayList<>();
        }
        mediatorEventsForMsg.add(mediatorEvent);
    }

    public List<MediatorEvent> getMediatorEventsForMessageId(String messageId) {
        return traceEvents.get(messageId);
    }

    public List<String> getMessageIds() {
        return new ArrayList<>(traceEvents.keySet());
    }
}
