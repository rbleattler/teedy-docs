package com.sismics.util.mime;

import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * Utility to check MIME types.
 *
 * @author jtremeaux
 */
public class MimeTypeUtil {
    /**
     * Try to guess the MIME type of a file by its magic number (header).
     * 
     * @param file File to inspect
     * @return MIME type
     * @throws Exception
     */
    public static String guessMimeType(InputStream is) throws Exception {
        byte[] headerBytes = new byte[64];
        PushbackInputStream pb = new PushbackInputStream(is, headerBytes.length);
        int readCount = pb.read(headerBytes);
        pb.unread(headerBytes);
        
        if (readCount <= 0) {
            throw new Exception("Cannot read input file");
        }
        String header = new String(headerBytes, "US-ASCII");
        
        if (header.startsWith("PK")) {
            return MimeType.APPLICATION_ZIP;
        } else if (header.startsWith("GIF87a") || header.startsWith("GIF89a")) {
            return MimeType.IMAGE_GIF;
        } else if (headerBytes[0] == ((byte) 0xff) && headerBytes[1] == ((byte) 0xd8)) {
            return MimeType.IMAGE_JPEG;
        } else if (headerBytes[0] == ((byte) 0x89) && headerBytes[1] == ((byte) 0x50) && headerBytes[2] == ((byte) 0x4e) && headerBytes[3] == ((byte) 0x47) &&
                headerBytes[4] == ((byte) 0x0d) && headerBytes[5] == ((byte) 0x0a) && headerBytes[6] == ((byte) 0x1a) && headerBytes[7] == ((byte) 0x0a)) {
            return MimeType.IMAGE_PNG;
        } else if (headerBytes[0] == ((byte) 0x00) && headerBytes[1] == ((byte) 0x00) && headerBytes[2] == ((byte) 0x01) && headerBytes[3] == ((byte) 0x00)) {
            return MimeType.IMAGE_X_ICON;
        }
        
        return null;
    }
}
