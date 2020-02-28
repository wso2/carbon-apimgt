package org.wso2.carbon.apimgt.impl.mediation.tracing;

import org.apache.commons.lang3.ClassUtils;

import java.util.*;

public class MediationEventRepo {

    private static MediationEventRepo instance = new MediationEventRepo();
    private Map<String, List<MediatorEvent>> traceEvents = Collections.synchronizedMap(new LinkedHashMap<>());

    public static MediationEventRepo getInstance() {
        return instance;
    }

    // save events for message
    public void saveMediationEvent(String messageId, MediatorEvent event) {
        List<MediatorEvent> mediatorEventsForMsg = traceEvents.get(messageId);
        MediatorEvent previous = new MediatorEvent();
        if (mediatorEventsForMsg == null) {
            mediatorEventsForMsg = new ArrayList<>();
            previous.setComponentId("Start");
            previous.setComponentName("Start");
            previous.setComponentType("Start");
            mediatorEventsForMsg.add(previous);
        } else {
            previous = mediatorEventsForMsg.get(mediatorEventsForMsg.size() - 1);
        }
        MediatorEvent next = new MediatorEvent();
        next.setComponentId(event.getComponentId());
        next.setComponentName(event.getComponentName());
        next.setComponentType(event.getComponentType());

        event.setComponentId(previous.getComponentId());
        event.setComponentName(previous.getComponentName());
        event.setComponentType(previous.getComponentType());
        mediatorEventsForMsg.remove(mediatorEventsForMsg.size() - 1);
        if (mediatorEventsForMsg.size() > 0) {
            previous = mediatorEventsForMsg.get(mediatorEventsForMsg.size() - 1);
        } else {
            previous = new MediatorEvent();
        }

        mediatorEventsForMsg.add(event);
        mediatorEventsForMsg.add(next);

        event.setAddedTransportHeaders(getAdditions(previous.getTransportHeaders(), event.getTransportHeaders()));
        event.setRemovedTransportHeaders(getAdditions(event.getTransportHeaders(), previous.getTransportHeaders()));

        event.setAddedSynapseCtxProperties(getAdditions(previous.getSynapseCtxProperties(), event.getSynapseCtxProperties()));
        event.setRemovedSynapseCtxProperties(getAdditions(event.getSynapseCtxProperties(), previous.getSynapseCtxProperties()));

        event.setAddedAxis2CtxProperties(getAdditions(previous.getAXIS2MessageContext(), event.getAddedAxis2CtxProperties()));
        event.setRemovedAxis2CtxProperties(getAdditions(event.getAXIS2MessageContext(), previous.getAXIS2MessageContext()));

        traceEvents.put(messageId, mediatorEventsForMsg);
    }

    public Map<String, Object> getAdditions( Map <String,Object> mapPrevious , Map <String,Object> mapCurrent) {
        Map <String,Object> newMap = new HashMap<>();
        if (mapCurrent != null) {
            for (String propName : mapCurrent.keySet()) {
                Object propValue = mapCurrent.get(propName);
                newMap.put(propName, propValue);
            }
        }
        if (mapPrevious != null) {
            for (String propName : mapPrevious.keySet()) {
                newMap.remove(propName);
            }
        }
        return newMap;
     }

    public List<MediatorEvent> getMediatorEventsForMessageId(String messageId) {
        return traceEvents.get(messageId);
    }

    public List<String> getMessageIds() {
        List<String> keys = new ArrayList<>(traceEvents.keySet());
        List<String> lastKey = new ArrayList<>();
        if (keys.size() > 0) {
            lastKey.add(keys.get(keys.size() - 1));
        }
        return lastKey;
    }
}
