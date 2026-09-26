package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that text cells and rows return the same height from rendering as from measuring,
 * so a row rendered outside a table advances the cursor by its measured height.
 */
class TextCellHeightTest {

    private static final String LONG_TEXT = "This text is long enough to wrap onto several lines in a narrow cell.";

    @Test
    void singleLineCellRendersMeasuredHeight() throws IOException {
        assertRenderMatchesMeasure(new TextCell("short"), 200);
    }

    @Test
    void wrappedCellRendersMeasuredHeight() throws IOException {
        assertRenderMatchesMeasure(new TextCell(LONG_TEXT), 60);
    }

    @Test
    void rowRendersMeasuredHeight() throws IOException {
        TableRow row = new TableRow().withCells("short", LONG_TEXT);
        try (Document document = new Document()) {
            Bounds bounds = document.getCurrentViewPort().width(200);
            float measured = row.innerHeight(bounds);

            assertEquals(measured, row.render(document, bounds.height(measured)));
        }
    }

    private void assertRenderMatchesMeasure(TextCell cell, float width) throws IOException {
        try (Document document = new Document()) {
            Bounds bounds = document.getCurrentViewPort().width(width);
            float measured = cell.innerHeight(bounds);

            assertEquals(measured, cell.render(document, bounds.height(measured)));
        }
    }
}
