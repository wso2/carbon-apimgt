package org.wso2.carbon.apimgt.tracing;

import io.opentracing.propagation.TextMap;

import java.util.Iterator;
import java.util.Map;

public class RequestExtractor implements TextMap {

    private Map<String, String> headers;
    private Iterator<Map.Entry<String, String>> iterator;

    public RequestExtractor(Map<String, String> headers) {
        this.headers = headers;
    }

    public RequestExtractor(Iterator<Map.Entry<String, String>> headers) {
        this.iterator = headers;
    }

    @Override
    public Iterator<Map.Entry<String, String>> iterator() {
        if (iterator == null) {
            return this.headers.entrySet().iterator();
        } else {
            return iterator;
        }
    }

    @Override
    public void put(String s, String s1) {
        throw new UnsupportedOperationException("This class should be used only with Tracer.extract()!");
    }

}
