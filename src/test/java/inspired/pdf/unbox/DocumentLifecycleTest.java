package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfEventListener;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static inspired.pdf.unbox.Unbox.paragraph;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies PDF ownership, automatic cleanup, and compatibility with caller-managed closing.
 */
class DocumentLifecycleTest {

    @Test
    void finishedDocumentCanBeSavedBeforeAutomaticClose() throws IOException {
        Document document = new Document();
        try (document) {
            document.render(paragraph("Hello"));
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.finish().save(output);
            assertTrue(output.size() > 0);
            assertFalse(document.getDocument().getDocument().isClosed());
        }
        assertTrue(document.getDocument().getDocument().isClosed());
        assertDoesNotThrow(document::close);
    }

    @Test
    void closeDoesNotCreatePagesOrInvokeListeners() {
        Document document = new Document().add(new PdfEventListener() {
            @Override
            public void onNewPage(Document document) {
                fail("close must not create a page");
            }

            @Override
            public void onFinished(Document document) {
                fail("close must not finish rendering");
            }
        });
        document.close();
        assertTrue(document.getDocument().getDocument().isClosed());
        assertDoesNotThrow(document::close);
        assertThrows(IllegalStateException.class, document::finish);
        assertThrows(IllegalStateException.class, document::addPage);
        assertThrows(IllegalStateException.class, document::getContentStream);
    }

    @Test
    void renderingFailureStillClosesDocument() {
        Document document = new Document();
        IllegalStateException failure = new IllegalStateException("rendering failed");
        document.add(new PdfEventListener() {
            @Override
            public void onNewPage(Document document) {
                document.getContentStream();
                throw failure;
            }

            @Override
            public void onFinished(Document document) {
                fail("cleanup must not finish failed rendering");
            }
        });
        assertSame(failure, assertThrows(IllegalStateException.class, () -> {
            try (document) {
                document.render(paragraph("Hello"));
            }
        }));
        assertTrue(document.getDocument().getDocument().isClosed());
    }

    @Test
    void callerMayStillCloseReturnedPdf() throws IOException {
        try (Document document = new Document()) {
            try (var pdf = document.finish()) {
                assertEquals(1, pdf.getNumberOfPages());
            }
        }
    }
}
