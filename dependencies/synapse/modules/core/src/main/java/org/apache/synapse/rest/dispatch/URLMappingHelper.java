/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.synapse.rest.dispatch;

import org.apache.synapse.rest.RESTUtils;

public class URLMappingHelper implements DispatcherHelper {

    private String[] pathSegments;
    private String extension;
    private String exactMatch;

    public URLMappingHelper(String urlMapping) {
        if (urlMapping.startsWith("/") && urlMapping.endsWith("/*")) {
            if (urlMapping.length() > 2) {
                pathSegments = urlMapping.substring(1, urlMapping.length() - 2).split("/");
            } else {
                pathSegments = new String[] {};
            }
        } else if (urlMapping.startsWith("*.")) {
            extension = urlMapping.substring(1);
        } else if(urlMapping.length() > 1 && urlMapping.endsWith("/")){
            exactMatch = urlMapping.substring(0, urlMapping.length() - 1);
        }
        else {
            exactMatch = urlMapping;
        }
    }

    public boolean isExactMatch(String url) {
        if (!"/".equals(url)) {
            url = RESTUtils.trimTrailingSlashes(url);
        }
        int index = url.indexOf('?');
        if (index != -1) {
            url = url.substring(0, index);
        }
        return exactMatch != null && exactMatch.equals(url);
    }

    public boolean isExtensionMatch(String url) {
        int index = url.indexOf('?');
        if (index != -1) {
            url = url.substring(0, index);
        }
        return extension != null && url.endsWith(extension);
    }

    public int getPrefixMatchingLength(String url) {
        if (pathSegments != null) {
            if (pathSegments.length == 0) {
                return 1;
            }

            url = RESTUtils.trimSlashes(url);
            int index = url.indexOf('?');
            if (index != -1) {
                url = url.substring(0, index);
            }
            String[] segments = url.split("/");
            int matchingLength = 0;
            for (int i = 0; i < pathSegments.length; i++) {
                if (segments.length > i) {
                    if (segments[i].equals(pathSegments[i])) {
                        matchingLength++;
                    } else {
                        return 0;
                    }
                } else {
                    return 0;
                }
            }
            return matchingLength;
        }
        return 0;
    }

    public String getString() {
        if (pathSegments != null) {
            StringBuilder str = new StringBuilder("");
            for (String segment : pathSegments) {
                str.append("/").append(segment);
            }
            return str.append("/*").toString();
        } else if (extension != null) {
            return "*." + extension;
        } else {
            return exactMatch;
        }
    }
}
