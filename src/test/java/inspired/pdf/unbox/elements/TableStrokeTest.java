package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Stroke;
import inspired.pdf.unbox.base.TableModel;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that row lines and column lines of a table can be styled independently,
 * while {@code with(Stroke)} still controls both.
 */
class TableStrokeTest {

    private static final Stroke STROKE = new Stroke(1f);

    @Test
    void defaultDrawsRowAndColumnLines() throws IOException {
        Lines lines = render(new FixedColumnsTable(TableModel.of(1f, 1f, 1f)));
        assertEquals(4, lines.horizontal, "two rows draw a line above and below each");
        assertEquals(4, lines.vertical, "two dividers per row");
    }

    @Test
    void rowStrokeOnlyDrawsHorizontalLines() throws IOException {
        Lines lines = render(new FixedColumnsTable(TableModel.of(1f, 1f, 1f))
                .withRowStroke(STROKE)
                .withColumnStroke(Stroke.none()));
        assertEquals(4, lines.horizontal);
        assertEquals(0, lines.vertical);
    }

    @Test
    void columnStrokeOnlyDrawsVerticalLines() throws IOException {
        Lines lines = render(new FixedColumnsTable(TableModel.of(1f, 1f, 1f))
                .withRowStroke(Stroke.none())
                .withColumnStroke(STROKE));
        assertEquals(0, lines.horizontal);
        assertEquals(4, lines.vertical);
    }

    @Test
    void strokeSetsBothAxes() throws IOException {
        Lines none = render(new FixedColumnsTable(TableModel.of(1f, 1f, 1f)).with(Stroke.none()));
        assertEquals(0, none.horizontal + none.vertical);

        Lines both = render(new FixedColumnsTable(TableModel.of(1f, 1f, 1f))
                .withRowStroke(Stroke.none())
                .with(STROKE));
        assertEquals(4, both.horizontal, "with(Stroke) overrides an earlier row stroke");
        assertEquals(4, both.vertical);
    }

    @Test
    void flexTableSupportsAxisStrokesFluently() throws IOException {
        FlexTable table = new FlexTable().withRowStroke(STROKE).withColumnStroke(Stroke.none());
        table.addRow("a", "b", "c");
        table.addRow("d", "e", "f");

        Lines lines = render(table);
        assertEquals(4, lines.horizontal);
        assertEquals(0, lines.vertical);
    }

    // -- helpers

    private static Lines render(Table table) throws IOException {
        if (table.getClass() == FixedColumnsTable.class) {
            table.addRow("a", "b", "c");
            table.addRow("d", "e", "f");
        }
        try (Document document = new Document()) {
            document.render(table);
            PDFStreamParser parser = new PDFStreamParser(document.finish().getPage(0));
            parser.parse();
            return countLines(parser.getTokens());
        }
    }

    /** Counts straight line segments by orientation, from move-to and line-to operator pairs. */
    private static Lines countLines(List<Object> tokens) {
        Lines lines = new Lines();
        float moveX = 0;
        float moveY = 0;
        for (int i = 2; i < tokens.size(); i++) {
            if (tokens.get(i) instanceof Operator operator
                    && (operator.getName().equals("m") || operator.getName().equals("l"))) {
                float x = ((COSNumber) tokens.get(i - 2)).floatValue();
                float y = ((COSNumber) tokens.get(i - 1)).floatValue();
                if (operator.getName().equals("m")) {
                    moveX = x;
                    moveY = y;
                } else {
                    if (y == moveY && x != moveX) {
                        lines.horizontal++;
                    } else if (x == moveX && y != moveY) {
                        lines.vertical++;
                    }
                }
            }
        }
        return lines;
    }

    private static final class Lines {
        int horizontal;
        int vertical;
    }
}
