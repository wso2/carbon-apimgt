package org.wso2.carbon.apimgt.rest.api.webserver.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.rest.api.webserver.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.webserver.PublisherApiService;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Locale;

public class PublisherApiServiceImpl extends PublisherApiService {

    private static final Logger log = LoggerFactory.getLogger(PublisherApiServiceImpl.class);

    @Override public Response publisherGet(String accept, String ifNoneMatch, String ifModifiedSince, Request request)
            throws NotFoundException {

        String rawUri = request.getUri();
        String hostName = String.valueOf(request.getProperties().get("HOST"));
        String port = String.valueOf(request.getProperties().get("LISTENER_PORT"));
        String protocol = String.valueOf(request.getProperties().get("PROTOCOL"));
        String absURL = protocol + "://" + hostName + ":" + port + rawUri;
        String path;
        try {
            URL requestURL = new URL(absURL);
            path = requestURL.getPath();
        } catch (MalformedURLException e) {
            String errorMessage = "Invalid URL, URL parsing error for : " + absURL;
            log.error(errorMessage, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        String[] apps = {"publisher","store_new", "admin"};
        String[] parts = path.split("/");
        // folder name ex: publisher or store or admin
        String context = parts[1];
        String filePath = "./deployment/webapps/" + context + "public/index.html";

        for (String app : apps) {
            if (context.toLowerCase(Locale.ENGLISH).equals(app)) {
                context = app;
                path = "/" + context + "/" + String.join("/", Arrays.copyOfRange(parts, 2, parts.length));
            }
            //#TODO read from config file
        /* TODO: Check the dot containment in last segment separated by '/' or use regex to capture file extension */
            if (app.equals(context)) {
                if (rawUri.split("\\?")[0].contains(".")) {
                    filePath = "./deployment/webapps" + path;
                } else {
                    filePath = "./deployment/webapps/" + context + "/public/index.html";
                }
            }
        }



        File file = new File(filePath);
        if (file.exists()) {
            return Response.ok(file).header("Cache-Control", "max-age=3600000000000, must-revalidate").build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();

    }

}
