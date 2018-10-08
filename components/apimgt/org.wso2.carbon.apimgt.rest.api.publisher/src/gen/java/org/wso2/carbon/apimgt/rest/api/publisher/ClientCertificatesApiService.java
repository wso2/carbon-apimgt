package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertMetadataDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ClientCertificatesDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ClientCertificatesApiService {
    public abstract Response clientCertificatesAliasContentGet(String alias);
    public abstract Response clientCertificatesAliasDelete(String alias);
    public abstract Response clientCertificatesAliasGet(String alias);
    public abstract Response clientCertificatesAliasPut(String alias,InputStream certificateInputStream,Attachment certificateDetail,String tier);
    public abstract Response clientCertificatesGet(Integer limit,Integer offset,String alias,String apiId);
    public abstract Response clientCertificatesPost(InputStream certificateInputStream,Attachment certificateDetail,String alias,String apiId,String tier);
}

