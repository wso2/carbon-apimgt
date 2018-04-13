package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * BaseAPIInfoDTO
 */
public class BaseAPIInfoDTO {
    @SerializedName("id")
    private String id = null;

    @SerializedName("name")
    private String name = null;

    @SerializedName("description")
    private String description = null;

    @SerializedName("context")
    private String context = null;

    @SerializedName("version")
    private String version = null;

    @SerializedName("hasOwnGateway")
    private Boolean hasOwnGateway = null;

    @SerializedName("provider")
    private String provider = null;

    /**
     * Gets or Sets type
     */
    public enum TypeEnum {
        @SerializedName("APIInfo")
        APIINFO("APIInfo"),

        @SerializedName("CompositeAPIInfo")
        COMPOSITEAPIINFO("CompositeAPIInfo");

        private String value;

        TypeEnum(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public static TypeEnum fromValue(String text) {
            for (TypeEnum b : TypeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            return null;
        }
    }

    @SerializedName("type")
    private TypeEnum type = null;

    public BaseAPIInfoDTO id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     *
     * @return id
     **/
    @ApiModelProperty(value = "")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BaseAPIInfoDTO name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
     *
     * @return name
     **/
    @ApiModelProperty(required = true, value = "")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BaseAPIInfoDTO description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get description
     *
     * @return description
     **/
    @ApiModelProperty(value = "")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BaseAPIInfoDTO context(String context) {
        this.context = context;
        return this;
    }

    /**
     * Get context
     *
     * @return context
     **/
    @ApiModelProperty(required = true, value = "")
    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public BaseAPIInfoDTO version(String version) {
        this.version = version;
        return this;
    }

    /**
     * Get version
     *
     * @return version
     **/
    @ApiModelProperty(required = true, value = "")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public BaseAPIInfoDTO hasOwnGateway(Boolean hasOwnGateway) {
        this.hasOwnGateway = hasOwnGateway;
        return this;
    }

    /**
     * Get hasOwnGateway
     *
     * @return hasOwnGateway
     **/
    @ApiModelProperty(value = "")
    public Boolean getHasOwnGateway() {
        return hasOwnGateway;
    }

    public void setHasOwnGateway(Boolean hasOwnGateway) {
        this.hasOwnGateway = hasOwnGateway;
    }

    public BaseAPIInfoDTO provider(String provider) {
        this.provider = provider;
        return this;
    }

    /**
     * If the provider value is not given, the user invoking the API will be used as the provider.
     *
     * @return provider
     **/
    @ApiModelProperty(required = true, value = "If the provider value is not given, the user invoking the API will be" +
            " used as the provider. ")
    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public BaseAPIInfoDTO type(TypeEnum type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     *
     * @return type
     **/
    @ApiModelProperty(required = true, value = "")
    public TypeEnum getType() {
        return type;
    }

    public void setType(TypeEnum type) {
        this.type = type;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseAPIInfoDTO baseAPIInfo = (BaseAPIInfoDTO) o;
        return Objects.equals(this.id, baseAPIInfo.id) &&
                Objects.equals(this.name, baseAPIInfo.name) &&
                Objects.equals(this.description, baseAPIInfo.description) &&
                Objects.equals(this.context, baseAPIInfo.context) &&
                Objects.equals(this.version, baseAPIInfo.version) &&
                Objects.equals(this.hasOwnGateway, baseAPIInfo.hasOwnGateway) &&
                Objects.equals(this.provider, baseAPIInfo.provider) &&
                Objects.equals(this.type, baseAPIInfo.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, context, version, hasOwnGateway, provider, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BaseAPIInfoDTO {\n");

        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    description: ").append(toIndentedString(description)).append("\n");
        sb.append("    context: ").append(toIndentedString(context)).append("\n");
        sb.append("    version: ").append(toIndentedString(version)).append("\n");
        sb.append("    hasOwnGateway: ").append(toIndentedString(hasOwnGateway)).append("\n");
        sb.append("    provider: ").append(toIndentedString(provider)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

