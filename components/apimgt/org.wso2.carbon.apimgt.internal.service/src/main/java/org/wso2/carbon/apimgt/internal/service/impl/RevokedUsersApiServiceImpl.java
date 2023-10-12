package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTUserListDTO;
import org.wso2.carbon.apimgt.internal.service.utils.BlockConditionDBUtil;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class RevokedUsersApiServiceImpl implements RevokedUsersApiService {

    public Response revokedUsersGet(MessageContext messageContext) {
        return Response.ok().entity(BlockConditionDBUtil.getRevokedJWTUsers()).build();
    }
}
