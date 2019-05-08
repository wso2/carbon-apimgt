package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TierDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class PoliciesApiService {
    public abstract Response policiesMediationGet(Integer limit,Integer offset,String query,String ifNoneMatch);
    public abstract Response policiesPolicyLevelGet(String policyLevel,Integer limit,Integer offset,String ifNoneMatch);
    public abstract Response policiesPolicyLevelPolicyNameGet(String policyName,String policyLevel,String ifNoneMatch);
}

