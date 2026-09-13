package inspired.pdf.unbox;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifies the optional physical-layout ruler and its page-grid geometry. */
class RulerTest {

    @Test
    void millimetreRulerDrawsFullPageGrid() throws IOException {
        try (Document document = new Document()) {
            document.add(Ruler.mm());
            document.getPage();
            PDFStreamParser parser = new PDFStreamParser(document.finish().getPage(0));
            parser.parse();

            Lines lines = countLines(parser.getTokens());
            assertEquals(211, lines.vertical);
            assertEquals(298, lines.horizontal);
        }
    }

    @Test
    void rulerOptionsAreFluentAndValidated() {
        Ruler ruler = new Ruler(Length.of(1, Unit.CM), 5);
        assertSame(ruler, ruler.withMinorColor(Color.LIGHT_GRAY));
        assertSame(ruler, ruler.withMajorColor(Color.GRAY));
        assertSame(ruler, ruler.withMinorWidth(0.1f));
        assertSame(ruler, ruler.withMajorWidth(0.8f));
        assertThrows(IllegalArgumentException.class, () -> new Ruler(Length.of(0, Unit.MM), 10));
        assertThrows(IllegalArgumentException.class, () -> new Ruler(Length.of(1, Unit.MM), 0));
        assertThrows(IllegalArgumentException.class, () -> ruler.withMinorWidth(0));
        assertThrows(IllegalArgumentException.class, () -> ruler.withMajorWidth(Float.NaN));
        assertThrows(NullPointerException.class, () -> new Ruler(null, 10));
    }

    private static Lines countLines(List<Object> tokens) {
        Lines lines = new Lines();
        float moveX = 0;
        float moveY = 0;
        for (int i = 2; i < tokens.size(); i++) {
            if (tokens.get(i) instanceof Operator operator
                    && (operator.getName().equals("m") || operator.getName().equals("l"))) {
                float x = ((org.apache.pdfbox.cos.COSNumber) tokens.get(i - 2)).floatValue();
                float y = ((org.apache.pdfbox.cos.COSNumber) tokens.get(i - 1)).floatValue();
                if (operator.getName().equals("m")) {
                    moveX = x;
                    moveY = y;
                } else if (y == moveY && x != moveX) {
                    lines.horizontal++;
                } else if (x == moveX && y != moveY) {
                    lines.vertical++;
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
