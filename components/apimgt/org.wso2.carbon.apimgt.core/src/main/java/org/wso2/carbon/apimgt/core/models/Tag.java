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

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * Represents an instance of a Tag. Tags can be associated with other entities such as APIs and used for
 * categorizing sets of entities against a given tag.
 */
public final class Tag {
    private final String name;
    private final int count;

    private Tag(Builder builder) {
        name = builder.name;
        count = builder.count;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Tag tag = (Tag) o;
        return count == tag.count &&
                Objects.equals(name, tag.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, count);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("count", count)
                .toString();
    }

    /**
     * {@code Tag} builder static inner class.
     */
    public static final class Builder {
        private String name;
        private int count;

        public Builder() {
        }

        public Builder(Tag copy) {
            this.name = copy.name;
            this.count = copy.count;
        }

        /**
         * Sets the {@code name} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param name the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the {@code count} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param count the {@code count} to set
         * @return a reference to this Builder
         */
        public Builder count(int count) {
            this.count = count;
            return this;
        }

        /**
         * Returns a {@code Tag} built from the parameters previously set.
         *
         * @return a {@code Tag} built with parameters of this {@code Tag.Builder}
         */
        public Tag build() {
            return new Tag(this);
        }
    }
}
