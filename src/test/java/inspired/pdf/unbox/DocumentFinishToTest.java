package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfEventListener;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static inspired.pdf.unbox.Unbox.paragraph;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the finish-and-save conveniences: they complete rendering, write the PDF,
 * and close the document, including when writing fails.
 */
class DocumentFinishToTest {

    @Test
    void finishToBytesReturnsPdfAndClosesDocument() throws IOException {
        Document document = new Document();
        document.render(paragraph("Hello bytes"));

        byte[] pdf = document.finishToBytes();

        assertEquals("Hello bytes", extractText(pdf));
        assertClosed(document);
    }

    @Test
    void finishToStreamWritesPdfAndClosesDocument() throws IOException {
        Document document = new Document();
        document.render(paragraph("Hello stream"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        document.finishTo(output);

        assertEquals("Hello stream", extractText(output.toByteArray()));
        assertClosed(document);
    }

    @Test
    void finishToPathWritesFileAndClosesDocument() throws IOException {
        Path file = Path.of("target", "finish-to-path.pdf");
        Files.createDirectories(file.getParent());
        Document document = new Document();
        document.render(paragraph("Hello file"));

        document.finishTo(file);

        assertEquals("Hello file", extractText(Files.readAllBytes(file)));
        assertClosed(document);
    }

    @Test
    void finishToInvokesFinishListenersOnce() {
        AtomicInteger finished = new AtomicInteger();
        PdfEventListener listener = new PdfEventListener() {
            @Override
            public void onNewPage(Document document) {
            }

            @Override
            public void onFinished(Document document) {
                finished.incrementAndGet();
            }
        };
        // finishToBytes() closes the document itself; the block only guards against a failure before that.
        try (Document document = new Document().add(listener)) {
            document.finishToBytes();
        }

        assertEquals(1, finished.get());
    }

    @Test
    void failedWriteStillClosesDocument() {
        Document document = new Document();
        OutputStream failing = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("disk full");
            }
        };

        assertThrows(PdfUnboxException.class, () -> document.finishTo(failing));
        assertClosed(document);
    }

    // -- helpers

    private static void assertClosed(Document document) {
        assertTrue(document.getDocument().getDocument().isClosed(), "PDF should be closed");
        assertThrows(IllegalStateException.class, () -> document.render(paragraph("late")));
    }

    private static String extractText(byte[] pdf) throws IOException {
        try (PDDocument loaded = PDDocument.load(pdf)) {
            return new PDFTextStripper().getText(loaded).strip();
        }
    }
}
