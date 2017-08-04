package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import java.io.File;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class ImportApiService {
    public abstract Response importApisPost(InputStream fileInputStream, FileInfo fileDetail
 ,String provider
 , Request request) throws NotFoundException;
    public abstract Response importApisPut(InputStream fileInputStream, FileInfo fileDetail
 ,String provider
 , Request request) throws NotFoundException;
}
