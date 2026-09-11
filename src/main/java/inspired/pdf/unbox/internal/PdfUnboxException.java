package inspired.pdf.unbox.internal;

import java.io.IOException;

/**
 * Wraps I/O failures from PDF operations while preserving the original cause.
 */
public class PdfUnboxException extends RuntimeException {

    public PdfUnboxException(IOException e) {
        super(e);
    }

    public PdfUnboxException(String message, IOException cause) {
        super(message, cause);
    }

}
