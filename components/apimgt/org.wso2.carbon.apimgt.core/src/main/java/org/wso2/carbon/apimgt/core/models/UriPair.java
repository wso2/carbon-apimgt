/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */
package org.wso2.carbon.apimgt.core.models;


import org.apache.commons.lang3.tuple.Pair;

/**
 * Used to keep map of uritemplate and scope
 */
public class UriPair extends Pair<UriTemplate, Scope> {

    private static final long serialVersionUID = 9167163605180068324L;
    private UriTemplate uriTemplate;
    private Scope scope;
    @Override
    public UriTemplate getLeft() {
        return uriTemplate;
    }

    @Override
    public Scope getRight() {
        return scope;
    }

    /**
     * Replaces the value corresponding to this entry with the specified
     * value (optional operation).  (Writes through to the map.)  The
     * behavior of this call is undefined if the mapping has already been
     * removed from the map (by the iterator's <tt>remove</tt> operation).
     *
     * @param value new value to be stored in this entry
     * @return old value corresponding to the entry
     * @throws UnsupportedOperationException if the <tt>put</tt> operation
     *                                       is not supported by the backing map
     * @throws ClassCastException            if the class of the specified value
     *                                       prevents it from being stored in the backing map
     * @throws NullPointerException          if the backing map does not permit
     *                                       null values, and the specified value is null
     * @throws IllegalArgumentException      if some property of this value
     *                                       prevents it from being stored in the backing map
     * @throws IllegalStateException         implementations may, but are not
     *                                       required to, throw this exception if the entry has been
     *                                       removed from the backing map.
     */
    @Override
    public Scope setValue(Scope value) {
        return null;
    }

    public UriPair(UriTemplate uriTemplate, Scope scope) {
        this.uriTemplate = uriTemplate;
        this.scope = scope;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        UriPair uriPair = (UriPair) o;

        if (uriTemplate != null ? !uriTemplate.equals(uriPair.uriTemplate) : uriPair.uriTemplate != null) {
            return false;
        }
        if (scope != null ? !scope.equals(uriPair.scope) : uriPair.scope != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uriTemplate != null ? uriTemplate.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        return result;
    }
}
