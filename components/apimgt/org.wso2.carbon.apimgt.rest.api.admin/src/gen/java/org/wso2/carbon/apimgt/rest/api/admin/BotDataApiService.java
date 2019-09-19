package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.EmailDTO;

import java.sql.SQLException;
import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class BotDataApiService {
    public abstract Response botDataAddEmailPost(EmailDTO body) throws APIManagementException, SQLException;
    public abstract Response botDataDeleteEmailDelete(String uuid) throws APIManagementException, SQLException;
    public abstract Response botDataGetEmailListGet(String tenantDomain) throws APIManagementException;
}

