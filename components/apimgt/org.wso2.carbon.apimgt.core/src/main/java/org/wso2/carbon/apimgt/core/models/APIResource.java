/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.models;

import java.util.List;

/**
 * Contains resource level information of a specific API resource extracted by parsing the Swagger definition
 */

public class APIResource {
    private final UriTemplate uriTemplate;
    private final List<String> scopes;
    private final String produces;
    private final String consumes;

    private APIResource(Builder builder) {
        uriTemplate = builder.uriTemplate;
        scopes = builder.scopes;
        produces = builder.produces;
        consumes = builder.consumes;
    }


    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    public List<String> getScope() {
        return scopes;
    }


    public String getProduces() {
        return produces;
    }

    public String getConsumes() {
        return consumes;
    }

    /**
     * {@code APIResource} builder static inner class.
     */
    public static final class Builder {
        private UriTemplate uriTemplate;
        private List<String> scopes;
        private String produces;
        private String consumes;

        public Builder() {
        }

        public Builder(APIResource copy) {
            this.uriTemplate = copy.uriTemplate;
            this.scopes = copy.scopes;
            this.produces = copy.produces;
            this.consumes = copy.consumes;
        }

        /**
         * Sets the {@code uriTemplate} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param uriTemplate the {@code uriTemplate} to set
         * @return a reference to this Builder
         */
        public Builder uriTemplate(UriTemplate uriTemplate) {
            this.uriTemplate = uriTemplate;
            return this;
        }

        /**
         * Sets the {@code scopes} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param scopes the {@code scopes} to set
         * @return a reference to this Builder
         */
        public Builder scopes(List<String> scopes) {
            this.scopes = scopes;
            return this;
        }

        /**
         * Sets the {@code produces} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param produces the {@code produces} to set
         * @return a reference to this Builder
         */
        public Builder produces(String produces) {
            this.produces = produces;
            return this;
        }

        /**
         * Sets the {@code consumes} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param consumes the {@code consumes} to set
         * @return a reference to this Builder
         */
        public Builder consumes(String consumes) {
            this.consumes = consumes;
            return this;
        }

        /**
         * Returns a {@code APIResource} built from the parameters previously set.
         *
         * @return a {@code APIResource} built with parameters of this {@code APIResource.Builder}
         */
        public APIResource build() {
            return new APIResource(this);
        }
    }

    @Override
    public String toString() {
        return "APIResource{" +
                "uriTemplate=" + uriTemplate +
                '}';
    }
}
