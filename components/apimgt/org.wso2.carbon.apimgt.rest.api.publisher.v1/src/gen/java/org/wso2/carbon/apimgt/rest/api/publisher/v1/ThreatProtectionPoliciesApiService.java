package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ThreatProtectionPoliciesApiService {
    public abstract Response threatProtectionPoliciesGet();
    public abstract Response threatProtectionPoliciesPolicyIdGet(String policyId);
}

