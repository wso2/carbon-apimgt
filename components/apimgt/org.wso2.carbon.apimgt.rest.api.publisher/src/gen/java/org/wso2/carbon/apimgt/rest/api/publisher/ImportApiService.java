package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import java.io.File;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-04T15:48:35.633+05:30")
public abstract class ImportApiService {
    public abstract Response importApisPost(InputStream fileInputStream, FileInfo fileDetail
 ,String contentType
 ,String provider
 ) throws NotFoundException;
    public abstract Response importApisPut(InputStream fileInputStream, FileInfo fileDetail
 ,String contentType
 ,String provider
 ) throws NotFoundException;
}
