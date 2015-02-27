/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.nhttp;

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.transport.http.HTTPTransportReceiver;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.transport.nhttp.util.RESTUtil;
import org.apache.ws.commons.schema.XmlSchema;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;

/**
 * Default http Get processor implementation for Synapse.
 */
public class DefaultHttpGetProcessor implements HttpGetRequestProcessor {
    private static final Log log = LogFactory.getLog(DefaultHttpGetProcessor.class);

    private static final String LOCATION = "Location";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String TEXT_HTML = "text/html";
    private static final String TEXT_XML = "text/xml";

    protected ConfigurationContext cfgCtx;

    protected ServerHandler serverHandler;

    public void init(ConfigurationContext cfgCtx, ServerHandler serverHandler) throws AxisFault {
        this.cfgCtx = cfgCtx;
        this.serverHandler = serverHandler;
    }

    /**
     * Process the HTTP GET request.
     *
     * @param request    The HttpRequest
     * @param response   The HttpResponse
     * @param msgContext The MessageContext
     * @param conn       The NHttpServerConnection
     * @param os         The OutputStream
     */
    public void process(HttpRequest request,
                        HttpResponse response,
                        MessageContext msgContext,
                        NHttpServerConnection conn,
                        OutputStream os,
                        boolean isRestDispatching) {

        String uri = request.getRequestLine().getUri();

        String servicePath = cfgCtx.getServiceContextPath();
        if (!servicePath.startsWith("/")) {
            servicePath = "/" + servicePath;
        }

        String serviceName = getServiceName(request);

        Map<String, String> parameters = new HashMap<String, String>();
        int pos = uri.indexOf("?");
        if (pos != -1) {
            msgContext.setTo(new EndpointReference(uri.substring(0, pos)));
            StringTokenizer st = new StringTokenizer(uri.substring(pos + 1), "&");
            while (st.hasMoreTokens()) {
                String param = st.nextToken();
                pos = param.indexOf("=");
                if (pos != -1) {
                    parameters.put(param.substring(0, pos), param.substring(pos + 1));
                } else {
                    parameters.put(param, null);
                }
            }
        } else {
            msgContext.setTo(new EndpointReference(uri));
        }

        if (isServiceListBlocked(uri)) {
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            serverHandler.commitResponseHideExceptions(conn,  response);
        } else if (uri.equals("/favicon.ico")) {
            response.setStatusCode(HttpStatus.SC_MOVED_PERMANENTLY);
            response.addHeader(LOCATION, "http://wso2.org/favicon.ico");
            serverHandler.commitResponseHideExceptions(conn, response);

//        } else if (!uri.startsWith(servicePath)) {
//            response.setStatusCode(HttpStatus.SC_MOVED_PERMANENTLY);
//            response.addHeader(LOCATION, servicePath + "/");
//            serverHandler.commitResponseHideExceptions(conn, response);

        } else if (serviceName != null && parameters.containsKey("wsdl")) {
            generateWsdl(request, response, msgContext,
                    conn, os, serviceName, parameters, isRestDispatching);
            return;
        } else if (serviceName != null && parameters.containsKey("wsdl2")) {
            generateWsdl2(request, response, msgContext,
                    conn, os, serviceName, isRestDispatching);
            return;
        } else if (serviceName != null && parameters.containsKey("xsd")) {
            generateXsd(request, response, msgContext, conn, os, serviceName, parameters, isRestDispatching);
            return;
        } else if (serviceName != null && parameters.containsKey("info")) {
            generateServiceDetailsPage(response, conn, os, serviceName);
        } else if (uri.startsWith(servicePath) &&
                (serviceName == null || serviceName.length() == 0)) {
            generateServicesList(response, conn, os, servicePath);
        } else {
            processGetAndDelete(request, response, msgContext,
                    conn, os, "GET", isRestDispatching);
            return;
        }

        // make sure that the output stream is flushed and closed properly
        closeOutputStream(os);
    }

    private void closeOutputStream(OutputStream os) {
        try {
            os.flush();
            os.close();
        } catch (IOException ignore) {
        }
    }

