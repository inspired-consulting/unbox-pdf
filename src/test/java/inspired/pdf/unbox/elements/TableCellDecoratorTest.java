package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Stroke;
import inspired.pdf.unbox.base.TableModel;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static inspired.pdf.unbox.Unbox.background;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_100;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that a table cell applies its decorators exactly once per render.
 */
class TableCellDecoratorTest {

    @Test
    void textCellBackgroundIsDrawnOnce() throws IOException {
        FixedColumnsTable table = new FixedColumnsTable(TableModel.of(1f)).with(Stroke.none());
        table.addRow().addCell(new TextCell("decorated").with(background(GRAY_100)));

        assertEquals(1, countRectangles(table));
    }

    @Test
    void undecoratedTextCellDrawsNoRectangle() throws IOException {
        FixedColumnsTable table = new FixedColumnsTable(TableModel.of(1f)).with(Stroke.none());
        table.addRow().addCell(new TextCell("plain"));

        assertEquals(0, countRectangles(table));
    }

    /** Counts rectangle path operators on the first page; backgrounds emit one each. */
    private static int countRectangles(Table table) throws IOException {
        try (Document document = new Document()) {
            document.render(table);
            PDFStreamParser parser = new PDFStreamParser(document.finish().getPage(0));
            parser.parse();
            int count = 0;
            for (Object token : parser.getTokens()) {
                if (token instanceof Operator operator && operator.getName().equals("re")) {
                    count++;
                }
            }
            return count;
        }
    }
}
