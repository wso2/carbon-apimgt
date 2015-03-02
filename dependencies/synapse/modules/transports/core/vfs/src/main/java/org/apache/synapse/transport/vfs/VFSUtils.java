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

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class VFSUtils extends BaseUtils {

    private static final Log log = LogFactory.getLog(VFSUtils.class);

    /**
     * URL pattern
     */
    private static final Pattern URL_PATTERN = Pattern.compile("[a-z]+://.*");

    /**
     * Password pattern
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(":(?:[^/]+)@");

    /**
     * Get a String property from FileContent message
     *
     * @param message the File message
     * @param property property name
     * @return property value
     */
    public static String getProperty(FileContent message, String property) {
        try {
            Object o = message.getAttributes().get(property);
            if (o instanceof String) {
                return (String) o;
            }
        } catch (FileSystemException ignored) {}
        return null;
    }

    public static String getFileName(MessageContext msgCtx, VFSOutTransportInfo vfsOutInfo) {
        String fileName = null;

        // first preference to a custom filename set on the current message context
        Map transportHeaders = (Map) msgCtx.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (transportHeaders != null) {
            fileName = (String) transportHeaders.get(VFSConstants.REPLY_FILE_NAME);
        }

        // if not, does the service (in its service.xml) specify one?
        if (fileName == null) {
            Parameter param = msgCtx.getAxisService().getParameter(VFSConstants.REPLY_FILE_NAME);
            if (param != null) {
                fileName = (String) param.getValue();
            }
        }

        // next check if the OutTransportInfo specifies one
        if (fileName == null) {
            fileName = vfsOutInfo.getOutFileName();
        }

        // if none works.. use default
        if (fileName == null) {
            fileName = VFSConstants.DEFAULT_RESPONSE_FILE;
        }
        return fileName;
    }

    /**
     * Acquires a file item lock before processing the item, guaranteing that the file is not
     * processed while it is being uploaded and/or the item is not processed by two listeners
     *
     * @param fsManager used to resolve the processing file
     * @param fo representing the processing file item
     * @return boolean true if the lock has been acquired or false if not
     */
    public synchronized static boolean acquireLock(FileSystemManager fsManager, FileObject fo) {
        
        // generate a random lock value to ensure that there are no two parties
        // processing the same file
        Random random = new Random();
        byte[] lockValue = String.valueOf(random.nextLong()).getBytes();
        
        try {
            // check whether there is an existing lock for this item, if so it is assumed
            // to be processed by an another listener (downloading) or a sender (uploading)
            // lock file is derived by attaching the ".lock" second extension to the file name
	    String fullPath = fo.getName().getURI();	
            int pos = fullPath.indexOf("?");
            if (pos != -1) {
                fullPath = fullPath.substring(0, pos);
            }
            FileObject lockObject = fsManager.resolveFile(fullPath + ".lock");
            if (lockObject.exists()) {
                log.debug("There seems to be an external lock, aborting the processing of the file "
                        + fo.getName() + ". This could possibly be due to some other party already "
                        + "processing this file or the file is still being uploaded");
            } else {

                // write a lock file before starting of the processing, to ensure that the
                // item is not processed by any other parties
                lockObject.createFile();
                OutputStream stream = lockObject.getContent().getOutputStream();
                try {
                    stream.write(lockValue);
                    stream.flush();
                    stream.close();
                } catch (IOException e) {
                    lockObject.delete();
                    log.error("Couldn't create the lock file before processing the file "
                            + fullPath, e);
                    return false;
                } finally {
                    lockObject.close();
                }

                // check whether the lock is in place and is it me who holds the lock. This is
                // required because it is possible to write the lock file simultaneously by
                // two processing parties. It checks whether the lock file content is the same
                // as the written random lock value.
                // NOTE: this may not be optimal but is sub optimal
                FileObject verifyingLockObject = fsManager.resolveFile(
                        fullPath + ".lock");
                if (verifyingLockObject.exists() && verifyLock(lockValue, verifyingLockObject)) {
                    return true;
                }
            }
        } catch (FileSystemException fse) {
            log.error("Cannot get the lock for the file : " + maskURLPassword(fo.getName().getURI())
                    + " before processing");
        }
        return false;
    }

    /**
     * Release a file item lock acquired either by the VFS listener or a sender
     *
     * @param fsManager which is used to resolve the processed file
     * @param fo representing the processed file
     */
    public static void releaseLock(FileSystemManager fsManager, FileObject fo) {
        try {
	    String fullPath = fo.getName().getURI();	
            int pos = fullPath.indexOf("?");
            if (pos > -1) {
                fullPath = fullPath.substring(0, pos);
            }
            FileObject lockObject = fsManager.resolveFile(fullPath + ".lock");
            if (lockObject.exists()) {
                lockObject.delete();
            }
        } catch (FileSystemException e) {
            log.error("Couldn't release the lock for the file : "
                    + fo.getName() + " after processing");
        }
    }

    /**
     * Mask the password of the connection url with ***
     * @param url the actual url
     * @return the masked url
     */
    public static String maskURLPassword(String url) {
        final Matcher urlMatcher = URL_PATTERN.matcher(url);
        String maskUrl;
        if (urlMatcher.find()) {
            final Matcher pwdMatcher = PASSWORD_PATTERN.matcher(url);
            maskUrl = pwdMatcher.replaceFirst("\":***@\"");
            return maskUrl;
        }
        return url;
    }

    public static String getSystemTime(String dateFormat) {
        return new SimpleDateFormat(dateFormat).format(new Date());
    }


    private static boolean verifyLock(byte[] lockValue, FileObject lockObject) {
        try {
            InputStream is = lockObject.getContent().getInputStream();
            byte[] val = new byte[lockValue.length];
            // noinspection ResultOfMethodCallIgnored
            is.read(val);
            if (Arrays.equals(lockValue, val) && is.read() == -1) {
                return true;
            } else {
                log.debug("The lock has been acquired by an another party");
            }
        } catch (FileSystemException e) {
            log.error("Couldn't verify the lock", e);
            return false;
        } catch (IOException e) {
            log.error("Couldn't verify the lock", e);
            return false;
        }
        return false;
    }
}
