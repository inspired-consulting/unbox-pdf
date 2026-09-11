package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.base.TableModel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that value rows added with {@code addRow(Object...)} render on both
 * table types, including surplus values and fewer values than columns.
 */
class TableValueRowsTest {

    @Test
    void flexTableRendersValueRows() throws IOException {
        FlexTable table = new FlexTable();
        table.addRow("alpha", "beta", "gamma");
        table.addRow("delta", 42);

        String text = render(table);
        assertTrue(text.contains("alpha"), text);
        assertTrue(text.contains("gamma"), text);
        assertTrue(text.contains("42"), text);
    }

    @Test
    void fixedColumnsTableRendersSurplusValues() throws IOException {
        TableModel model = TableModel.of(1f, 1f);
        FixedColumnsTable table = new FixedColumnsTable(model);
        table.addRow("alpha", "beta", "gamma");

        String text = render(table);
        assertTrue(text.contains("gamma"), text);
        assertEquals(3, model.size());
    }

    @Test
    void fixedColumnsTableRendersFewerValuesThanColumns() throws IOException {
        TableModel model = TableModel.of(1f, 1f, 1f);
        FixedColumnsTable table = new FixedColumnsTable(model);
        table.addRow("only");

        String text = render(table);
        assertEquals("only", text);
        assertEquals(3, model.size());
    }

    private String render(Table table) throws IOException {
        try (Document document = new Document()) {
            document.render(table);
            try (PDDocument pdf = document.finish()) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setLineSeparator("\n");
                return stripper.getText(pdf).strip();
            }
        }
    }
}
