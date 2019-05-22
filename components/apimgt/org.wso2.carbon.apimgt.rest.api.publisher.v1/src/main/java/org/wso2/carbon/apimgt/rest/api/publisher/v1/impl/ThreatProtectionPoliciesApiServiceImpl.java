package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThreatProtectionPolicyListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;


public class ThreatProtectionPoliciesApiServiceImpl implements ThreatProtectionPoliciesApiService {
      public Response threatProtectionPoliciesGet(MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response threatProtectionPoliciesPolicyIdGet(String policyId, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
