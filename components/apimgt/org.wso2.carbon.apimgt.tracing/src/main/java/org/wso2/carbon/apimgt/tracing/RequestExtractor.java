//package org.wso2.carbon.apimgt.tracing;
//
//import io.opentracing.propagation.TextMap;
//
//import java.util.Iterator;
//import java.util.Map;
//
//public class RequestExtractor implements TextMap {
//
//    private Map<String, String> headers;
//    private Iterator<Map.Entry<String, String>> iterator;
//
//    RequestExtractor(Map<String, String> headers) { this.headers = headers; }
//
//    @Override
//    public Iterator<Map.Entry<String, String>> iterator() {
//        if (iterator == null) {
//            return this.headers.entrySet().iterator();
//        } else {
//            return iterator;
//        }
//    }
//
//    @Override
//    public void put(String s, String s1) {
//        throw new UnsupportedOperationException("This class should be used only with Tracer.extract()!");
//    }
//
//}
