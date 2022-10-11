/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.apk.apimgt.impl.dto;

import java.io.Serializable;
import java.util.Set;

public class ResourceInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String urlPattern;

    private Set<VerbInfoDTO> httpVerbs;

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public Set<VerbInfoDTO> getHttpVerbs() {
        return httpVerbs;
    }

    public void setHttpVerbs(Set<VerbInfoDTO> httpVerbs) {
        this.httpVerbs = httpVerbs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceInfoDTO that = (ResourceInfoDTO) o;

        if (!urlPattern.equals(that.getUrlPattern())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getUrlPattern().hashCode();
    }
}
