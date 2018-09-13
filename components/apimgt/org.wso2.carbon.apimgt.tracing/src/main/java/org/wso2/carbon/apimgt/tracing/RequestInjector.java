package org.wso2.carbon.apimgt.tracing;

import io.opentracing.propagation.TextMap;

import java.util.Iterator;
import java.util.Map;

public class RequestInjector implements TextMap {

    private final Map<String, String> carrier;

    public RequestInjector(Map<String, String> carrier) {
        this.carrier = carrier;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        throw new UnsupportedOperationException("This class should be used only with Tracer.inject()!");

    }

    @Override
    public void put(String key, String value) {
        carrier.put(key, value);
    }
}
