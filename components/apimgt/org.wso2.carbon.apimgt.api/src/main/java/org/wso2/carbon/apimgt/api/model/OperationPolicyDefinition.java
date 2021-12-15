package org.wso2.carbon.apimgt.api.model;

import java.util.Objects;

public class OperationPolicyDefinition {

    private int policyId;
    private int templateId;
    private String apiId;
    private String revisionId;
    private String name;
    private String flow;
    private OperationPolicySpecification specification;
    private String definition;

    public int getPolicyId() {

        return policyId;
    }

    public void setPolicyId(int policyId) {

        this.policyId = policyId;
    }

    public int getTemplateId() {

        return templateId;
    }

    public void setTemplateId(int templateId) {

        this.templateId = templateId;
    }

    public String getApiId() {

        return apiId;
    }

    public void setApiId(String apiId) {

        this.apiId = apiId;
    }

    public String getRevisionId() {

        return revisionId;
    }

    public void setRevisionId(String revisionId) {

        this.revisionId = revisionId;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getFlow() {

        return flow;
    }

    public void setFlow(String flow) {

        this.flow = flow;
    }

    public OperationPolicySpecification getSpecification() {

        return specification;
    }

    public void setSpecification(OperationPolicySpecification specification) {

        this.specification = specification;
    }

    public String getDefinition() {

        return definition;
    }

    public void setDefinition(String definition) {

        this.definition = definition;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationPolicyDefinition that = (OperationPolicyDefinition) o;
        return Objects.equals(templateId, that.templateId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(flow, that.flow) &&
                Objects.equals(specification, that.specification) &&
                Objects.equals(definition, that.definition);
    }

    @Override
    public int hashCode() {

        return Objects.hash(templateId, name, flow, specification, definition);
    }
}
