package org.apache.synapse.commons.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * Class representing some temporary data in the form of a byte stream.
 * <p>
 * Data is stored by writing to the output stream obtained using
 * {@link #getOutputStream()}. It can then be read back using
 * the input stream obtained from {@link #getInputStream()}.
 * The data is first stored into a fixed size buffer. Once this
 * buffer overflows, it is transferred to a temporary file. The buffer
 * is divided into a given number of fixed size chunks that are allocated
 * on demand. Since a temporary file may be created it is mandatory to
 * call {@link #release()} to discard the temporary data.
 *
 * @deprecated this class is deprecated and will be removed from the next release,
 * please use the {@link org.apache.axiom.util.blob.OverflowBlob} from axiom instead
 */
@Deprecated
public class TemporaryData {
    
    private static final Log log = LogFactory.getLog(TemporaryData.class);
    
    class OutputStreamImpl extends OutputStream {
        
        private FileOutputStream fileOutputStream;
        
        public void write(byte[] b, int off, int len) throws IOException {

            if (fileOutputStream != null) {
                fileOutputStream.write(b, off, len);
            } else if (len > (chunks.length-chunkIndex)*chunkSize - chunkOffset) {

                // The buffer will overflow. Switch to a temporary file.
                fileOutputStream = switchToTempFile();
                
                // Write the new data to the temporary file.
                fileOutputStream.write(b, off, len);

            } else {

                // The data will fit into the buffer.
                while (len > 0) {

                    byte[] chunk = getCurrentChunk();

                    // Determine number of bytes that can be copied to the current chunk.
                    int c = Math.min(len, chunkSize-chunkOffset);
                    // Copy data to the chunk.
                    System.arraycopy(b, off, chunk, chunkOffset, c);

                    // Update variables.
                    len -= c;
                    off += c;
                    chunkOffset += c;
                    if (chunkOffset == chunkSize) {
                        chunkIndex++;
                        chunkOffset = 0;
                    }
                }
            }
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public void write(int b) throws IOException {
            write(new byte[] { (byte)b }, 0, 1);
        }

        public void flush() throws IOException {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
            }
        }

        public void close() throws IOException {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }
    
    class InputStreamImpl extends InputStream {

        private int currentChunkIndex;
        private int currentChunkOffset;
        private int markChunkIndex;
        private int markChunkOffset;
        
        public int available() throws IOException {
            return (chunkIndex-currentChunkIndex)*chunkSize + chunkOffset - currentChunkOffset;
        }

        public int read(byte[] b, int off, int len) throws IOException {

            if (len == 0) {
                return 0;
            }

            int read = 0;
            while (len > 0 && !(currentChunkIndex == chunkIndex
                    && currentChunkOffset == chunkOffset)) {

                int c;
                if (currentChunkIndex == chunkIndex) {
                    // The current chunk is the last one => take into account the offset
                    c = Math.min(len, chunkOffset-currentChunkOffset);
                } else {
                    c = Math.min(len, chunkSize-currentChunkOffset);
                }

                // Copy the data.
                System.arraycopy(chunks[currentChunkIndex], currentChunkOffset, b, off, c);

                // Update variables
                len -= c;
                off += c;
                currentChunkOffset += c;
                read += c;
                if (currentChunkOffset == chunkSize) {
                    currentChunkIndex++;
                    currentChunkOffset = 0;
                }
            }

            if (read == 0) {
                // We didn't read anything (and the len argument was not 0) => we reached the end of the buffer.
                return -1;
            } else {
                return read;
            }
        }

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            return read(b) == -1 ? -1 : (int)b[0] & 0xFF;
        }

        public boolean markSupported() {
            return true;
        }

        public void mark(int readlimit) {
            markChunkIndex = currentChunkIndex;
            markChunkOffset = currentChunkOffset;
        }

        public void reset() throws IOException {
            currentChunkIndex = markChunkIndex;
            currentChunkOffset = markChunkOffset;
        }

        public long skip(long n) throws IOException {

            int available = available();
            int c = n < available ? (int)n : available;
            int newOffset = currentChunkOffset + c;
            int chunkDelta = newOffset/chunkSize;
            currentChunkIndex += chunkDelta;
            currentChunkOffset = newOffset - (chunkDelta*chunkSize);
            return c;
        }
        
        public void close() throws IOException {
        }
    }
    
    /**
     * Size of the chunks that will be allocated in the buffer.
     */
    final int chunkSize;
    
    /**
     * The prefix to be used in generating the name of the temporary file.
     */
    final String tempPrefix;
    
    /**
     * The suffix to be used in generating the name of the temporary file.
     */
    final String tempSuffix;
    
    /**
     * Array of <code>byte[]</code> representing the chunks of the buffer.
     * A chunk is only allocated when the first byte is written to it.
     * This attribute is set to <code>null</code> when the buffer overflows and
     * is written out to a temporary file.
     */
    byte[][] chunks;
    
    /**
     * Index of the chunk the next byte will be written to.
     */
    int chunkIndex;
    
    /**
     * Offset into the chunk where the next byte will be written.
     */
    int chunkOffset;
    
    /**
     * The handle of the temporary file. This is only set when the memory buffer
     * overflows and is written out to a temporary file.
     */
    File temporaryFile;
    
    public TemporaryData(int numberOfChunks, int chunkSize, String tempPrefix, String tempSuffix) {
        this.chunkSize = chunkSize;
        this.tempPrefix = tempPrefix;
        this.tempSuffix = tempSuffix;
        chunks = new byte[numberOfChunks][];
    }
    
    /**
     * Get the current chunk to write to, allocating it if necessary.
     * 
     * @return the current chunk to write to (never null)
     */
    byte[] getCurrentChunk() {
        if (chunkOffset == 0) {
            // We will write the first byte to the current chunk. Allocate it.
            byte[] chunk = new byte[chunkSize];
            chunks[chunkIndex] = chunk;
            return chunk;
        } else {
            // The chunk has already been allocated.
            return chunks[chunkIndex];
        }
    }
    
    /**
     * Create a temporary file and write the existing in memory data to it.
     * 
     * @return an open FileOutputStream to the temporary file
     * @throws IOException in case of an error in switching to the temp file
     */
    FileOutputStream switchToTempFile() throws IOException {
        temporaryFile = File.createTempFile(tempPrefix, tempSuffix);
        if (log.isDebugEnabled()) {
            log.debug("Using temporary file " + temporaryFile);
        }
        temporaryFile.deleteOnExit();

        FileOutputStream fileOutputStream = new FileOutputStream(temporaryFile);
        // Write the buffer to the temporary file.
        for (int i=0; i<chunkIndex; i++) {
            fileOutputStream.write(chunks[i]);
        }

        if (chunkOffset > 0) {
            fileOutputStream.write(chunks[chunkIndex], 0, chunkOffset);
        }

        // Release references to the buffer so that it can be garbage collected.
        chunks = null;
        
        return fileOutputStream;
    }
    
    public OutputStream getOutputStream() {
        return new OutputStreamImpl();
    }
    
    /**
     * Fill this object with data read from a given InputStream.
     * <p>
     * A call <code>tmp.readFrom(in)</code> has the same effect as the
     * following code:
     * <pre>
     * OutputStream out = tmp.getOutputStream();
     * IOUtils.copy(in, out);
     * out.close();
     * </pre>
     * However it does so in a more efficient way.
     * 
     * @param in An InputStream to read data from. This method will not
     *           close the stream.
     * @throws IOException in case of an error in reading from <code>InputStream</code>
     */
    public void readFrom(InputStream in) throws IOException {
        while (true) {
            int c = in.read(getCurrentChunk(), chunkOffset, chunkSize-chunkOffset);
            if (c == -1) {
                break;
            }
            chunkOffset += c;
            if (chunkOffset == chunkSize) {
                chunkIndex++;
                chunkOffset = 0;
                if (chunkIndex == chunks.length) {
                    FileOutputStream fileOutputStream = switchToTempFile();
                    IOUtils.copy(in, fileOutputStream);
                    fileOutputStream.close();
                    break;
                }
            }
        }
    }
    
    public InputStream getInputStream() throws IOException {
        if (temporaryFile != null) {
            return new FileInputStream(temporaryFile);
        } else {
            return new InputStreamImpl();
        }
    }
    
    /**
     * Write the data to a given output stream.
     * 
     * @param out The output stream to write the data to. This method will
     *            not close the stream.
     * @throws IOException  in case of an error in writing to the <code>OutputStream</code>
     */
    public void writeTo(OutputStream out) throws IOException {
        if (temporaryFile != null) {
            FileInputStream in = new FileInputStream(temporaryFile);
            try {
                IOUtils.copy(in, out);
            } finally {
                in.close();
            }
        } else {
            for (int i=0; i<chunkIndex; i++) {
                out.write(chunks[i]);
            }
            if (chunkOffset > 0) {
                out.write(chunks[chunkIndex], 0, chunkOffset);
            }
        }
    }
    
    public long getLength() {
        if (temporaryFile != null) {
            return temporaryFile.length();
        } else {
            return chunkIndex*chunkSize + chunkOffset;
        }
    }
    
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public void release() {
        if (temporaryFile != null) {
            if (log.isDebugEnabled()) {
                log.debug("Deleting temporary file " + temporaryFile);
            }
            FileUtils.deleteQuietly(temporaryFile);
            temporaryFile = null;
        }
    }

    @SuppressWarnings({"FinalizeDoesntCallSuperFinalize", "ResultOfMethodCallIgnored"})
    @Override
    protected void finalize() throws Throwable {
        if (temporaryFile != null) {
            log.warn("Cleaning up unreleased temporary file " + temporaryFile);
            FileUtils.deleteQuietly(temporaryFile);
        }
    }
}
