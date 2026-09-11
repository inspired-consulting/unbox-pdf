package inspired.pdf.unbox;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static inspired.pdf.unbox.Unbox.paragraph;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies that failing finish callbacks release their page streams and preserve the failure.
 */
class DocumentFinisherTest {

    @Test
    void callbackFailureClosesPageStreamBeforePropagating() {
        IllegalStateException failure = new IllegalStateException("finisher failed");
        try (Document document = new Document()) {
            document.render(paragraph("Content"));
            document.add(new DocumentFinisher() {
                @Override
                public void finish(DocumentContext context, PDPageContentStream stream,
                                   int pageNumber, int pageCount) {
                    throw failure;
                }
            });

            assertSame(failure, assertThrows(IllegalStateException.class, document::finish));
            // PDFBox cannot save while a page stream is still open for writing.
            // Saving here checks cleanup before Document.close() can mask a leak.
            assertDoesNotThrow(() -> document.getDocument().save(new ByteArrayOutputStream()));
        }
    }
}
