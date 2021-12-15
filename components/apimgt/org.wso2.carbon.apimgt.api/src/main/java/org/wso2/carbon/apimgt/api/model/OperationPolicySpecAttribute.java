package org.wso2.carbon.apimgt.api.model;

public class OperationPolicySpecAttribute {

    private String attributeName;
    private String attributeDisplayName;
    private String attributeDescription;
    private String attributeValidationRegex;
    private String attributeType;
    private boolean required;

    public String getAttributeName() {

        return attributeName;
    }

    public void setAttributeName(String attributeName) {

        this.attributeName = attributeName;
    }

    public String getAttributeDisplayName() {

        return attributeDisplayName;
    }

    public void setAttributeDisplayName(String attributeDisplayName) {

        this.attributeDisplayName = attributeDisplayName;
    }

    public String getAttributeDescription() {

        return attributeDescription;
    }

    public void setAttributeDescription(String attributeDescription) {

        this.attributeDescription = attributeDescription;
    }

    public String getAttributeValidationRegex() {

        return attributeValidationRegex;
    }

    public void setAttributeValidationRegex(String attributeValidationRegex) {

        this.attributeValidationRegex = attributeValidationRegex;
    }

    public String getAttributeType() {

        return attributeType;
    }

    public void setAttributeType(String attributeType) {

        this.attributeType = attributeType;
    }

    public boolean isRequired() {

        return required;
    }

    public void setRequired(boolean required) {

        this.required = required;
    }
}
