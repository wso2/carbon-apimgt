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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.builder.BuilderUtil;
import org.apache.axis2.builder.SOAPBuilder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.format.DataSourceMessageBuilder;
import org.apache.axis2.format.ManagedDataSource;
import org.apache.axis2.format.ManagedDataSourceFactory;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.transport.base.AbstractPollingTransportListener;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.axis2.transport.base.threads.WorkerPool;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.AutoCloseInputStream;

import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.util.*;

/**
 * The "vfs" transport is a polling based transport - i.e. it gets kicked off at
 * specified periodic durations, and would iterate through a list of directories or files
 * specified according to poll durations. When scanning a directory, it will match
 * its contents against a given regex to find the set of input files. For compressed
 * files, the contents could be matched against a regex to find individual files.
 * Each of these files thus found would be submitted as an Axis2 "message" into the
 * Axis2 engine.
 *
 * The processed files would be deleted or renamed as specified in the configuration
 *
 * Supported VFS example URIs
 * 
 * file:///directory/filename.ext
 * file:////somehost/someshare/afile.txt
 * jar:../lib/classes.jar!/META-INF/manifest.mf
 * zip:http://somehost/downloads/somefile.zip
 * jar:zip:outer.zip!/nested.jar!/somedir
 * jar:zip:outer.zip!/nested.jar!/some%21dir
 * tar:gz:http://anyhost/dir/mytar.tar.gz!/mytar.tar!/path/in/tar/README.txt
 * tgz:file://anyhost/dir/mytar.tgz!/somepath/somefile
 * gz:/my/gz/file.gz
 * http://somehost:8080/downloads/somefile.jar
 * http://myusername@somehost/index.html
 * webdav://somehost:8080/dist
 * ftp://myusername:mypassword@somehost/pub/downloads/somefile.tgz[?passive=true]
 * sftp://myusername:mypassword@somehost/pub/downloads/somefile.tgz
 * smb://somehost/home
 *
 * axis2.xml - transport definition
 *  <transportReceiver name="file" class="org.apache.synapse.transport.vfs.VFSTransportListener">
 *      <parameter name="transport.vfs.Locking">enable|disable</parameter> ?
 *  </transportReceiver>
 *
 * services.xml - service attachment
 *  required parameters
 *  <parameter name="transport.vfs.FileURI">..</parameter>
 *  <parameter name="transport.vfs.ContentType">..</parameter>
 *
 *  optional parameters
 *  <parameter name="transport.vfs.FileNamePattern">..</parameter>
 *  <parameter name="transport.PollInterval">..</parameter>
 * 
 *  <parameter name="transport.vfs.ActionAfterProcess">..</parameter>
 * 	<parameter name="transport.vfs.ActionAfterErrors" >..</parameter>
 *  <parameter name="transport.vfs.ActionAfterFailure">..</parameter>
 *
 *  <parameter name="transport.vfs.ReplyFileURI" >..</parameter>
 *  <parameter name="transport.vfs.ReplyFileName">..</parameter>
 *
 * FTP testing URIs
 * ftp://ftpuser:password@asankha/somefile.csv?passive=true
 * ftp://vfs:apache@vfs.netfirms.com/somepath/somefile.xml?passive=true
 */
