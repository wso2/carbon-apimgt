package org.wso2.carbon.apimgt.core.models;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

/**
 * Represents an instance of a Label. Labels can be associated with other entities such as APIs and used for
 * categorizing sets of entities against a given label. When starting up gateway, label can be given.
 */
public final class Label {
    private final String name;
    private final String accessUrl;

    private Label(Builder builder) {
        name = builder.name;
        accessUrl = builder.accessUrl;
    }

    public String getName() {
        return name;
    }

    public String getAccessUrl() {
        return accessUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Label label = (Label) o;
        return Objects.equals(name, label.name) &&
                Objects.equals(accessUrl, label.accessUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, accessUrl);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("name", name)
                .append("accessUrl", accessUrl)
                .toString();
    }

    /**
     * {@code Label} builder static inner class.
     */
    public static final class Builder {
        private String name;
        private String accessUrl;

        public Builder() {
        }

        public Builder(Label label) {
            this.name = label.name;
            this.accessUrl = label.accessUrl;
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
         * Sets the {@code accessUrl} and returns a reference to this Builder so that the methods can be chained
         * together.
         *
         * @param accessUrl the {@code accessUrl} to set
         * @return a reference to this Builder
         */
        public Builder accessUrl(String accessUrl) {
            this.accessUrl = accessUrl;
            return this;
        }

        /**
         * Returns a {@code Label} built from the parameters previously set.
         *
         * @return a {@code Label} built with parameters of this {@code Label.Builder}
         */
        public Label build() {
            return new Label(this);
        }
    }
}
