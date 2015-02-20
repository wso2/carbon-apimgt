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

package org.apache.synapse.transport.vfs;

import org.apache.axis2.transport.OutTransportInfo;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.provider.UriParser;

import java.util.Map;

/**
 * The VFS OutTransportInfo is a holder of information to send an outgoing message
 * (e.g. a Response) to a VFS destination. Thus at a minimum a reference to a
 * File URI (i.e. directory or a file) are held
 */

public class VFSOutTransportInfo implements OutTransportInfo {

    private static final Log log = LogFactory.getLog(VFSOutTransportInfo.class);

    private String outFileURI = null;
    private String outFileName = null;
    private String contentType = null;
    private int maxRetryCount = 3;
    private long reconnectTimeout = 30000;
    private boolean append;
    private boolean fileLocking;

    private static final String[] uriParamsToDelete = {VFSConstants.APPEND+"=true", VFSConstants.APPEND+"=false"};

    /**
     * Constructs the VFSOutTransportInfo containing the information about the file to which the
     * response has to be submitted to.
     * 
     * @param outFileURI URI of the file to which the message is delivered
     */
    VFSOutTransportInfo(String outFileURI, boolean fileLocking) {

     	if (outFileURI.startsWith(VFSConstants.VFS_PREFIX)) {
            String vfsURI = outFileURI.substring(VFSConstants.VFS_PREFIX.length());
            String queryParams = UriParser.extractQueryString(new StringBuilder(vfsURI));

            //Lets get rid of unwanted query params and clean the URI
            if(null != queryParams && !"".equals(queryParams) && vfsURI.contains(VFSConstants.APPEND)) {
               this.outFileURI = cleanURI(vfsURI, queryParams, outFileURI);
            } else {
                this.outFileURI = vfsURI;
            }
        } else {
            this.outFileURI = outFileURI;
        }

        Map<String,String> properties = BaseUtils.getEPRProperties(outFileURI);
        if (properties.containsKey(VFSConstants.MAX_RETRY_COUNT)) {
            String strMaxRetryCount = properties.get(VFSConstants.MAX_RETRY_COUNT);
            maxRetryCount = Integer.parseInt(strMaxRetryCount);
        } else {
            maxRetryCount = VFSConstants.DEFAULT_MAX_RETRY_COUNT;
        }

        if (properties.containsKey(VFSConstants.RECONNECT_TIMEOUT)) {
            String strReconnectTimeout = properties.get(VFSConstants.RECONNECT_TIMEOUT);
            reconnectTimeout = Long.parseLong(strReconnectTimeout) * 1000;
        } else {
            reconnectTimeout = VFSConstants.DEFAULT_RECONNECT_TIMEOUT;
        }

        if (properties.containsKey(VFSConstants.TRANSPORT_FILE_LOCKING)) {
            String strFileLocking = properties.get(VFSConstants.TRANSPORT_FILE_LOCKING);
            if (VFSConstants.TRANSPORT_FILE_LOCKING_ENABLED.equals(strFileLocking)) {
                fileLocking = true;
            } else if (VFSConstants.TRANSPORT_FILE_LOCKING_DISABLED.equals(strFileLocking)) {
                fileLocking = false;
            }
        } else {
            this.fileLocking = fileLocking;
        }

        if (properties.containsKey(VFSConstants.APPEND)) {
            String strAppend = properties.get(VFSConstants.APPEND);
            append = Boolean.parseBoolean(strAppend);
        }

        if (log.isDebugEnabled()) {
            log.debug("Using the fileURI        : " + this.outFileURI);
            log.debug("Using the maxRetryCount  : " + maxRetryCount);
            log.debug("Using the reconnectionTimeout : " + reconnectTimeout);
            log.debug("Using the append         : " + append);
            log.debug("File locking             : " + (this.fileLocking ? "ON" : "OFF"));
        }
    }

    private String cleanURI(String vfsURI, String queryParams, String originalFileURI) {
        // Using Apache Commons StringUtils and Java StringBuilder for improved performance.
        vfsURI = StringUtils.replace(vfsURI, "?" + queryParams, "");

        for(String deleteParam: uriParamsToDelete) {
            queryParams = StringUtils.replace(queryParams, deleteParam, "");
        }
        queryParams = StringUtils.replace(queryParams, "&&", "&");

        // We can sometimes be left with && in the URI
        if(!"".equals(queryParams) && queryParams.toCharArray()[0] == "&".charAt(0)) {
            queryParams = queryParams.substring(1);
        } else if("".equals(queryParams)) {
            return vfsURI;
        }

        String[] queryParamsArray = queryParams.split("&");
        StringBuilder newQueryParams = new StringBuilder("");
        if(queryParamsArray.length > 0) {
            for(String param : queryParamsArray) {
                newQueryParams.append(param);
                newQueryParams.append("&");
            }
            newQueryParams = newQueryParams.deleteCharAt(newQueryParams.length()-1);
            if(!"".equals(newQueryParams)) {
                return vfsURI + "?" + newQueryParams;
            } else {
                return vfsURI;
            }
        } else {
            return originalFileURI.substring(VFSConstants.VFS_PREFIX.length());
        }
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getOutFileURI() {
        return outFileURI;
    }

    public String getOutFileName() {
        return outFileName;
    }

    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public void setMaxRetryCount(int maxRetryCount) {
        this.maxRetryCount = maxRetryCount;
    }

    public long getReconnectTimeout() {
        return reconnectTimeout;
    }

    public void setReconnectTimeout(long reconnectTimeout) {
        this.reconnectTimeout = reconnectTimeout;
    }
    
    public boolean isAppend() {
        return append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public String getContentType() {
        return contentType;
    }

    public boolean isFileLockingEnabled() {
        return fileLocking;
    }
}