package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.msf4j.formparam.FileInfo;
import java.io.InputStream;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-19T18:57:34.679+05:30")
public abstract class ImportApiService {
    public abstract Response importApisPut(InputStream fileInputStream, FileInfo fileDetail,String contentType)
            throws NotFoundException;
}
