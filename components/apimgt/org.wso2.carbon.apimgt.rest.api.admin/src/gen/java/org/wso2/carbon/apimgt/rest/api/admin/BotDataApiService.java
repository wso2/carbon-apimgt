package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.EmailDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class BotDataApiService {
    public abstract Response botDataAddEmailPost(EmailDTO body);
    public abstract Response botDataDeleteEmailDelete(String uuid);
    public abstract Response botDataGetEmailListGet(String tenantDomain);
}

