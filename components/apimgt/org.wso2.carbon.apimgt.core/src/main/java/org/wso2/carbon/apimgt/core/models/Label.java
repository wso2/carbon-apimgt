package org.wso2.carbon.apimgt.core.models;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.List;
import java.util.Objects;

/**
 * Represents an instance of a Label. Labels can be associated with other entities such as APIs and used for
 * categorizing sets of entities against a given label. When starting up gateway, label can be given.
 */
public final class Label {
    private final String id;
    private final String name;

    private final String description;
    private final List<String> accessUrls;
    private final String type;


    private Label(Builder builder) {
        id = builder.id;
        name = builder.name;
        description = builder.description;
        accessUrls = builder.accessUrls;
        type = builder.type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getAccessUrls() {
        return accessUrls;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
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
        return Objects.equals(id, label.id) &&
                Objects.equals(name, label.name) &&
                Objects.equals(type, label.type) &&
                APIUtils.isListsEqualIgnoreOrder(accessUrls, label.accessUrls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, accessUrls);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("accessUrls", accessUrls)
                .append("type", type)
                .toString();
    }


    /**
     * {@code Label} builder static inner class.
     */
    public static final class Builder {
        private String id;
        private String name;
        private String description;
        private List<String> accessUrls;
        private String type;

        public Builder() {
        }

        public Builder(Label label) {
            this.id = label.id;
            this.name = label.name;
            this.accessUrls = label.accessUrls;
            this.type = label.type;
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
         * Sets the {@code description} and returns a reference to this Builder so that the methods can be chained
         * together.
         *
         * @param description the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
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
         * Sets the {@code accessUrls} and returns a reference to this Builder so that the methods can be chained
         * together.
         *
         * @param accessUrls the {@code accessUrls} to set
         * @return a reference to this Builder
         */
        public Builder accessUrls(List<String> accessUrls) {
            this.accessUrls = accessUrls;
            return this;
        }

        /**
         * Sets the {@code type} and returns a reference to this Builder so that the methods can be chained
         * together.
         *
         * @param type the {@code type} to set
         * @return a reference to this Builder
         */
        public Builder type (String type) {
            this.type = type;
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
