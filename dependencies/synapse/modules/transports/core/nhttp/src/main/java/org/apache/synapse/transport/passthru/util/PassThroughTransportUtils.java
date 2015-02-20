/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.transport.passthru.util;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.Constants;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.AxisOperation;
import org.apache.http.protocol.HTTP;
import org.apache.http.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.nhttp.NhttpConstants;
import org.apache.synapse.transport.passthru.PassThroughConstants;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.NetworkInterface;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Utility methods used by the transport.
 */
public class PassThroughTransportUtils {
    private static Log log = LogFactory.getLog(PassThroughTransportUtils.class);

    /**
     * This method tries to determine the hostname of the given InetAddress without
     * triggering a reverse DNS lookup.  {@link java.net.InetAddress#getHostName()}
     * triggers a reverse DNS lookup which can be very costly in cases where reverse
     * DNS fails. Tries to parse a symbolic hostname from {@link java.net.InetAddress#toString()},
     * which is documented to return a String of the form "hostname / literal IP address"
     * with 'hostname' blank if not already computed & stored in <code>address</code>.
     * <p/>
     * If the hostname cannot be determined from InetAddress.toString(),
     * the value of {@link java.net.InetAddress#getHostAddress()} is returned.
     *
     * @param address The InetAddress whose hostname has to be determined
     * @return hostsname, if it can be determined. hostaddress, if not.          
     */
    public static String getHostName(InetAddress address) {
        String result;
        String hostAddress = address.getHostAddress();
        String inetAddr = address.toString();
        int index1 = inetAddr.lastIndexOf('/');
        int index2 = inetAddr.indexOf(hostAddress);
        if (index2 == index1 + 1) {
            if (index1 == 0) {
                result = hostAddress;
            } else {
                result = inetAddr.substring(0, index1);
            }
        } else {
            result = hostAddress;
        }
        return result;
    }

    /**
     * Get the EPR for the message passed in
     * @param msgContext the message context
     * @return the destination EPR
     */
    public static EndpointReference getDestinationEPR(MessageContext msgContext) {

        // Trasnport URL can be different from the WSA-To
        String transportURL = (String) msgContext.getProperty(
            Constants.Configuration.TRANSPORT_URL);

        if (transportURL != null) {
            return new EndpointReference(transportURL);
        } else if (
            (msgContext.getTo() != null) && !msgContext.getTo().hasAnonymousAddress()) {
            return msgContext.getTo();
        }
        return null;
    }

    /**
     * Remove unwanted headers from the http response of outgoing request. These are headers which
     * should be dictated by the transport and not the user. We remove these as these may get
     * copied from the request messages
     * 
     * @param msgContext the Axis2 Message context from which these headers should be removed
     * @param preserveServerHeader if true preserve the original server header
     * @param preserveUserAgentHeader if true preserve the original user-agent header
     */
    public static void removeUnwantedHeaders(MessageContext msgContext,
                                             boolean preserveServerHeader,
                                             boolean preserveUserAgentHeader) {
        Map headers = (Map) msgContext.getProperty(MessageContext.TRANSPORT_HEADERS);
   	
        if (headers == null || headers.isEmpty()) {
            return;
        }
        

        
        //a hack which takes the original content header
     	if(headers.get(HTTP.CONTENT_LEN) != null){
           msgContext.setProperty(PassThroughConstants.ORGINAL_CONTEN_LENGTH,headers.get(HTTP.CONTENT_LEN));
         }

        Iterator iter = headers.keySet().iterator();
        while (iter.hasNext()) {
            String headerName = (String) iter.next();
            if (HTTP.CONN_DIRECTIVE.equalsIgnoreCase(headerName) ||
                HTTP.TRANSFER_ENCODING.equalsIgnoreCase(headerName) ||
                HTTP.DATE_HEADER.equalsIgnoreCase(headerName) ||
                HTTP.CONTENT_LEN.equalsIgnoreCase(headerName) ||
                HTTP.CONN_KEEP_ALIVE.equalsIgnoreCase(headerName)) {
                iter.remove();
            }

            if (!preserveServerHeader && HTTP.SERVER_HEADER.equalsIgnoreCase(headerName)) {
                iter.remove();
            }

            if (!preserveUserAgentHeader && HTTP.USER_AGENT.equalsIgnoreCase(headerName)) {
                iter.remove();
            }
        }
    }

