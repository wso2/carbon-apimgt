/*
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

package org.apache.synapse.transport.passthru;

import org.apache.http.nio.IOControl;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.MalformedChunkCodingException;
import org.apache.synapse.transport.passthru.config.BaseConfiguration;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is a buffer shared by both producers and consumers.
 */
public class Pipe {

    /** IOControl of the reader */
    private IOControl producerIoControl;

    /** IOControl of the consumer */
    private IOControl consumerIoControl;

    /** Fixed size buffer to read and write data */
    private ByteBuffer buffer;

    private ByteBuffer outputBuffer;

    private boolean producerCompleted = false;

    public boolean isProducerCompleted() {
		return producerCompleted;
	}

	/** Lock to synchronize the producers and consumers */
    private Lock lock = new ReentrantLock();

    private Condition readCondition = lock.newCondition();
    private Condition writeCondition = lock.newCondition();

    /** Name to identify the buffer */
    private String name = "Buffer";

    private boolean consumerError = false;

    private boolean producerError = false;

    private BaseConfiguration baseConfig;

    private boolean serializationComplete = false;
    
    private boolean rawSerializationComplete = false;

  	private boolean hasHttpProducer = true;

    private AtomicBoolean inBufferInputMode = new AtomicBoolean(true);
    private AtomicBoolean outBufferInputMode;

    private ByteBufferInputStream inputStream;
    private ByteBufferOutputStream outputStream;

    public Pipe(IOControl producerIoControl, ByteBuffer buffer,
                String name, BaseConfiguration baseConfig) {
        this.producerIoControl = producerIoControl;
        this.buffer = buffer;
        this.name += "_" + name;
        this.baseConfig = baseConfig;
    }

    public Pipe(ByteBuffer buffer, String name, BaseConfiguration baseConfig) {
        this.buffer = buffer;
        this.name += "_" + name;
        this.baseConfig = baseConfig;
        this.hasHttpProducer = false;
    }

    /**
     * Set the consumers IOControl
     * @param consumerIoControl IOControl of the consumer
     */
    public void attachConsumer(IOControl consumerIoControl) {
        this.consumerIoControl = consumerIoControl;
    }

