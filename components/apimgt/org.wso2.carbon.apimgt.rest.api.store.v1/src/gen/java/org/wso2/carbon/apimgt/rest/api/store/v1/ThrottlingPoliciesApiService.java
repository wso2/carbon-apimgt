package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ThrottlingPoliciesApiService {
    public abstract Response throttlingPoliciesPolicyLevelGet(String policyLevel,Integer limit,Integer offset,String ifNoneMatch,String xWSO2Tenant);
    public abstract Response throttlingPoliciesPolicyLevelPolicyIdGet(String policyId,String policyLevel,String xWSO2Tenant,String ifNoneMatch);
}

