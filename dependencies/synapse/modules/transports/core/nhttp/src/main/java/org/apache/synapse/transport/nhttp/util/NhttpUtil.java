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
package org.apache.synapse.transport.nhttp.util;

import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.http.HttpRequest;

import java.net.InetAddress;

/**
 * A useful set of utility methods for the HTTP transport
 */
public class NhttpUtil {

    /**
     * This method tries to determine the hostname of the given InetAddress without
     * triggering a reverse DNS lookup.  {@link java.net.InetAddress#getHostName()}
     * triggers a reverse DNS lookup which can be very costly in cases where reverse
     * DNS fails. Tries to parse a symbolic hostname from {@link java.net.InetAddress#toString()},
     * which is documented to return a String of the form "hostname / literal IP address"
     * with 'hostname' blank if not already computed & stored in <code>address</code<.
     * <p/>
     * If the hostname cannot be determined from InetAddress.toString(),
     * the value of {@link java.net.InetAddress#getHostAddress()} is returned.
     *
     * @param address The InetAddress whose hostname has to be determined
     * @return hostsname, if it can be determined. hostaddress, if not.
     *
     * TODO: We may introduce a System property or some other method of configuration
     * TODO: which will specify whether to allow reverse DNS lookup or not
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
     * Retirn the OMOutputFormat to be used for the message context passed in
     * @param msgContext the message context
     * @return the OMOutputFormat to be used
     */
    public static OMOutputFormat getOMOutputFormat(MessageContext msgContext) {

        OMOutputFormat format = new OMOutputFormat();
        msgContext.setDoingMTOM(TransportUtils.doWriteMTOM(msgContext));
        msgContext.setDoingSwA(TransportUtils.doWriteSwA(msgContext));
        msgContext.setDoingREST(TransportUtils.isDoingREST(msgContext));
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

    /**
     * Get the content type for the message passed in
     * @param msgContext the message
     * @return content type of the message
     */
    public static String getContentType(MessageContext msgContext) {
        Object contentTypeObject = msgContext.getProperty(Constants.Configuration.CONTENT_TYPE);
        if (contentTypeObject != null) {
            return (String) contentTypeObject;
        } else if (msgContext.isDoingREST()) {
            return HTTPConstants.MEDIA_TYPE_APPLICATION_XML;
        } else {
            return getOMOutputFormat(msgContext).getContentType();
        }
    }

    /**
     * Calculate the REST_URL_POSTFIX from the request URI
     * @param uri  - The Request URI - String
     * @param servicePath  String
     * @return  REST_URL_POSTFIX String
     */
    public static String getRestUrlPostfix(String uri, String servicePath){

        servicePath = "/" + servicePath;
        if (uri.startsWith(servicePath)) {
            // discard upto servicePath
            uri = uri.substring(uri.indexOf(servicePath) +
                    servicePath.length());
            // discard [proxy] service name if any
            int pos = uri.indexOf("/", 1);
            if (pos > 0) {
                uri = uri.substring(pos);
            } else {
                pos = uri.indexOf("?");
                if (pos != -1) {
                    uri = uri.substring(pos);
                } else {
                    uri = "";
                }
            }
        } else {
            // remove any absolute prefix if any
            int pos = uri.indexOf("://");
            //compute index of beginning of Query Parameter
            int indexOfQueryStart = uri.indexOf("?");
            
            //Check if there exist a absolute prefix '://' and it is before query parameters
            //To allow query parameters with URLs. ex: /test?a=http://asddd
            if (pos != -1 && ((indexOfQueryStart == -1 || pos < indexOfQueryStart))) {
                uri = uri.substring(pos + 3);
            }
            pos = uri.indexOf("/");
            if (pos != -1) {
                uri = uri.substring(pos + 1);
            }
        }

        return uri;
    }
}