    /**
     * Consume the data from the buffer. Before calling this method attachConsumer
     * method must be called with a valid IOControl.
     *
     * @param encoder encoder used to write the data means there will not be any data
     * written in to this buffer
     * @return number of bytes written (consumed)
     * @throws IOException if an error occurred while consuming data
     */
    public int consume(final ContentEncoder encoder) throws IOException {
        if (consumerIoControl == null) {
            throw new IllegalStateException("Consumer cannot be null when calling consume");
        }

        if (hasHttpProducer && producerIoControl == null) {
            throw new IllegalStateException("Producer cannot be null when calling consume");
        }

        lock.lock();
        ByteBuffer consumerBuffer;
        AtomicBoolean inputMode;
        if (outputBuffer != null) {
            consumerBuffer = outputBuffer;
            inputMode = outBufferInputMode;
        } else {
            consumerBuffer = buffer;
            inputMode = inBufferInputMode;
        }
        try {
            // if producer at error we have to stop the encoding and return immediately
            if (producerError) {
                encoder.complete();
                return -1;
            }

            setOutputMode(consumerBuffer, inputMode);
            int bytesWritten = encoder.write(consumerBuffer);
            setInputMode(consumerBuffer, inputMode);

            if (consumerBuffer.position() == 0) {
                if (outputBuffer == null) {
                    if (producerCompleted) {
                        encoder.complete();
                    } else {
                        // buffer is empty. Wait until the producer fills up
                        // the buffer
                        consumerIoControl.suspendOutput();
                    }
                } else if (serializationComplete || rawSerializationComplete) {
                    encoder.complete();
                }
            }

            if (bytesWritten > 0) {
                if (!encoder.isCompleted() && !producerCompleted && hasHttpProducer) {
                    producerIoControl.requestInput();
                }
                writeCondition.signalAll();
            }

            return bytesWritten;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Produce data in to the buffer.
     *
     * @param decoder decoder to read bytes from the underlying stream
     * @return bytes read (consumed)
     * @throws IOException if an error occurs while reading data
     */
    public int produce(final ContentDecoder decoder) throws IOException {
        if (producerIoControl == null) {
            throw new IllegalStateException("Producer cannot be null when calling produce");
        }

        lock.lock();
        try {
            setInputMode(buffer, inBufferInputMode);
            int bytesRead=0;
            try{
                bytesRead = decoder.read(buffer);
            } catch(MalformedChunkCodingException ignore) {
            	// we assume that this is a truncated chunk, hence simply ignore the exception
            	// https://issues.apache.org/jira/browse/HTTPCORE-195
            	// we should add the EoF character
            	buffer.putInt(-1);
            	// now the buffer's position should give us the bytes read.
            	bytesRead = buffer.position();
            	
            }

            // if consumer is at error we have to let the producer complete
            if (consumerError) {
                buffer.clear();
            }

            if (!buffer.hasRemaining()) {
                // Input buffer is full. Suspend client input
                // until the origin handler frees up some space in the buffer
                producerIoControl.suspendInput();
            }

            // If there is some content in the input buffer make sure consumer output is active
            if (buffer.position() > 0 || decoder.isCompleted()) {
                if (consumerIoControl != null) {
                    consumerIoControl.requestOutput();
                }
                readCondition.signalAll();
            }

            if (decoder.isCompleted()) {
                producerCompleted = true;
            }
           return bytesRead;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public void consumerError() {
        lock.lock();
        try {
            this.consumerError = true;
            writeCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void producerError() {
        lock.lock();
        try {
            this.producerError = true;
            readCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Creates an InputStream object on the underlying ByteBuffer. The returned
     * InputStream can be used to read bytes from the underlying buffer which
     * is being filled by the producer.
     *
     * @return An InputStream object
     */
    public synchronized InputStream getInputStream() {
        if (inputStream == null) {
            inputStream = new ByteBufferInputStream();
        }
        return inputStream;
    }

    /**
     * Creates a separate ByteBuffer for the output data and returns an OutputStream
     * on top of it.
     *
     * @return An OutputStream object
     */
    public synchronized OutputStream getOutputStream() {
        if (outputStream == null) {
            outputBuffer = baseConfig.getBufferFactory().getBuffer();
            outBufferInputMode = new AtomicBoolean(true);
            outputStream = new ByteBufferOutputStream();
        }
        return outputStream;
    }
    
    
    /**
     * Creates a separate ByteBuffer for the output data and returns an OutputStream
     * on top of it.
     *
     * @return An OutputStream object
     */
    public synchronized OutputStream resetOutputStream() {
    	outputBuffer = baseConfig.getBufferFactory().getBuffer();
        outBufferInputMode = new AtomicBoolean(true);
        outputStream = new ByteBufferOutputStream();
        return outputStream;
    }

    public synchronized void setSerializationComplete(boolean serializationComplete) {
        if (!this.serializationComplete) {
            this.serializationComplete = serializationComplete;
            if (consumerIoControl != null && hasData(outputBuffer, outBufferInputMode)) {
                consumerIoControl.requestOutput();
            }
        }
    }
    
    public synchronized void setSerializationCompleteWithoutData(boolean serializationComplete) {
        if (!this.serializationComplete) {
            this.serializationComplete = serializationComplete;
            consumerIoControl.requestOutput();
        }
    }
    
    public void setRawSerializationComplete(boolean rawSerializationComplete) {
    	this.rawSerializationComplete = rawSerializationComplete;
    }
    
    public void forceSetSerializationRest(){
    	if(this.serializationComplete){
    		this.serializationComplete = false;
    	}
    }

    
    

    public boolean isSerializationComplete() {
		return serializationComplete;
	}

	public ByteBuffer getBuffer() {
        return buffer;
    }

    public boolean hasHttpProducer() {
        return hasHttpProducer;
    }

    private void setInputMode(ByteBuffer buffer, AtomicBoolean inputMode) {
        if (inputMode.compareAndSet(false, true)) {
            if (buffer.hasRemaining()) {
                buffer.compact();
            } else {
                buffer.clear();
            }
        }
    }

    private void setOutputMode(ByteBuffer buffer, AtomicBoolean inputMode) {
        if (inputMode.compareAndSet(true, false)) {
            buffer.flip();
        }
    }

    private boolean hasData(ByteBuffer buffer, AtomicBoolean inputMode) {
        lock.lock();
        try {
            setOutputMode(buffer, inputMode);
            return buffer.hasRemaining();
        } finally {
            lock.unlock();
        }
    }

    private class ByteBufferInputStream extends InputStream {

        @Override
        public int read() throws IOException {
            lock.lock();
            try {
                if (!hasData(buffer, inBufferInputMode)) {
                    waitForData();
                }
                if (isEndOfStream()) {
                    return -1;
                }
                return buffer.get() & 0xff;
            } finally {
                lock.unlock();
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                return 0;
            }

            lock.lock();
            try {
                if (!hasData(buffer, inBufferInputMode)) {
                    waitForData();
                }
                if (isEndOfStream()) {
                    return -1;
                }
                setOutputMode(buffer, inBufferInputMode);
                int chunk = len;
                if (chunk > buffer.remaining()) {
                    chunk = buffer.remaining();
                }
                buffer.get(b, off, chunk);
                return chunk;
            } finally {
                lock.unlock();
            }
        }

        private void waitForData() throws IOException {
            lock.lock();
            try {
                try {
                    while (!hasData(buffer, inBufferInputMode) && !producerCompleted) {
                        producerIoControl.requestInput();
                        readCondition.await();
                    }
                } catch (InterruptedException e) {
                    throw new IOException("Interrupted while waiting for data");
                }
            } finally {
                lock.unlock();
            }
        }

        private boolean isEndOfStream() {
            return !hasData(buffer, inBufferInputMode) && producerCompleted;
        }
    }

    private class ByteBufferOutputStream extends OutputStream {

        @Override
        public void write(int b) throws IOException {
            lock.lock();
            try {
                setInputMode(outputBuffer, outBufferInputMode);
                if (!outputBuffer.hasRemaining()) {
                    flushContent();
                    setInputMode(outputBuffer, outBufferInputMode);
                }
                outputBuffer.put((byte) b);
            } finally {
                lock.unlock();
            }
        }

        public void write(final byte[] b, int off, int len) throws IOException {
            if (b == null) {
                return;
            }
            lock.lock();
            try {
                setInputMode(outputBuffer, outBufferInputMode);
                int remaining = len;
                while (remaining > 0) {
                    if (!outputBuffer.hasRemaining()) {
                        flushContent();
                        setInputMode(outputBuffer, outBufferInputMode);
                    }
                    int chunk = Math.min(remaining, outputBuffer.remaining());
                    outputBuffer.put(b, off, chunk);
                    remaining -= chunk;
                    off += chunk;
                }
            } finally {
                lock.unlock();
            }
        }

        private void flushContent() throws IOException {
            lock.lock();
           
            if(rawSerializationComplete){
            	return;
            }
            
            try {
                try {
					while (hasData(outputBuffer, outBufferInputMode)) {
						if (consumerIoControl != null && writeCondition != null) {
							consumerIoControl.requestOutput();
							writeCondition.await();
						}
					}
                     	
                } catch (InterruptedException ex) {
                    throw new IOException("Interrupted while flushing the content buffer");
                }
            } finally {
                lock.unlock();
            }
        }
    }

}

