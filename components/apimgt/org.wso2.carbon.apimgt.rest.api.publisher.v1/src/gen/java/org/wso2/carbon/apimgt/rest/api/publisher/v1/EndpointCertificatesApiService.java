package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertMetadataDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificatesDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class EndpointCertificatesApiService {
    public abstract Response endpointCertificatesAliasContentGet(String alias);
    public abstract Response endpointCertificatesAliasDelete(String alias);
    public abstract Response endpointCertificatesAliasGet(String alias);
    public abstract Response endpointCertificatesAliasPut(InputStream certificateInputStream,Attachment certificateDetail,String alias);
    public abstract Response endpointCertificatesGet(Integer limit,Integer offset,String alias,String endpoint);
    public abstract Response endpointCertificatesPost(InputStream certificateInputStream,Attachment certificateDetail,String alias,String endpoint);
}

