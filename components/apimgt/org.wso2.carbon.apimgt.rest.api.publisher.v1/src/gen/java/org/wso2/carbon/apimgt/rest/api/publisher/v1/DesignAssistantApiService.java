package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantAPIPayloadResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantChatQueryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantChatResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantGenAPIPayloadDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface DesignAssistantApiService {
      public Response designAssistantApiPayloadGen(DesignAssistantGenAPIPayloadDTO designAssistantGenAPIPayloadDTO, MessageContext messageContext) throws APIManagementException;
      public Response designAssistantChat(DesignAssistantChatQueryDTO designAssistantChatQueryDTO, MessageContext messageContext) throws APIManagementException;
}
