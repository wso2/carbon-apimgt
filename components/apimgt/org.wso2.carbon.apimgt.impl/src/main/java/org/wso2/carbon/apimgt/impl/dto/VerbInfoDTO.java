package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.impl.APIConstants;

import org.wso2.carbon.apimgt.api.dto.ConditionDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class VerbInfoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String httpVerb;

    private String authType;

    private String throttling;

    private String applicableLevel;

    private List<String> throttlingConditions = new ArrayList<String>();

    private String requestKey;

    private ConditionGroupDTO[] conditionGroups;
    
    private boolean contentAware;

    private BackendOperationMapping backendAPIOperationMapping;

    public VerbInfoDTO() {
    }

    /**
     * Deep clone constructor. Creates a new VerbInfoDTO as a deep copy of the given instance.
     *
     * @param other the VerbInfoDTO to deep clone
     */
    public VerbInfoDTO(VerbInfoDTO other) {
        this.httpVerb = other.httpVerb;
        this.authType = other.authType;
        this.throttling = other.throttling;
        this.applicableLevel = other.applicableLevel;
        this.requestKey = other.requestKey;
        this.contentAware = other.contentAware;
        this.throttlingConditions = other.throttlingConditions != null
                ? new ArrayList<>(other.throttlingConditions) : new ArrayList<>();
        if (other.conditionGroups != null) {
            this.conditionGroups = new ConditionGroupDTO[other.conditionGroups.length];
            for (int i = 0; i < other.conditionGroups.length; i++) {
                ConditionGroupDTO src = other.conditionGroups[i];
                if (src != null) {
                    ConditionGroupDTO copy = new ConditionGroupDTO();
                    copy.setConditionGroupId(src.getConditionGroupId());
                    if (src.getConditions() != null) {
                        ConditionDTO[] condCopy = new ConditionDTO[src.getConditions().length];
                        for (int j = 0; j < src.getConditions().length; j++) {
                            ConditionDTO c = src.getConditions()[j];
                            if (c != null) {
                                ConditionDTO cd = new ConditionDTO();
                                cd.setConditionName(c.getConditionName());
                                cd.setConditionType(c.getConditionType());
                                cd.setConditionValue(c.getConditionValue());
                                cd.isInverted(c.isInverted());
                                condCopy[j] = cd;
                            }
                        }
                        copy.setConditions(condCopy);
                    }
                    this.conditionGroups[i] = copy;
                }
            }
        }
        if (other.backendAPIOperationMapping != null) {
            BackendOperationMapping bom = new BackendOperationMapping();
            bom.setBackendId(other.backendAPIOperationMapping.getBackendId());
            if (other.backendAPIOperationMapping.getBackendOperation() != null) {
                org.wso2.carbon.apimgt.api.model.BackendOperation bo =
                        new org.wso2.carbon.apimgt.api.model.BackendOperation();
                bo.setTarget(other.backendAPIOperationMapping.getBackendOperation().getTarget());
                bo.setVerb(other.backendAPIOperationMapping.getBackendOperation().getVerb());
                bo.setRefUriMappingId(other.backendAPIOperationMapping.getBackendOperation().getRefUriMappingId());
                bom.setBackendOperation(bo);
            }
            this.backendAPIOperationMapping = bom;
        }
    }

    public String getThrottling() {
        return throttling;
    }

    public void setThrottling(String throttling) {
        this.throttling = throttling;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public boolean requiresAuthentication() {
        return !APIConstants.AUTH_TYPE_NONE.equalsIgnoreCase(authType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VerbInfoDTO that = (VerbInfoDTO) o;

        if (httpVerb != null ? !httpVerb.equals(that.getHttpVerb()) : that.getHttpVerb() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return httpVerb != null ? httpVerb.hashCode() : 0;
    }


    public List<String> getThrottlingConditions() {
        return throttlingConditions;
    }

    public void setThrottlingConditions(List<String> throttlingConditions) {
        this.throttlingConditions = throttlingConditions;
    }

    public String getApplicableLevel() {
        return applicableLevel;
    }

    public void setApplicableLevel(String applicableLevel) {
        this.applicableLevel = applicableLevel;
    }

    public void setConditionGroups(ConditionGroupDTO[] conditionGroups) {
        this.conditionGroups = conditionGroups;
    }

    public ConditionGroupDTO[] getConditionGroups() {
        return conditionGroups;
    }
    
    public boolean isContentAware() {
        return contentAware;
    }

    public void setContentAware(boolean contentAware) {
        this.contentAware = contentAware;
    }

    public BackendOperationMapping getBackendAPIOperationMapping() {
        return backendAPIOperationMapping;
    }

    public void setBackendOperationMapping(BackendOperationMapping backendAPIOperationMapping) {
        this.backendAPIOperationMapping = backendAPIOperationMapping;
    }
}
