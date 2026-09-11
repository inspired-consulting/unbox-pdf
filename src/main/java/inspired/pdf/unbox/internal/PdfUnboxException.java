package inspired.pdf.unbox.internal;

import java.io.IOException;

/**
 * Runtime exception for PDF unbox operations.
 */
public class PdfUnboxException extends RuntimeException {

    public PdfUnboxException(IOException e) {
        super(e);
    }

    public PdfUnboxException(String message, IOException cause) {
        super(message, cause);
    }

}
