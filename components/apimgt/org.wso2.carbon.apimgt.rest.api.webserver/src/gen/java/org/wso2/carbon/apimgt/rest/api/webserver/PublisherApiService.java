package org.wso2.carbon.apimgt.rest.api.webserver;

import org.wso2.carbon.apimgt.rest.api.webserver.*;
import org.wso2.carbon.apimgt.rest.api.webserver.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.webserver.dto.ErrorDTO;
import java.io.File;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.webserver.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class PublisherApiService {
    public abstract Response publisherGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
}
