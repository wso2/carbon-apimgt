package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertMetadataDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CertificatesDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class CertificatesApiService {
    public abstract Response certificatesAliasContentGet(String alias);
    public abstract Response certificatesAliasDelete(String alias);
    public abstract Response certificatesAliasGet(String alias);
    public abstract Response certificatesAliasPut(InputStream certificateInputStream,Attachment certificateDetail,String alias);
    public abstract Response certificatesGet(Integer limit,Integer offset,String alias,String endpoint);
    public abstract Response certificatesPost(InputStream certificateInputStream,Attachment certificateDetail,String alias,String endpoint);
}

