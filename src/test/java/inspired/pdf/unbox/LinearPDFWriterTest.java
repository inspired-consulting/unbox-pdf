package inspired.pdf.unbox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.FixedColumnsTable;
import inspired.pdf.unbox.elements.Table;
import inspired.pdf.unbox.elements.TableRow;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.Test;


import static inspired.pdf.unbox.Unbox.background;
import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.*;
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

    @Test
    void createTable() {
        var writer = new LinearPDFWriter();
        var tableModel = new TableModel()
                .add("Article", 2f)
                .add("Size")
                .add("Price", Align.RIGHT);
        Table table = new FixedColumnsTable(tableModel)
                .with(Margin.of(10))
                .with(border(1, GRAY_500));
        table.addHeader(TableRow.header(tableModel, helvetica_bold(8)).with(background(GRAY_100)));
        table.addRow()
                .addCell("SmartTV 200+")
                .addCell("55", Align.LEFT, helvetica_bold(8, RED_ORANGE))
                .addCell("200.12 EUR");
        table.addRow().withCells("SmartPhone", "5,5", "320.00 EUR");
        writer.render(table);

        var document = writer.finish();
        assertDocumentMatchesReference(document, "table.pdf");
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