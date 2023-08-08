package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTConsumerKeyListDTO;
import org.wso2.carbon.apimgt.internal.service.utils.*;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class RevokedconsumerkeysApiServiceImpl implements RevokedconsumerkeysApiService {

    public Response revokedconsumerkeysGet(MessageContext messageContext) {
        return Response.ok().entity(BlockConditionDBUtil.getRevokedJWTConsumerKeys()).build();
    }
}
