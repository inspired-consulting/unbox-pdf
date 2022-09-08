package inspired.pdf.unbox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;


import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.*;

class LinearPDFWriterTest {
    @TempDir
    Path folder;

    @Test
    void createEmptyPdf() {
        var writer = new LinearPDFWriter();
        var document = writer.finish();
        assertDocumentMatchesReference(document, "empty.pdf");
    }

    void assertDocumentMatchesReference(PDDocument document, String fileName) {
        try {
            var documentFilePath = folder.resolve(fileName);

            var resourceDirectory = Paths.get("src", "test", "resources");
            var referenceFilePath = resourceDirectory.resolve(fileName);

            document.setDocumentId(1L);
            document.save(documentFilePath.toString());
            document.close();

//            Files.copy(documentFilePath, referenceFilePath, REPLACE_EXISTING);
            long mismatchPosition = Files.mismatch(documentFilePath, referenceFilePath);
            assertEquals(-1L, mismatchPosition, "PDF document doesn't match reference.");
        } catch (IOException e) {
            fail("Error writing pdf", e);
        }
    }
}