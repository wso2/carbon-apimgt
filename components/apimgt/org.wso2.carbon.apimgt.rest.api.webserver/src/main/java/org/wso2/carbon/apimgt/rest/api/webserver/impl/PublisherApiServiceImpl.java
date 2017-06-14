package org.wso2.carbon.apimgt.rest.api.webserver.impl;

import org.wso2.carbon.apimgt.rest.api.webserver.*;
import org.wso2.carbon.apimgt.rest.api.webserver.dto.*;


import java.io.File;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.webserver.NotFoundException;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class PublisherApiServiceImpl extends PublisherApiService {
    @Override
    public Response publisherGet(String accept
            , String ifNoneMatch
            , String ifModifiedSince
            , Request request) throws NotFoundException {

        String rawUri = request.getUri();

        String[] parts = rawUri.split("/");
        // folder name ex: publisher or store or admin
        String context = parts[1];
        String filePath = "./deployment/webapps/" + context + "public/index.html";

        //#TODO read from config file
        if ("publisher_new".equals(context)) {
            if (rawUri.contains(".")) {
                filePath = "./deployment/webapps/" + rawUri;
            } else {
                filePath = "./deployment/webapps/" + context + "/public/index.html";
            }
        } else {

        }

        File file = new File(filePath);
        if (file.exists()) {
            return Response.ok(file).header("Cache-Control", "max-age=3600000000000, must-revalidate").build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();

    }

}

