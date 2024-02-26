package org.wso2.carbon.apimgt.rest.api.util.servlet.impl;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomCXFNonSpringJaxrsServlet extends CXFNonSpringJaxrsServlet {

    static Map<String, String> systemPropMap = new HashMap();

    static {
        systemPropMap.put("rest.api.admin.attachment.max.size", "10485760");
        systemPropMap.put("rest.api.devportal.attachment.max.size", "10485760");
        systemPropMap.put("rest.api.publisher.attachment.max.size", "10485760");
        systemPropMap.put("rest.api.service.catalog.attachment.max.size", "10485760");
    }

    protected static Map<String, String> parseMapSequence(String sequence) {
        if (sequence != null) {
            sequence = sequence.trim();
            Map<String, String> map = new HashMap();
            String[] pairs = StringUtils.split(sequence, " ");
            String[] arr = pairs;
            int len$ = pairs.length;

            for(int i = 0; i < len$; ++i) {
                String pair = arr[i];
                String thePair = pair.trim();
                if (!thePair.isEmpty()) {
                    String[] value = StringUtils.split(thePair, "=");
                    if (value.length == 2) {
                        if (value[1].startsWith("{systemProperties")) {
                            //resolve system property if exists or else set default value
                            int begin = value[1].indexOf("'");
                            int end = value[1].lastIndexOf("'");
                            String key = value[1].substring(begin, end);
                            String systemPropValue = System.getProperty(key);
                            if (StringUtils.isNotEmpty(systemPropValue)) {
                                map.put(value[0].trim(), systemPropValue);
                            } else {
                                map.put(value[0].trim(), systemPropMap.get(value[0]));
                            }
                        } else {
                            map.put(value[0].trim(), value[1].trim());
                        }
                    } else {
                        map.put(thePair, "");
                    }
                }
            }

            return map;
        } else {
            return Collections.emptyMap();
        }
    }
}
