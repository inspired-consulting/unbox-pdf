package inspired.pdf.unbox.internal;

import java.io.IOException;

/**
 * Wraps I/O failures from PDF operations while preserving the original cause, and
 * reports failures detected by the library itself.
 */
public class PdfUnboxException extends RuntimeException {

    public PdfUnboxException(IOException e) {
        super(e);
    }

    public PdfUnboxException(String message, IOException cause) {
        super(message, cause);
    }

    /**
     * A failure detected by the library itself, such as text a font cannot encode.
     */
    public PdfUnboxException(String message) {
        super(message);
    }

}
