package inspired.pdf.unbox.internal;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies that wrapped I/O failures retain their causes and diagnostic messages.
 */
class PdfUnboxExceptionTest {

    @Test
    void preservesCauseAndItsDiagnosticMessage() {
        IOException cause = new IOException("disk full");

        PdfUnboxException exception = new PdfUnboxException(cause);

        assertSame(cause, exception.getCause());
        assertEquals(cause.toString(), exception.getMessage());
    }

    @Test
    void preservesExplicitMessageAndCause() {
        IOException cause = new IOException("disk full");

        PdfUnboxException exception = new PdfUnboxException("Failed to save PDF", cause);

        assertEquals("Failed to save PDF", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
