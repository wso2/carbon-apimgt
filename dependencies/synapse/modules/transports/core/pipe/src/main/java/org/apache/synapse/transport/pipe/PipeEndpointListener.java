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
package org.apache.synapse.transport.pipe;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.CountDownLatch;

import org.apache.axis2.transport.base.datagram.DatagramDispatcherCallback;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link Runnable} that reads messages from a given UNIX pipe.
 * <p>
 * The pipe will be opened in read/write mode. There are several reasons to
 * do this:
 * <ul>
 *   <li>Opening a pipe in read only mode blocks until the other end of the pipe is opened for writing
 *       (see <a href="http://linux.die.net/man/7/fifo"><tt>man 7 fifo</tt></a>). Since there is no
 *       way to cleanly stop a thread blocking in the constructor of {@link FileInputStream} we open
 *       the pipe in read/write mode to avoid blocking.</li>
 *   <li>A pipe opened in read only mode will be closed when the other end is closed. By opening the pipe
 *       in read/write mode we avoid this. If we unexpectedly receive an end-of-file, we shut down the
 *       listener. This avoids unexpected behavior if the file system object is not a pipe (there is
 *       no reliable way in Java to determine this).</li>
 *   <li>Read operations on the pipe are blocking. By opening the pipe in read/write mode we have a
 *       simple way to wake up the listener thread to shut it down cleanly. However, since read/write
 *       operations on a file channel can't be invoked concurrently from different threads,
 *       we need to create two separate channels from the same file descriptor.</li>
 * </ul>
 */
public class PipeEndpointListener implements Runnable {
    private static final Log log = LogFactory.getLog(PipeEndpointListener.class);
    
    private final PipeEndpoint endpoint;
    private final DatagramDispatcherCallback callback;
    private final RandomAccessFile pipe;
    private final FileChannel readChannel;
    private final FileChannel writeChannel;
    private final Object guard = new Object();
    private boolean running;
    private final CountDownLatch done = new CountDownLatch(1);
    
    public PipeEndpointListener(PipeEndpoint endpoint, DatagramDispatcherCallback callback) throws IOException {
        this.endpoint = endpoint;
        this.callback = callback;
        pipe = new RandomAccessFile(endpoint.getPipe(), "rw");
        FileDescriptor fd = pipe.getFD();
        readChannel = new FileInputStream(fd).getChannel();
        writeChannel = new FileOutputStream(fd).getChannel();
        if (log.isDebugEnabled()) {
            log.debug("Pipe " + endpoint.getPipe().getAbsolutePath() + " opened");
        }
    }

    public void run() {
        running = true;
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        try {
            while (true) {
                ProtocolDecoder decoder;
                decoder = endpoint.getProtocol().createProtocolDecoder();
                while (true) {
                    while (decoder.inputRequired()) {
                        int c;
                        try {
                            c = readChannel.read(readBuffer);
                        } catch (IOException ex) {
                            log.error("Error while reading from pipe " + endpoint.getPipe().getAbsolutePath() + "; shutting down listener", ex);
                            return;
                        }
                        if (c == -1) {
                            log.error("Pipe " + endpoint.getPipe().getAbsolutePath() + " was unexpectedly closed; shutting down listener");
                            return;
                        }
                        synchronized (guard) {
                            if (!running) {
                                return;
                            }
                        }
                        decoder.decode(readBuffer.array(), 0, readBuffer.position());
                        readBuffer.rewind();
                    }
                    byte[] message = decoder.getNext();
                    callback.receive(endpoint, message, message.length, null);
                }
            }
        }
        finally {
            try {
                pipe.close();
                if (log.isDebugEnabled()) {
                    log.debug("Pipe " + endpoint.getPipe().getAbsolutePath() + " closed");
                }
            } catch (IOException ex) {
                log.warn("Error while closing pipe " + endpoint.getPipe().getAbsolutePath(), ex);
            }
            done.countDown();
        }
    }

    public void stop() throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Stopping listener for pipe " + endpoint.getPipe().getAbsolutePath() + " ...");
        }
        synchronized (guard) {
            running = false;
            writeChannel.write(ByteBuffer.allocate(1));
        }
        try {
            done.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        if (log.isDebugEnabled()) {
            log.debug("Listener for pipe " + endpoint.getPipe().getAbsolutePath() + " stopped");
        }
    }
}
