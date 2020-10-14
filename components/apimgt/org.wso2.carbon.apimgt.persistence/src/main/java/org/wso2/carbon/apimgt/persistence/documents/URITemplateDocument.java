package org.wso2.carbon.apimgt.persistence.documents;

import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Scope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class URITemplateDocument {

    private String uriTemplate;
    private String resourceURI;
    private String resourceSandboxURI;
    private String httpVerb;
    private String authType;
    private LinkedHashSet<String> httpVerbs = new LinkedHashSet<String>();
    private List<String> authTypes = new ArrayList<String>();
    private List<String> throttlingConditions = new ArrayList<String>();
    private String applicableLevel;
    private String throttlingTier;
    private List<String> throttlingTiers = new ArrayList<String>();
    private Scope scope;
    private String mediationScript;
    private List<Scope> scopes = new ArrayList<Scope>();
    private Map<String, String> mediationScripts = new HashMap<String, String>();
    private ConditionGroupDTO[] conditionGroups;
    private int id;
    private Set<APIProductIdentifierDocument> usedByProducts = new HashSet<>();
    private String amznResourceName;
    private int amznResourceTimeout;

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public String getResourceSandboxURI() {
        return resourceSandboxURI;
    }

    public void setResourceSandboxURI(String resourceSandboxURI) {
        this.resourceSandboxURI = resourceSandboxURI;
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

    public LinkedHashSet<String> getHttpVerbs() {
        return httpVerbs;
    }

    public void setHttpVerbs(LinkedHashSet<String> httpVerbs) {
        this.httpVerbs = httpVerbs;
    }

    public List<String> getAuthTypes() {
        return authTypes;
    }

    public void setAuthTypes(List<String> authTypes) {
        this.authTypes = authTypes;
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

    public String getThrottlingTier() {
        return throttlingTier;
    }

    public void setThrottlingTier(String throttlingTier) {
        this.throttlingTier = throttlingTier;
    }

    public List<String> getThrottlingTiers() {
        return throttlingTiers;
    }

    public void setThrottlingTiers(List<String> throttlingTiers) {
        this.throttlingTiers = throttlingTiers;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public String getMediationScript() {
        return mediationScript;
    }

    public void setMediationScript(String mediationScript) {
        this.mediationScript = mediationScript;
    }

    public List<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(List<Scope> scopes) {
        this.scopes = scopes;
    }

    public Map<String, String> getMediationScripts() {
        return mediationScripts;
    }

    public void setMediationScripts(Map<String, String> mediationScripts) {
        this.mediationScripts = mediationScripts;
    }

    public ConditionGroupDTO[] getConditionGroups() {
        return conditionGroups;
    }

    public void setConditionGroups(ConditionGroupDTO[] conditionGroups) {
        this.conditionGroups = conditionGroups;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Set<APIProductIdentifierDocument> getUsedByProducts() {
        return usedByProducts;
    }

    public void setUsedByProducts(Set<APIProductIdentifierDocument> usedByProducts) {
        this.usedByProducts = usedByProducts;
    }

    public String getAmznResourceName() {
        return amznResourceName;
    }

    public void setAmznResourceName(String amznResourceName) {
        this.amznResourceName = amznResourceName;
    }

    public int getAmznResourceTimeout() {
        return amznResourceTimeout;
    }

    public void setAmznResourceTimeout(int amznResourceTimeout) {
        this.amznResourceTimeout = amznResourceTimeout;
    }
}