public class VFSTransportListener extends AbstractPollingTransportListener<PollTableEntry> 
    implements ManagementSupport {

    public static final String TRANSPORT_NAME = "vfs";

    public static final String DELETE = "DELETE";
    public static final String MOVE = "MOVE";

    /** The VFS file system manager */
    private FileSystemManager fsManager = null;

    private WorkerPool workerPool = null;

    private static final int STATE_STOPPED = 0;

    private static final int STATE_RUNNING = 1;

    private volatile int removeTaskState = STATE_STOPPED;

    /**
     * By default file locking in VFS transport is turned on at a global level
     *
     * NOTE: DO NOT USE THIS FLAG, USE PollTableEntry#isFileLockingEnabled() TO CHECK WHETHR
     * FILE LOCKING IS ENABLED
     */
    private boolean globalFileLockingFlag = true;

    @Override
    protected void doInit() throws AxisFault {
        super.doInit();
        try {
            StandardFileSystemManager fsm = new StandardFileSystemManager();
            fsm.setConfiguration(getClass().getClassLoader().getResource("providers.xml"));
            fsm.init();
            this.workerPool = super.workerPool;
            fsManager = fsm;
            Parameter lockFlagParam = getTransportInDescription().getParameter(VFSConstants.TRANSPORT_FILE_LOCKING);
            if (lockFlagParam != null) {
                String strLockingFlag = lockFlagParam.getValue().toString();
                // by-default enabled, if explicitly specified as "disable" make it disable
                if (VFSConstants.TRANSPORT_FILE_LOCKING_DISABLED.equals(strLockingFlag)) {
                    globalFileLockingFlag = false;
                }
            }
        } catch (FileSystemException e) {
            handleException("Error initializing the file transport : " + e.getMessage(), e);
        }
    }

    @Override
    protected void poll(PollTableEntry entry) {
        scanFileOrDirectory(entry, entry.getFileURI());
    }

    /**
     * Search for files that match the given regex pattern and create a list
     * Then process each of these files and update the status of the scan on
     * the poll table
     * @param entry the poll table entry for the scan
     * @param fileURI the file or directory to be scanned
     */
    private void scanFileOrDirectory(final PollTableEntry entry, String fileURI) {

        FileObject fileObject = null;

        //TODO : Trying to make the correct URL out of the malformed one.
        if(fileURI.contains("vfs:")){
            fileURI=fileURI.substring(fileURI.indexOf("vfs:")+4);
        }

        if (log.isDebugEnabled()) {
            log.debug("Scanning directory or file : " + VFSUtils.maskURLPassword(fileURI));
        }

        boolean wasError = true;
        int retryCount = 0;
        int maxRetryCount = entry.getMaxRetryCount();
        long reconnectionTimeout = entry.getReconnectTimeout();

        while (wasError) {
            try {
                retryCount++;
                fileObject = fsManager.resolveFile(fileURI);

                if (fileObject == null) {
                    log.error("fileObject is null");
                    throw new FileSystemException("fileObject is null");
                }

                wasError = false;

            } catch (FileSystemException e) {
                if (retryCount >= maxRetryCount) {
                    processFailure("Repeatedly failed to resolve the file URI: " +
                            VFSUtils.maskURLPassword(fileURI), e, entry);
                    return;
                } else {
                    log.warn("Failed to resolve the file URI: " +
                            VFSUtils.maskURLPassword(fileURI) + ", in attempt " + retryCount +
                            ", " + e.getMessage() + " Retrying in " + reconnectionTimeout +
                            " milliseconds.");
                }
            }

            if (wasError) {
                try {
                    Thread.sleep(reconnectionTimeout);
                } catch (InterruptedException e2) {
                    log.error("Thread was interrupted while waiting to reconnect.", e2);
                }
            }
        }

        try {
            if (fileObject.exists() && fileObject.isReadable()) {

                entry.setLastPollState(PollTableEntry.NONE);
                FileObject[] children = null;
                try {
                    children = fileObject.getChildren();
                } catch (FileNotFolderException ignored) {
                } catch (FileSystemException ex) {
                    log.error(ex.getMessage(), ex);
                }

                // if this is a file that would translate to a single message
                if (children == null || children.length == 0) {
                    boolean isFailedRecord = false;
                    if (entry.getMoveAfterMoveFailure() != null) {
                        isFailedRecord = isFailedRecord(fileObject, entry);
                    }

                    if (fileObject.getType() == FileType.FILE &&
                            !isFailedRecord) {
                        if (!entry.isFileLockingEnabled() || (entry.isFileLockingEnabled() &&
                                VFSUtils.acquireLock(fsManager, fileObject))) {
                            try {
                                processFile(entry, fileObject);
                                entry.setLastPollState(PollTableEntry.SUCCSESSFUL);
                                metrics.incrementMessagesReceived();

                            } catch (AxisFault e) {
                                logException("Error processing File URI : "
                                        + fileObject.getName(), e);
                                entry.setLastPollState(PollTableEntry.FAILED);
                                metrics.incrementFaultsReceiving();
                            }

                            try {
                                moveOrDeleteAfterProcessing(entry, fileObject);
                            } catch (AxisFault axisFault) {
                                logException("File object '" + fileObject.getURL().toString() + "' " +
                                        "cloud not be moved", axisFault);
                                entry.setLastPollState(PollTableEntry.FAILED);
                                String timeStamp =
                                        VFSUtils.getSystemTime(entry.getFailedRecordTimestampFormat());
                                addFailedRecord(entry, fileObject, timeStamp);
                            }
                            if (entry.isFileLockingEnabled()) {
                                VFSUtils.releaseLock(fsManager, fileObject);
                                if (log.isDebugEnabled()) {
                                    log.debug("Removed the lock file '" + fileObject.toString() +
                                            ".lock' of the file '" + fileObject.toString());
                                }
                            }
                        } else if (log.isDebugEnabled()) {
                            log.debug("Couldn't get the lock for processing the file : "
                                    + fileObject.getName());
                        } else if (isFailedRecord) {
                            if (entry.isFileLockingEnabled()) {
                                VFSUtils.releaseLock(fsManager, fileObject);
                            }
                            // schedule a cleanup task if the file is there
                            if (fsManager.resolveFile(fileObject.getURL().toString()) != null &&
                                    removeTaskState == STATE_STOPPED && entry.getMoveAfterMoveFailure() != null) {
                                workerPool.execute(new FileRemoveTask(entry, fileObject));
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("File '" + fileObject.getURL() + "' has been marked as a failed" +
                                        " record, it will not process");
                            }
                        }
                    }

                } else {
                    int failCount = 0;
                    int successCount = 0;
                    int processCount = 0;
                    Integer iFileProcessingInterval = entry.getFileProcessingInterval();
                    Integer iFileProcessingCount = entry.getFileProcessingCount();
                    
                    if (log.isDebugEnabled()) {
                        log.debug("File name pattern : " + entry.getFileNamePattern());
                    }
                    for (FileObject child : children) {
                        //skipping *.lock file
                        if(child.getName().getBaseName().endsWith(".lock")){
                            continue;
                        }
                        boolean isFailedRecord = false;
                        if (entry.getMoveAfterMoveFailure() != null) {
                            isFailedRecord = isFailedRecord(child, entry);
                        }

                        if(entry.getFileNamePattern()!=null &&
                                child.getName().getBaseName().matches(entry.getFileNamePattern())){
                            //child's file name matches the file name pattern
                            //now we try to get the lock and process
                            if (log.isDebugEnabled()) {
                                log.debug("Matching file : " + child.getName().getBaseName());
                            }
                            
                            if((!entry.isFileLockingEnabled() 
                                    || (entry.isFileLockingEnabled() && VFSUtils.acquireLock(fsManager, child))) 
                                    && !isFailedRecord){
                                //process the file
                                try {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Processing file :" + child);
                                    }
                                    processCount++;
                                    processFile(entry, child);
                                    successCount++;
                                    // tell moveOrDeleteAfterProcessing() file was success
                                    entry.setLastPollState(PollTableEntry.SUCCSESSFUL);
                                    metrics.incrementMessagesReceived();

                                } catch (Exception e) {
                                    logException("Error processing File URI : " + child.getName(), e);
                                    failCount++;
                                    // tell moveOrDeleteAfterProcessing() file failed
                                    entry.setLastPollState(PollTableEntry.FAILED);
                                    metrics.incrementFaultsReceiving();
                                }
                                //skipping un-locking file if failed to do delete/move after process
                                boolean skipUnlock = false;
                                try {
                                    moveOrDeleteAfterProcessing(entry, child);
                                } catch (AxisFault axisFault) {
                                    logException("File object '" + child.getURL().toString() +
                                            "'cloud not be moved, will remain in \"locked\" state", axisFault);
                                    skipUnlock = true;
                                    failCount++;
                                    entry.setLastPollState(PollTableEntry.FAILED);
                                    String timeStamp =
                                            VFSUtils.getSystemTime(entry.getFailedRecordTimestampFormat());
                                    addFailedRecord(entry, child, timeStamp);
                                }
                                // if there is a failure or not we'll try to release the lock
                                if (entry.isFileLockingEnabled() && !skipUnlock) {
                                    VFSUtils.releaseLock(fsManager, child);
                                }
                            } 
                        }else if(entry.getFileNamePattern()!=null && 
                                !child.getName().getBaseName().matches(entry.getFileNamePattern())){
                            //child's file name does not match the file name pattern
                            if (log.isDebugEnabled()) {
                                log.debug("Non-Matching file : " + child.getName().getBaseName());
                            }
                        } else if(isFailedRecord){
                            //it is a failed record
                            if (entry.isFileLockingEnabled()) {
                                VFSUtils.releaseLock(fsManager, child);
                                VFSUtils.releaseLock(fsManager, fileObject);
                            }
                            if (fsManager.resolveFile(child.getURL().toString()) != null &&
                                    removeTaskState == STATE_STOPPED && entry.getMoveAfterMoveFailure() != null) {
                                workerPool.execute(new FileRemoveTask(entry, child));
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("File '" + fileObject.getURL() +
                                        "' has been marked as a failed record, it will not " +
                                        "process");
                            }
                        }
                        
                        if(iFileProcessingInterval != null && iFileProcessingInterval > 0){
                        	try{
                                if (log.isDebugEnabled()) {
                                    log.debug("Put the VFS processor to sleep for : " + iFileProcessingInterval);
                                }                        		
                        		Thread.sleep(iFileProcessingInterval);
                        	}catch(InterruptedException ie){
                        		log.error("Unable to set the interval between file processors." + ie);
                        	}
                        }else if(iFileProcessingCount != null && iFileProcessingCount <= processCount){
                        	break;
                        }
                    }

                    if (failCount == 0 && successCount > 0) {
                        entry.setLastPollState(PollTableEntry.SUCCSESSFUL);
                    } else if (successCount == 0 && failCount > 0) {
                        entry.setLastPollState(PollTableEntry.FAILED);
                    } else {
                        entry.setLastPollState(PollTableEntry.WITH_ERRORS);
                    }
                }

                // processing of this poll table entry is complete
                long now = System.currentTimeMillis();
                entry.setLastPollTime(now);
                entry.setNextPollTime(now + entry.getPollInterval());

            } else if (log.isDebugEnabled()) {
				log.debug("Unable to access or read file or directory : " + VFSUtils.maskURLPassword(fileURI)+ "." +
						                        " Reason: " + (fileObject.exists()? (fileObject.isReadable()? "Unknown reason":"The file can not be read!"):
													                        "The file does not exists!"));
            }
            onPollCompletion(entry);
        } catch (FileSystemException e) {
            processFailure("Error checking for existence and readability : " + VFSUtils.maskURLPassword(fileURI), e, entry);
        }
    }

    /**
     * Take specified action to either move or delete the processed file, depending on the outcome
     * @param entry the PollTableEntry for the file that has been processed
     * @param fileObject the FileObject representing the file to be moved or deleted
     */
    private void moveOrDeleteAfterProcessing(final PollTableEntry entry, FileObject fileObject)
            throws AxisFault {

        String moveToDirectoryURI = null;
        try {
            switch (entry.getLastPollState()) {
                case PollTableEntry.SUCCSESSFUL:
                    if (entry.getActionAfterProcess() == PollTableEntry.MOVE) {
                        moveToDirectoryURI = entry.getMoveAfterProcess();
                    }
                    break;

                case PollTableEntry.FAILED:
                    if (entry.getActionAfterFailure() == PollTableEntry.MOVE) {
                        moveToDirectoryURI = entry.getMoveAfterFailure();
                    }
                    break;
                
                default:
                    return;
            }

            if (moveToDirectoryURI != null) {
                FileObject moveToDirectory = fsManager.resolveFile(moveToDirectoryURI);
                String prefix;
                if(entry.getMoveTimestampFormat() != null) {
                    prefix = entry.getMoveTimestampFormat().format(new Date());
                } else {
                    prefix = "";
                }
                FileObject dest = moveToDirectory.resolveFile(
                        prefix + fileObject.getName().getBaseName());
                if (log.isDebugEnabled()) {
                    log.debug("Moving to file :" + dest.getName().getURI());
                }
                try {
                    fileObject.moveTo(dest);
                } catch (FileSystemException e) {
                    handleException("Error moving file : " + fileObject + " to " +
                            moveToDirectoryURI, e);
                }
            } else {
                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Deleting file :" + fileObject);
                    }
                    fileObject.close();
                    if (!fileObject.delete()) {
                        String msg = "Cannot delete file : " + fileObject;
                        log.error(msg);
                        throw new AxisFault(msg);
                    }
                } catch (FileSystemException e) {
                    log.error("Error deleting file : " + fileObject, e);
                }
            }

        } catch (FileSystemException e) {
            handleException("Error resolving directory to move after processing : "
                    + moveToDirectoryURI, e);
        }
    }

    /**
     * Process a single file through Axis2
     * @param entry the PollTableEntry for the file (or its parent directory or archive)
     * @param file the file that contains the actual message pumped into Axis2
     * @throws AxisFault on error
     */
    private void processFile(PollTableEntry entry, FileObject file) throws AxisFault {

        try {
            FileContent content = file.getContent();
            String fileName = file.getName().getBaseName();
            String filePath = file.getName().getPath();
            String fileURI = file.getName().getURI();

            metrics.incrementBytesReceived(content.getSize());

            Map<String, Object> transportHeaders = new HashMap<String, Object>();
            transportHeaders.put(VFSConstants.FILE_PATH, filePath);
            transportHeaders.put(VFSConstants.FILE_NAME, fileName);
            transportHeaders.put(VFSConstants.FILE_URI, fileURI);

            try {
                transportHeaders.put(VFSConstants.FILE_LENGTH, content.getSize());
                transportHeaders.put(VFSConstants.LAST_MODIFIED, content.getLastModifiedTime());
            } catch (FileSystemException ignore) {}

            MessageContext msgContext = entry.createMessageContext();
            
            String contentType = entry.getContentType();
            if (BaseUtils.isBlank(contentType)) {
                if (file.getName().getExtension().toLowerCase().endsWith(".xml")) {
                    contentType = "text/xml";
                } else if (file.getName().getExtension().toLowerCase().endsWith(".txt")) {
                    contentType = "text/plain";
                }
            } else {
                // Extract the charset encoding from the configured content type and
                // set the CHARACTER_SET_ENCODING property as e.g. SOAPBuilder relies on this.
                String charSetEnc = null;
                try {
                    if (contentType != null) {
                        charSetEnc = new ContentType(contentType).getParameter("charset");
                    }
                } catch (ParseException ex) {
                    // ignore
                }
                msgContext.setProperty(Constants.Configuration.CHARACTER_SET_ENCODING, charSetEnc);
            }

            // if the content type was not found, but the service defined it.. use it
            if (contentType == null) {
                if (entry.getContentType() != null) {
                    contentType = entry.getContentType();
                } else if (VFSUtils.getProperty(
                    content, BaseConstants.CONTENT_TYPE) != null) {
                    contentType =
                        VFSUtils.getProperty(content, BaseConstants.CONTENT_TYPE);
                }
            }

            // does the service specify a default reply file URI ?
            String replyFileURI = entry.getReplyFileURI();
            if (replyFileURI != null) {
                msgContext.setProperty(Constants.OUT_TRANSPORT_INFO,
                        new VFSOutTransportInfo(replyFileURI, entry.isFileLockingEnabled()));
            }

            // Determine the message builder to use
            Builder builder;
            if (contentType == null) {
                log.debug("No content type specified. Using SOAP builder.");
                builder = new SOAPBuilder();
            } else {
                int index = contentType.indexOf(';');
                String type = index > 0 ? contentType.substring(0, index) : contentType;
                builder = BuilderUtil.getBuilderFromSelector(type, msgContext);
                if (builder == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("No message builder found for type '" + type +
                                "'. Falling back to SOAP.");
                    }
                    builder = new SOAPBuilder();
                }
            }

            // set the message payload to the message context
            InputStream in;
            ManagedDataSource dataSource;
            if (builder instanceof DataSourceMessageBuilder && entry.isStreaming()) {
                in = null;
                dataSource = ManagedDataSourceFactory.create(
                        new FileObjectDataSource(file, contentType));
            } else {
                in = new AutoCloseInputStream(content.getInputStream());
                dataSource = null;
            }
            
            try {
                OMElement documentElement;
                if (in != null) {
                    documentElement = builder.processDocument(in, contentType, msgContext);
                } else {
                    documentElement = ((DataSourceMessageBuilder)builder).processDocument(
                            dataSource, contentType, msgContext);
                }
                msgContext.setEnvelope(TransportUtils.createSOAPEnvelope(documentElement));
                
                handleIncomingMessage(
                    msgContext,
                    transportHeaders,
                    null, //* SOAP Action - not applicable *//
                    contentType
                );
            }
            finally {
             if(dataSource != null) {
					dataSource.destroy();
				}
            }

            if (log.isDebugEnabled()) {
                log.debug("Processed file : " + file + " of Content-type : " + contentType);
            }

        } catch (FileSystemException e) {
            handleException("Error reading file content or attributes : " + file, e);
            
        } finally {
            try {
                file.close();
            } catch (FileSystemException warn) {
                 //  log.warn("Cannot close file after processing : " + file.getName().getPath(), warn);
               // ignore the warning, since we handed over the stream close job to AutocloseInputstream..
            }
        }
    }

    @Override
    protected PollTableEntry createEndpoint() {
        return new PollTableEntry(globalFileLockingFlag);
    }

    private synchronized void addFailedRecord(PollTableEntry pollTableEntry,
                                              FileObject failedObject,
                                              String timeString) {
        try {
            String record = failedObject.getName().getBaseName() + VFSConstants.FAILED_RECORD_DELIMITER
                    + timeString;
            String recordFile = pollTableEntry.getFailedRecordFileDestination() +
                    pollTableEntry.getFailedRecordFileName();
            File failedRecordFile = new File(recordFile);
            if (!failedRecordFile.exists()) {
                FileUtils.writeStringToFile(failedRecordFile, record);
                if (log.isDebugEnabled()) {
                    log.debug("Added fail record '" + record + "' into the record file '"
                            + recordFile + "'");
                }
            } else {
                List<String> content = FileUtils.readLines(failedRecordFile);
                if (!content.contains(record)) {
                    content.add(record);
                }
                FileUtils.writeLines(failedRecordFile, content);
            }
        } catch (IOException e) {
            log.fatal("Failure while writing the failed records!", e);
        }
    }

    private boolean isFailedRecord(FileObject fileObject, PollTableEntry entry) {
        String failedFile = entry.getFailedRecordFileDestination() +
                entry.getFailedRecordFileName();
        File file = new File(failedFile);
        if (file.exists()) {
            try {
                List list = FileUtils.readLines(file);
                for (Object aList : list) {
                    String str = (String) aList;
                    StringTokenizer st = new StringTokenizer(str,
                            VFSConstants.FAILED_RECORD_DELIMITER);
                    String fileName = st.nextToken();
                    if (fileName != null &&
                            fileName.equals(fileObject.getName().getBaseName())) {
                        return true;
                    }
                }
            } catch (IOException e) {
                log.fatal("Error while reading the file '" + failedFile + "'", e);
            }
        }
        return false;
    }

    private class FileRemoveTask implements Runnable {
        private FileObject failedFileObject;
        private PollTableEntry pollTableEntry;

        public FileRemoveTask(PollTableEntry pollTableEntry, FileObject fileObject) {
            this.pollTableEntry = pollTableEntry;
            this.failedFileObject = fileObject;
        }

        public void run() {
            if (log.isDebugEnabled()) {
                log.debug("New file remove task is starting..thread id : " +
                        Thread.currentThread().getName());
            }
            // there has been a failure, basically it should be a move operation
            // failure. we'll re-try to move in a loop suspending with the
            // configured amount of time
            // we'll check if the lock is still there and if the lock is there
            // then we assume that the respective file object is also there
            // try to remove the folder waiting on a busy loop, if the remove operation success
            // we just exit from the busy loop and mark end of the file move task.
            boolean isDeletionSucceed = false;
            int nextRetryDuration = pollTableEntry.getNextRetryDuration();
            int count = 0;
            while (!isDeletionSucceed) {
                try {
                    reTryFailedMove(pollTableEntry, failedFileObject);
                    isDeletionSucceed = true;
                    removeTaskState = STATE_STOPPED;
                } catch (AxisFault axisFault) {
                    removeTaskState = STATE_RUNNING;
                    try {
                        log.error("Remove attempt '" + (count++) + "' failed for the file '" +
                                failedFileObject.getURL().toString() + "', next re-try will be " +
                                "after '" + nextRetryDuration + "' milliseconds");
                    } catch (FileSystemException e) {
                        log.error("Error while retrying the file url of the file object '" +
                                failedFileObject + "'");
                    }
                    try {
                        Thread.sleep(nextRetryDuration);
                    } catch (InterruptedException ignore) {
                        // ignore
                    }
                }
            }
        }

        private synchronized void reTryFailedMove(PollTableEntry entry, FileObject fileObject)
                throws AxisFault {
            try {

                String moveToDirectoryURI = entry.getMoveAfterMoveFailure();
                FileObject moveToDirectory = fsManager.resolveFile(moveToDirectoryURI);
                if (!moveToDirectory.exists()) {
                    moveToDirectory.createFolder();
                }
                String prefix;
                if (entry.getMoveTimestampFormat() != null) {
                    prefix = entry.getMoveTimestampFormat().format(new Date());
                } else {
                    prefix = "";
                }
                FileObject dest = moveToDirectory.resolveFile(
                        prefix + fileObject.getName().getBaseName());
                if (log.isDebugEnabled()) {
                    log.debug("The failed file is moving to :" + dest.getName().getURI());
                }
                try {
                    fileObject.moveTo(dest);  // FIXME - when an exception occurs here it causes the in folder to vanish
                } catch (FileSystemException e) {
                    handleException("Error moving the failed file : " + fileObject + " to " +
                            moveToDirectoryURI, e);
                }
            } catch (FileSystemException e) {
                handleException("Cloud not move the failed file object '" + fileObject + "'", e);
            } catch (IOException e) {
                handleException("Cloud not create the folder", e);
            }
        }
    }
}
