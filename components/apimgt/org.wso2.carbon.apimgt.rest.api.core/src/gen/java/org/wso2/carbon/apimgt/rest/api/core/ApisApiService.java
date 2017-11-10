package org.wso2.carbon.apimgt.rest.api.core;

import org.wso2.carbon.apimgt.rest.api.core.*;
import org.wso2.carbon.apimgt.rest.api.core.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.core.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ErrorDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class ApisApiService {
    public abstract Response apisApiIdGatewayConfigGet(String apiId
 ,String accept
  ,Request request) throws NotFoundException;
    public abstract Response apisGet(String labels
 ,String status
  ,Request request) throws NotFoundException;
}
