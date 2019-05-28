package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ThrottlingPoliciesApiService {
      public Response throttlingPoliciesPolicyLevelGet(String policyLevel, Integer limit, Integer offset, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext);
      public Response throttlingPoliciesPolicyLevelPolicyIdGet(String policyId, String policyLevel, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
}
