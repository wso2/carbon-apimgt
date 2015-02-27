package org.apache.synapse.transport.nhttp;

import junit.framework.TestCase;
import org.apache.synapse.transport.nhttp.util.NhttpUtil;

public class RestURLPostfixTest extends TestCase {

    /**
     * Test whether the REST_URL_PPOSTFIX contains a prefix as "/"
     */
    public void testPrefixingSlash(){
        String uri = "/epdata?size=10";
        String servicePath = "services";

        String REST_URL_POSTFIX = NhttpUtil.getRestUrlPostfix(uri, servicePath);
        assertTrue(REST_URL_POSTFIX != null && !REST_URL_POSTFIX.startsWith("/"));
    }

    /**
     * Include the service patch (services) keyword in the uri and check whether the REST_URL_POSTFIX is generated properly.
     */
    public void testServicePathInclusion(){
        //When the servicePath is somewhere in the middle of the uri
        String uri = "/epdata/services/?size=10";
        String servicePath = "services";

        String REST_URL_POSTFIX = NhttpUtil.getRestUrlPostfix(uri, servicePath);
        //REST_URL_POSTFIX should not be null and should contain the servicePath.
        assertTrue(REST_URL_POSTFIX != null && REST_URL_POSTFIX.contains(servicePath));

        //When the servicePatch is in the beginning of the uri
        uri = "/services/epdata?size=10";
        REST_URL_POSTFIX = NhttpUtil.getRestUrlPostfix(uri, servicePath);
        //REST_URL_POSTFIX should not be null and should not contain the servicePath.
        assertTrue(REST_URL_POSTFIX != null && !"".equals(REST_URL_POSTFIX) && !REST_URL_POSTFIX.contains(servicePath));
    }
    
    /**
     * check whether REST_URL_POSTFIX is generated properly when
     * there are query parameters with URLs as values
     */
    public void testQueryParamWithURL() {
    	String uri = "/test/admin?a=http://test.com";
        String servicePath = "services";
    
        String REST_URL_POSTFIX = NhttpUtil.getRestUrlPostfix(uri, servicePath);
        assertTrue(REST_URL_POSTFIX != null && REST_URL_POSTFIX.equals("test/admin?a=http://test.com"));
    }
}