    /**
     * Is the incoming URI is requesting service list and http.block_service_list=true in
     * nhttp.properties
     * @param incomingURI incoming URI
     * @return whether to proceed with incomingURI

     */
    protected boolean isServiceListBlocked(String incomingURI) {
        String isBlocked = NHttpConfiguration.getInstance().isServiceListBlocked();

        return (("/services").equals(incomingURI) || ("/services" + "/").equals(incomingURI)) &&
               Boolean.parseBoolean(isBlocked);
    }

    /**
     * Returns the service name.
     *
     * @param request HttpRequest
     * @return service name as a String
     */
    protected String getServiceName(HttpRequest request) {
        String uri = request.getRequestLine().getUri();

        String servicePath = cfgCtx.getServiceContextPath();
        if (!servicePath.startsWith("/")) {
            servicePath = "/" + servicePath;
        }

        String serviceName = null;
        if (uri.startsWith(servicePath)) {
            serviceName = uri.substring(servicePath.length());
            if (serviceName.startsWith("/")) {
                serviceName = serviceName.substring(1);
            }
            if (serviceName.indexOf("?") != -1) {
                serviceName = serviceName.substring(0, serviceName.indexOf("?"));
            }
        } else {
            // this may be a custom URI
            String incomingURI = request.getRequestLine().getUri();

            Map serviceURIMap = (Map) cfgCtx.getProperty(NhttpConstants.EPR_TO_SERVICE_NAME_MAP);
            if (serviceURIMap != null) {
                Set keySet = serviceURIMap.keySet();
                for (Object key : keySet) {
                    if (incomingURI.toLowerCase().contains(((String) key).toLowerCase())) {
                        return (String) serviceURIMap.get(key);
                    }
                }
            }
        }

        if (serviceName != null) {
            int opnStart = serviceName.indexOf("/");
            if (opnStart != -1) {
                serviceName = serviceName.substring(0, opnStart);
            }
        }
        return serviceName;
    }

    /**
     * Generates the services list.
     *
     * @param response    HttpResponse
     * @param conn        NHttpServerConnection
     * @param os          OutputStream
     * @param servicePath service path of the service
     */
    protected void generateServicesList(HttpResponse response,
                                        NHttpServerConnection conn,
                                        OutputStream os, String servicePath) {
        try {
            byte[] bytes = getServicesHTML(
                    servicePath.endsWith("/") ? "" : servicePath + "/").getBytes();
            response.addHeader(CONTENT_TYPE, TEXT_HTML);
            serverHandler.commitResponseHideExceptions(conn, response);
            os.write(bytes);

        } catch (IOException e) {
            handleBrowserException(response, conn, os,
                    "Error generating services list", e);
        }
    }

    /**
     * Generates service details page.
     *
     * @param response    HttpResponse
     * @param conn        NHttpServerConnection
     * @param os          OutputStream
     * @param serviceName service name
     */
    protected void generateServiceDetailsPage(HttpResponse response,
                                              NHttpServerConnection conn,
                                              OutputStream os, String serviceName) {
        AxisService service = cfgCtx.getAxisConfiguration().
                getServices().get(serviceName);
        if (service != null) {
            String parameterValue = (String) service.getParameterValue("serviceType");
            if ("proxy".equals(parameterValue) && !isWSDLProvidedForProxyService(service)) {
                handleBrowserException(response, conn, os,
                        "No WSDL was provided for the Service " + serviceName +
                                ". A WSDL cannot be generated.", null);
            }
            try {
                byte[] bytes =
                        HTTPTransportReceiver.printServiceHTML(serviceName, cfgCtx).getBytes();
                response.addHeader(CONTENT_TYPE, TEXT_HTML);
                serverHandler.commitResponseHideExceptions(conn, response);
                os.write(bytes);

            } catch (IOException e) {
                handleBrowserException(response, conn, os,
                        "Error generating service details page for : " + serviceName, e);
            }
        } else {
            handleBrowserException(response, conn, os,
                    "Invalid service : " + serviceName, null);
        }
    }

