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

/**
 * Contains resource level information of a specific API resource extracted by parsing the Swagger definition
 */

public class APIResource {
    private final UriTemplate uriTemplate;
    private final Scope scope;
    private final String produces;
    private final String consumes;

    private APIResource(Builder builder) {
        uriTemplate = builder.uriTemplate;
        scope = builder.scope;
        produces = builder.produces;
        consumes = builder.consumes;
    }


    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    public Scope getScope() {
        return scope;
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
        private Scope scope;
        private String produces;
        private String consumes;

        public Builder() {
        }

        public Builder(APIResource copy) {
            this.uriTemplate = copy.uriTemplate;
            this.scope = copy.scope;
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
         * Sets the {@code scope} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param scope the {@code scope} to set
         * @return a reference to this Builder
         */
        public Builder scope(Scope scope) {
            this.scope = scope;
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
}