    /**
     * Determine the Http Status Code depending on the message type processed <br>
     * (normal response versus fault response) as well as Axis2 message context properties set
     * via Synapse configuration or MessageBuilders.
     *
     * @see PassThroughConstants#FAULTS_AS_HTTP_200
     * @see PassThroughConstants#HTTP_SC
     *
     * @param msgContext the Axis2 message context
     *
     * @return the HTTP status code to set in the HTTP response object
     */
    public static int determineHttpStatusCode(MessageContext msgContext) {

        int httpStatus = HttpStatus.SC_OK;

        // if this is a dummy message to handle http 202 case with non-blocking IO
        // set the status code to 202
        if (msgContext.isPropertyTrue(PassThroughConstants.SC_ACCEPTED)) {
            httpStatus = HttpStatus.SC_ACCEPTED;
        } else {
            // is this a fault message
            boolean handleFault = msgContext.getEnvelope() != null ?
                (msgContext.getEnvelope().getBody().hasFault() || msgContext.isProcessingFault()):false;

            // shall faults be transmitted with HTTP 200
            boolean faultsAsHttp200 =
                PassThroughConstants.TRUE.equals(
                    msgContext.getProperty(PassThroughConstants.FAULTS_AS_HTTP_200));

            // Set HTTP status code to 500 if this is a fault case and we shall not use HTTP 200
            if (handleFault && !faultsAsHttp200) {
                httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
            }

            // Any status code previously set shall be overwritten with the value of the following
            // message context property if it is set.
            Object statusCode = msgContext.getProperty(PassThroughConstants.HTTP_SC);
            if (statusCode != null) {
                try {
                    httpStatus = Integer.parseInt(
                            msgContext.getProperty(PassThroughConstants.HTTP_SC).toString());
                } catch (NumberFormatException e) {
                    log.warn("Unable to set the HTTP status code from the property "
                            + PassThroughConstants.HTTP_SC + " with value: " + statusCode);
                }
            }
        }

        return httpStatus;
    }

    /**
     * Whatever this method returns as the IP is ignored by the actual http/s listener when
     * its getServiceEPR is invoked. This was originally copied from axis2
     *
     * @return Returns String.
     * @throws java.net.SocketException if the socket can not be accessed
     */
    public static String getIpAddress() throws SocketException {
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

    private static boolean isIP(String hostAddress) {
        return hostAddress.split("[.]").length == 4;
    }


    /**
     * Returns the HTML text for the list of services deployed.
     * This can be delegated to another Class as well
     * where it will handle more options of GET messages.
     *
     * @param prefix to be used for the Service names
     * @param cfgCtx axis2 configuration context
     * @return the HTML to be displayed as a String
     */
    public String getServicesHTML(String prefix, ConfigurationContext cfgCtx) {

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
                        PassThroughConstants.HIDDEN_SERVICE_PARAM_NAME);
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

    public static OMOutputFormat getOMOutputFormat(MessageContext msgContext) {

    	OMOutputFormat format = null;
    	if(msgContext.getProperty(PassThroughConstants.MESSAGE_OUTPUT_FORMAT) != null){
    		format = (OMOutputFormat) msgContext.getProperty(PassThroughConstants.MESSAGE_OUTPUT_FORMAT);
    	}else{
    		format = new OMOutputFormat();
    	}
     
        msgContext.setDoingMTOM(TransportUtils.doWriteMTOM(msgContext));
        msgContext.setDoingSwA(TransportUtils.doWriteSwA(msgContext));
        msgContext.setDoingREST(TransportUtils.isDoingREST(msgContext));

        /**
               *  PassThroughConstants.INVOKED_REST set to true here if isDoingREST is true -
               *  this enables us to check whether the original request to the endpoint was a
               * REST request inside DefferedMessageBuilder (which we need to convert
               * text/xml content type into application/xml if the request was not a SOAP
               * request.
               */
        if(msgContext.getSoapAction() == null) {
            msgContext.setProperty(PassThroughConstants.INVOKED_REST, true);
        }
        format.setSOAP11(msgContext.isSOAP11());
        format.setDoOptimize(msgContext.isDoingMTOM());
        format.setDoingSWA(msgContext.isDoingSwA());

        format.setCharSetEncoding(TransportUtils.getCharSetEncoding(msgContext));
        Object mimeBoundaryProperty = msgContext.getProperty(Constants.Configuration.MIME_BOUNDARY);
        if (mimeBoundaryProperty != null) {
            format.setMimeBoundary((String) mimeBoundaryProperty);
        }

        return format;
    }

}
