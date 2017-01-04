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

import java.util.Objects;

/**
 * Endpoint specific information
 */
public class Endpoint {
    private final String id;
    private final String endpointConfig;
    private final MaxTps maxTps;
    private final String security;

    public String getId() {
        return id;
    }

    public String getEndpointConfig() {
        return endpointConfig;
    }

    public MaxTps getMaxTps() {
        return maxTps;
    }

    public String getSecurity() {
        return security;
    }

    private Endpoint(Builder builder) {
        id = builder.id;
        endpointConfig = builder.endpointConfig;
        maxTps = builder.maxTps;
        security = builder.security;
    }


    /**
     * MaxTps specific information
     */
    public static class MaxTps {
        private long production;
        private long sandbox;

        public MaxTps(long production, long sandbox) {
            this.production = production;
            this.sandbox = sandbox;
        }

        public long getProduction() {
            return production;
        }

        public long getSandbox() {
            return sandbox;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         * <p>
         * The {@code equals} method implements an equivalence relation
         * on non-null object references:
         * <ul>
         * <li>It is <i>reflexive</i>: for any non-null reference value
         * {@code x}, {@code x.equals(x)} should return
         * {@code true}.
         * <li>It is <i>symmetric</i>: for any non-null reference values
         * {@code x} and {@code y}, {@code x.equals(y)}
         * should return {@code true} if and only if
         * {@code y.equals(x)} returns {@code true}.
         * <li>It is <i>transitive</i>: for any non-null reference values
         * {@code x}, {@code y}, and {@code z}, if
         * {@code x.equals(y)} returns {@code true} and
         * {@code y.equals(z)} returns {@code true}, then
         * {@code x.equals(z)} should return {@code true}.
         * <li>It is <i>consistent</i>: for any non-null reference values
         * {@code x} and {@code y}, multiple invocations of
         * {@code x.equals(y)} consistently return {@code true}
         * or consistently return {@code false}, provided no
         * information used in {@code equals} comparisons on the
         * objects is modified.
         * <li>For any non-null reference value {@code x},
         * {@code x.equals(null)} should return {@code false}.
         * </ul>
         * <p>
         * The {@code equals} method for class {@code Object} implements
         * the most discriminating possible equivalence relation on objects;
         * that is, for any non-null reference values {@code x} and
         * {@code y}, this method returns {@code true} if and only
         * if {@code x} and {@code y} refer to the same object
         * ({@code x == y} has the value {@code true}).
         * <p>
         * Note that it is generally necessary to override the {@code hashCode}
         * method whenever this method is overridden, so as to maintain the
         * general contract for the {@code hashCode} method, which states
         * that equal objects must have equal hash codes.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj
         * argument; {@code false} otherwise.
         * @see #hashCode()
         * @see
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MaxTps maxTps = (MaxTps) obj;
            return Objects.equals(maxTps.production, production) && Objects.equals(maxTps.sandbox, sandbox);
        }

        @Override
        public int hashCode() {
            int result = (int) (production ^ (production >>> 32));
            result = 31 * result + (int) (sandbox ^ (sandbox >>> 32));
            return result;
        }
    }

    /**
     * {@code Endpoint} builder static inner class.
     */
    public static final class Builder {
        private String id;
        private String endpointConfig;
        private MaxTps maxTps;
        private String security;

        public Builder() {
        }

        public Builder(Endpoint copy) {
            this.id = copy.id;
            this.endpointConfig = copy.endpointConfig;
            this.maxTps = copy.maxTps;
            this.security = copy.security;
        }

        /**
         * Sets the {@code id} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param id the {@code id} to set
         * @return a reference to this Builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the {@code endpointConfig} and returns a reference to this Builder so that the methods can be chained
         * together.
         *
         * @param endpointConfig the {@code endpointConfig} to set
         * @return a reference to this Builder
         */
        public Builder endpointConfig(String endpointConfig) {
            this.endpointConfig = endpointConfig;
            return this;
        }

        /**
         * Sets the {@code maxTps} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param maxTps the {@code maxTps} to set
         * @return a reference to this Builder
         */
        public Builder maxTps(MaxTps maxTps) {
            this.maxTps = maxTps;
            return this;
        }

        /**
         * Sets the {@code security} and returns a reference to this Builder so that the methods can be chained
         * together.
         *
         * @param security the {@code security} to set
         * @return a reference to this Builder
         */
        public Builder security(String security) {
            this.security = security;
            return this;
        }

        /**
         * Returns a {@code Endpoint} built from the parameters previously set.
         *
         * @return a {@code Endpoint} built with parameters of this {@code Endpoint.Builder}
         */
        public Endpoint build() {
            return new Endpoint(this);
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     * {@code x}, {@code x.equals(x)} should return
     * {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     * {@code x} and {@code y}, {@code x.equals(y)}
     * should return {@code true} if and only if
     * {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     * {@code x}, {@code y}, and {@code z}, if
     * {@code x.equals(y)} returns {@code true} and
     * {@code y.equals(z)} returns {@code true}, then
     * {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     * {@code x} and {@code y}, multiple invocations of
     * {@code x.equals(y)} consistently return {@code true}
     * or consistently return {@code false}, provided no
     * information used in {@code equals} comparisons on the
     * objects is modified.
     * <li>For any non-null reference value {@code x},
     * {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Endpoint endpoint = (Endpoint) obj;
        return Objects.equals(endpoint.id, id) &&
                Objects.equals(endpoint.endpointConfig, endpointConfig) &&
                Objects.equals(endpoint.security, security) &&
                Objects.equals(endpoint.maxTps, maxTps);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + endpointConfig.hashCode();
        result = 31 * result + maxTps.hashCode();
        result = 31 * result + security.hashCode();
        return result;
    }
}
