package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Border;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Unbox;
import inspired.pdf.unbox.elements.Paragraph;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies rounded corners on border and background decorators: a positive radius
 * traces four curves, a zero radius keeps the plain rectangle, and invalid
 * combinations are rejected.
 */
class RoundedCornerTest {

    private static final Bounds BOUNDS = new Bounds(50, 700, 200, 40);

    @Test
    void roundedBorderTracesFourCurves() throws IOException {
        Ops ops = render(border(Border.of(1).withRadius(4), Color.BLACK));
        assertEquals(4, ops.curves);
        assertEquals(0, ops.rectangles);
    }

    @Test
    void roundedBackgroundTracesFourCurves() throws IOException {
        Ops ops = render(Unbox.background(Color.LIGHT_GRAY, 4));
        assertEquals(4, ops.curves);
        assertEquals(0, ops.rectangles);
    }

    @Test
    void zeroRadiusKeepsPlainRectangles() throws IOException {
        assertEquals(1, render(border(Border.of(1), Color.BLACK)).rectangles);
        assertEquals(0, render(border(Border.of(1), Color.BLACK)).curves);
        assertEquals(1, render(Unbox.background(Color.LIGHT_GRAY)).rectangles);
        assertEquals(1, render(Unbox.background(Color.LIGHT_GRAY, 0)).rectangles);
    }

    @Test
    void radiusIsClampedToHalfOfTheSmallerSide() throws IOException {
        // Height 40, requested radius 100: the path starts at left + 20.
        Ops ops = render(border(Border.of(1).withRadius(100), Color.BLACK));
        assertEquals(BOUNDS.left() + 20, ops.firstMoveX, 0.001f);
        assertEquals(20, RoundedRectangle.clamp(100, BOUNDS), 0.001f);
    }

    @Test
    void fourArgumentConstructorAndFactoriesKeepSquareCorners() {
        assertEquals(0, new Border(1, 2, 3, 4).radius());
        assertEquals(0, Border.of(1).radius());
        assertEquals(new Border(1, 1, 1, 1, 3), Border.of(1).withRadius(3));
    }

    @Test
    void invalidRadiusCombinationsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Border.of(1).withRadius(-1));
        assertThrows(IllegalArgumentException.class, () -> Border.of(1, 2).withRadius(3));
        assertThrows(IllegalArgumentException.class, () -> Border.top(1).withRadius(3));
        assertThrows(IllegalArgumentException.class, () -> new BackgroundDecorator(Color.RED, -2));
    }

    // -- helpers

    private static Ops render(Decorator decorator) throws IOException {
        try (Document document = new Document()) {
            // A fixed inner height keeps the decorated bounds at the full 40 points.
            new Paragraph("box").withInnerHeight(BOUNDS.height()).with(decorator).render(document, BOUNDS);
            PDFStreamParser parser = new PDFStreamParser(document.finish().getPage(0));
            parser.parse();
            return count(parser.getTokens());
        }
    }

    private static Ops count(List<Object> tokens) {
        Ops ops = new Ops();
        boolean firstMove = true;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i) instanceof Operator operator) {
                switch (operator.getName()) {
                    case "c" -> ops.curves++;
                    case "re" -> ops.rectangles++;
                    case "m" -> {
                        if (firstMove) {
                            ops.firstMoveX = ((COSNumber) tokens.get(i - 2)).floatValue();
                            firstMove = false;
                        }
                    }
                    default -> { }
                }
            }
        }
        return ops;
    }

    private static final class Ops {
        int curves;
        int rectangles;
        float firstMoveX;
    }
}