    /**
     * Generates Schema.
     *
     * @param response    HttpResponse
     * @param conn        NHttpServerConnection
     * @param os          OutputStream
     * @param serviceName service name
     * @param parameters  url parameters
     */
    protected void generateXsd(HttpRequest request, HttpResponse response,
                               MessageContext messageCtx, NHttpServerConnection conn,
                               OutputStream os, String serviceName,
                               Map<String, String> parameters, boolean isRestDispatching) {
        if (parameters.get("xsd") == null || "".equals(parameters.get("xsd"))) {
            AxisService service = cfgCtx.getAxisConfiguration()
                    .getServices().get(serviceName);
            if (service != null) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    service.printSchema(baos);
                    response.addHeader(CONTENT_TYPE, TEXT_XML);
                    serverHandler.commitResponseHideExceptions(conn, response);
                    os.write(baos.toByteArray());
                    closeOutputStream(os);

                } catch (Exception e) {
                    handleBrowserException(response, conn, os,
                            "Error generating ?xsd output for service : " + serviceName, e);
                }
            } else {
                processGetAndDelete(request, response, messageCtx, conn, os,
                        serviceName, isRestDispatching);
            }

        } else {
            //cater for named xsds - check for the xsd name
            String schemaName = parameters.get("xsd");
            AxisService service = cfgCtx.getAxisConfiguration()
                    .getServices().get(serviceName);

            if (service != null) {
                //run the population logic just to be sure
                service.populateSchemaMappings();
                //write out the correct schema
                Map schemaTable = service.getSchemaMappingTable();
                XmlSchema schema = (XmlSchema) schemaTable.get(schemaName);
                if (schema == null) {
                    int dotIndex = schemaName.indexOf('.');
                    if (dotIndex > 0) {
                        String schemaKey = schemaName.substring(0, dotIndex);
                        schema = (XmlSchema) schemaTable.get(schemaKey);
                    }
                }
                //schema found - write it to the stream
                if (schema != null) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        schema.write(baos);
                        response.addHeader(CONTENT_TYPE, TEXT_XML);
                        serverHandler.commitResponseHideExceptions(conn, response);
                        os.write(baos.toByteArray());
                        closeOutputStream(os);
                    } catch (Exception e) {
                        handleBrowserException(response, conn, os,
                                "Error generating named ?xsd output for service : " + serviceName, e);
                    }

                } else {
                    // no schema available by that name  - send 404
                    response.setStatusCode(HttpStatus.SC_NOT_FOUND);
                    closeOutputStream(os);
                }
            } else {
                processGetAndDelete(request, response, messageCtx, conn, os,
                        serviceName, isRestDispatching);
            }
        }
    }

    /**
     * Generate WSDL2.
     *
     * @param request           HttpRequest
     * @param response          HttpResponse
     * @param msgContext        MessageContext
     * @param conn              NHttpServerConnection
     * @param os                OutputStream
     * @param serviceName       service name
     * @param isRestDispatching weather nhttp should do rest dispatching
     */
    protected void generateWsdl2(HttpRequest request, HttpResponse response,
                                 MessageContext msgContext,
                                 NHttpServerConnection conn,
                                 OutputStream os, String serviceName, boolean isRestDispatching) {
        AxisService service = cfgCtx.getAxisConfiguration().
                getServices().get(serviceName);
        if (service != null) {
            String parameterValue = (String) service.getParameterValue("serviceType");
            if ("proxy".equals(parameterValue) && !isWSDLProvidedForProxyService(service)) {
                handleBrowserException(response, conn, os,
                        "No WSDL was provided for the Service " + serviceName +
                                ". A WSDL cannot be generated.", null);
            }
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                service.printWSDL2(baos, getIpAddress());
                response.addHeader(CONTENT_TYPE, TEXT_XML);
                serverHandler.commitResponseHideExceptions(conn, response);
                os.write(baos.toByteArray());
                closeOutputStream(os);

            } catch (Exception e) {
                handleBrowserException(response, conn, os,
                        "Error generating ?wsdl2 output for service : " + serviceName, e);
            }
        } else {
            processGetAndDelete(request, response, msgContext,
                    conn, os, "GET", isRestDispatching);
        }
    }

    /**
     * Generate WSDL.
     *
     * @param request           HttpRequest
     * @param response          HttpResponse
     * @param msgContext        MessageContext
     * @param conn              NHttpServerConnection
     * @param os                OutputStream
     * @param serviceName       service name
     * @param parameters        parameters
     * @param isRestDispatching if restDispatching is on
     */
    protected void generateWsdl(HttpRequest request, HttpResponse response,
                                MessageContext msgContext,
                                NHttpServerConnection conn,
                                OutputStream os, String serviceName,
                                Map<String, String> parameters, boolean isRestDispatching) {
        AxisService service = cfgCtx.getAxisConfiguration().
                getServices().get(serviceName);
        if (service != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String parameterValue = parameters.get("wsdl");
                if (parameterValue == null) {
                    service.printWSDL(baos, getIpAddress());
                } else {
                    // here the parameter value should be the wsdl file name
                    service.printUserWSDL(baos, parameterValue);
                }
                response.addHeader(CONTENT_TYPE, TEXT_XML);
                serverHandler.commitResponseHideExceptions(conn, response);
                os.write(baos.toByteArray());
                closeOutputStream(os);

            } catch (Exception e) {
                handleBrowserException(response, conn, os,
                        "Error generating ?wsdl output for service : " + serviceName, e);
            }
        } else {
            processGetAndDelete(request, response, msgContext,
                    conn, os, "GET", isRestDispatching);
        }
    }

    /**
     * Calls the RESTUtil to process GET and DELETE Request
     *
     * @param request           HttpRequest
     * @param response          HttpResponse
     * @param msgContext        MessageContext
     * @param conn              NHttpServerConnection
     * @param os                OutputStream
     * @param method            HTTP method, either GET or DELETE
     * @param isRestDispatching weather transport should do rest dispatching
     */
    protected void processGetAndDelete(HttpRequest request, HttpResponse response,
                                       MessageContext msgContext,
                                       NHttpServerConnection conn, OutputStream os,
                                       String method, boolean isRestDispatching) {
        try {
            RESTUtil.processGetAndDeleteRequest(
                    msgContext, os, request.getRequestLine().getUri(),
                    request.getFirstHeader(HTTP.CONTENT_TYPE), method, isRestDispatching);
            // do not let the output stream close (as by default below) since
            // we are serving this GET/DELETE request through the Synapse engine
        } catch (AxisFault axisFault) {
            handleException(response, msgContext, conn, os,
                    "Error processing " + method + " request for: " +
                            request.getRequestLine().getUri(), axisFault);
        }

    }

    /**
     * Handles exception.
     *
     * @param response   HttpResponse
     * @param msgContext MessageContext
     * @param conn       NHttpServerConnection
     * @param os         OutputStream
     * @param msg        message
     * @param e          Exception
     */
    protected void handleException(HttpResponse response, MessageContext msgContext,
                                   NHttpServerConnection conn,
                                   OutputStream os, String msg, Exception e) {
        if (e == null) {
            log.error(msg);
        } else {
            log.error(msg, e);
        }

        if (e == null) {
            e = new Exception(msg);
        }

        try {
            MessageContext faultContext = MessageContextBuilder.createFaultMessageContext(
                    msgContext, e);
            AxisEngine.sendFault(faultContext);

        } catch (Exception ex) {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.addHeader(CONTENT_TYPE, TEXT_XML);
            serverHandler.commitResponseHideExceptions(conn, response);

            try {
                os.write(msg.getBytes());
                if (ex != null) {
                    os.write(ex.getMessage().getBytes());
                }
            } catch (IOException ignore) {
            }

            if (conn != null) {
                try {
                    conn.shutdown();
                } catch (IOException ignore) {
                }
            }
        }
    }

    /**
     * Handles browser exception.
     *
     * @param response HttpResponse
     * @param conn     NHttpServerConnection
     * @param os       OutputStream
     * @param msg      message
     * @param e        Exception
     */
    protected void handleBrowserException(HttpResponse response,
                                          NHttpServerConnection conn, OutputStream os,
                                          String msg, Exception e) {
        if (e == null) {
            log.error(msg);
        } else {
            log.error(msg, e);
        }

        if (!response.containsHeader(HTTP.TRANSFER_ENCODING)) {
            response.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setReasonPhrase(msg);
            response.addHeader(CONTENT_TYPE, TEXT_HTML);
            serverHandler.commitResponseHideExceptions(conn, response);
            try {
                os.write(msg.getBytes());
                os.close();
            } catch (IOException ignore) {
            }
        }

        if (conn != null) {
            try {
                conn.shutdown();
            } catch (IOException ignore) {
            }
        }
    }

    /**
     * Checks whether a wsdl is provided for a proxy service.
     *
     * @param service AxisService
     * @return whether the wsdl is provided or not
     */
    protected boolean isWSDLProvidedForProxyService(AxisService service) {
        boolean isWSDLProvided = false;
        if (service.getParameterValue(WSDLConstants.WSDL_4_J_DEFINITION) != null ||
                service.getParameterValue(WSDLConstants.WSDL_20_DESCRIPTION) != null) {
            isWSDLProvided = true;
        }
        return isWSDLProvided;
    }

    /**
     * Whatever this method returns as the IP is ignored by the actual http/s listener when
     * its getServiceEPR is invoked. This was originally copied from axis2
     *
     * @return Returns String.
     * @throws java.net.SocketException if the socket can not be accessed
     */
    protected static String getIpAddress() throws SocketException {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        String address = "127.0.0.1";

        while (e.hasMoreElements()) {
            NetworkInterface netface = (NetworkInterface) e.nextElement();
            Enumeration addresses = netface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress ip = (InetAddress) addresses.nextElement();
                if (!ip.isLoopbackAddress() && isIP(ip.getHostAddress())) {
                    return ip.getHostAddress();
                }
            }
        }
        return address;
    }

    protected static boolean isIP(String hostAddress) {
        return hostAddress.split("[.]").length == 4;
    }

    /**
     * Returns the HTML text for the list of services deployed.
     * This can be delegated to another Class as well
     * where it will handle more options of GET messages.
     *
     * @param prefix to be used for the Service names
     * @return the HTML to be displayed as a String
     */
    protected String getServicesHTML(String prefix) {

        Map services = cfgCtx.getAxisConfiguration().getServices();
        Hashtable erroneousServices = cfgCtx.getAxisConfiguration().getFaultyServices();
        boolean servicesFound = false;

        StringBuffer resultBuf = new StringBuffer();
        resultBuf.append("<html><head><title>Axis2: Services</title></head>" + "<body>");

        if ((services != null) && !services.isEmpty()) {

            servicesFound = true;
            resultBuf.append("<h2>" + "Deployed services" + "</h2>");

            for (Object service : services.values()) {

                AxisService axisService = (AxisService) service;
                Parameter parameter = axisService.getParameter(
                        NhttpConstants.HIDDEN_SERVICE_PARAM_NAME);
                if (axisService.getName().startsWith("__") ||
                        (parameter != null && JavaUtils.isTrueExplicitly(parameter.getValue()))) {
                    continue;    // skip private services
                }

                Iterator iterator = axisService.getOperations();
                resultBuf.append("<h3><a href=\"").append(prefix).append(axisService.getName()).append(
                        "?wsdl\">").append(axisService.getName()).append("</a></h3>");

                if (iterator.hasNext()) {
                    resultBuf.append("Available operations <ul>");

                    for (; iterator.hasNext();) {
                        AxisOperation axisOperation = (AxisOperation) iterator.next();
                        resultBuf.append("<li>").append(
                                axisOperation.getName().getLocalPart()).append("</li>");
                    }
                    resultBuf.append("</ul>");
                } else {
                    resultBuf.append("No operations specified for this service");
                }
            }
        }

        if ((erroneousServices != null) && !erroneousServices.isEmpty()) {
            servicesFound = true;
            resultBuf.append("<hr><h2><font color=\"blue\">Faulty Services</font></h2>");
            Enumeration faultyservices = erroneousServices.keys();

            while (faultyservices.hasMoreElements()) {
                String faultyserviceName = (String) faultyservices.nextElement();
                resultBuf.append("<h3><font color=\"blue\">").append(
                        faultyserviceName).append("</font></h3>");
            }
        }

        if (!servicesFound) {
            resultBuf.append("<h2>There are no services deployed</h2>");
        }

        resultBuf.append("</body></html>");
        return resultBuf.toString();
    }

}
