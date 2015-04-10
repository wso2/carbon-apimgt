/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.woden.WSDLFactory;
import org.apache.woden.WSDLReader;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.wsdl.WSDLException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class CommonUtil {
    private static final Log log = LogFactory.getLog(CommonUtil.class);

    private static void handleException(String msg) throws APIManagementException {
        log.error(msg);
        throw new APIManagementException(msg);
    }

    private static void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

    public static void validateWsdl(String url) throws APIManagementException, IOException {
        BufferedReader in = null;
        try {
            URL wsdl = new URL(url);
            in = new BufferedReader(new InputStreamReader(wsdl.openStream()));

            String inputLine;
            boolean isWsdl2 = false;
            boolean isWsdl10 = false;
            StringBuilder urlContent = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
                String wsdl10NameSpace = "http://schemas.xmlsoap.org/wsdl/";
                urlContent.append(inputLine);
                isWsdl2 = urlContent.indexOf(wsdl2NameSpace) > 0;
                isWsdl10 = urlContent.indexOf(wsdl10NameSpace) > 0;
            }
            if (isWsdl10) {
                javax.wsdl.xml.WSDLReader wsdlReader11 = null;
                try {
                    wsdlReader11 = javax.wsdl.factory.WSDLFactory.newInstance().newWSDLReader();
                    wsdlReader11.readWSDL(url);
                } catch (WSDLException e) {
                    handleException("Unable to process wsdl 1.0", e);
                }

            } else if (isWsdl2) {
                WSDLReader wsdlReader20 = null;
                try {
                    wsdlReader20 = WSDLFactory.newInstance().newWSDLReader();
                    wsdlReader20.readWSDL(url);
                } catch (org.apache.woden.WSDLException e) {
                    handleException("Unable to process wsdl 2.0", e);
                }

            } else {
                handleException("URL is not in format of wsdl1/wsdl2");
            }
        } finally {
            in.close();
        }
    }

    /**
     * Validate the backend by sending HTTP HEAD
     *
     * @param urlVal - backend URL
     * @return - status of HTTP HEAD Request to backend
     */
    public static String sendHttpHEADRequest(String urlVal) {

        String response = "error while connecting";

        HttpClient client = new DefaultHttpClient();
        HttpHead head = new HttpHead(urlVal);
        client.getParams().setParameter("http.socket.timeout", 4000);
        client.getParams().setParameter("http.connection.timeout", 4000);


        if (System.getProperty(APIConstants.HTTP_PROXY_HOST) != null &&
                System.getProperty(APIConstants.HTTP_PROXY_PORT) != null) {
            if (log.isDebugEnabled()) {
                log.debug("Proxy configured, hence routing through configured proxy");
            }
            String proxyHost = System.getProperty(APIConstants.HTTP_PROXY_HOST);
            String proxyPort = System.getProperty(APIConstants.HTTP_PROXY_PORT);
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
                    new HttpHost(proxyHost, new Integer(proxyPort)));
        }

        try {
            HttpResponse httpResponse = client.execute(head);
            int statusCode = httpResponse.getStatusLine().getStatusCode();

            //If the endpoint doesn't support HTTP HEAD or if status code is < 400
            if (statusCode == 405 || statusCode % 100 < 4) {
                if (log.isDebugEnabled() && statusCode == 405) {
                    log.debug("Endpoint doesn't support HTTP HEAD");
                }
                response = "success";
            }
        } catch (IOException e) {
            // sending a default error message.
            log.error("Error occurred while connecting backend : " + urlVal + ", reason : " + e.getMessage());
        } finally {
            client.getConnectionManager().shutdown();
        }
        return response;
    }

}
