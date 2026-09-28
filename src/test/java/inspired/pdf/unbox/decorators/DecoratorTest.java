package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Unbox;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.Paragraph;
import inspired.pdf.unbox.elements.PdfElement;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies decorator ordering by level: defaults paint backgrounds before borders,
 * {@link Decorator#atLevel(int)} overrides the level at any time before rendering,
 * equal levels keep insertion order, and containers keep insertion order regardless of level.
 */
class DecoratorTest {

    private static final Bounds BOUNDS = new Bounds(50, 700, 200, 40);

    private final List<String> painted = new ArrayList<>();

    @Test
    void backgroundPaintsBeforeBorderWhenAddedFirst() throws IOException {
        var paragraph = new Paragraph("box").with(Unbox.background(Color.LIGHT_GRAY)).with(border(1f, Color.BLACK));
        assertEquals("fS", fillStrokeOrder(paragraph));
    }

    @Test
    void backgroundPaintsBeforeBorderWhenAddedLast() throws IOException {
        var paragraph = new Paragraph("box").with(border(1f, Color.BLACK)).with(Unbox.background(Color.LIGHT_GRAY));
        assertEquals("fS", fillStrokeOrder(paragraph));
    }

    @Test
    void atLevelReturnsSameInstanceAndSetsLevel() {
        BackgroundDecorator background = Unbox.background(Color.WHITE);
        Decorator result = background.atLevel(50);
        assertSame(background, result);
        assertTrue(background.compareTo(new Recording("other", 100)) < 0);
    }

    @Test
    void atLevelOverrideReversesDefaultOrder() throws IOException {
        var paragraph = new Paragraph("box")
                .with(Unbox.background(Color.LIGHT_GRAY))
                .with(border(1f, Color.BLACK).atLevel(0));
        assertEquals("Sf", fillStrokeOrder(paragraph));
    }

    @Test
    void loweredBackgroundStillPaintsFirst() throws IOException {
        var paragraph = new Paragraph("box")
                .with(Unbox.background(Color.WHITE).atLevel(0))
                .with(border(1f, Color.BLACK));
        assertEquals("fS", fillStrokeOrder(paragraph));
    }

    @Test
    void equalLevelsPaintInInsertionOrder() throws IOException {
        var paragraph = new Paragraph("box")
                .with(new Recording("A", 500))
                .with(new Recording("B", 500))
                .with(new Recording("C", 500));
        render(paragraph);
        assertEquals(List.of("A", "B", "C"), painted);
    }

    @Test
    void atLevelAfterAttachingAffectsFirstRender() throws IOException {
        var a = new Recording("A", 100);
        var paragraph = new Paragraph("box").with(a).with(new Recording("B", 200));
        a.atLevel(300);
        render(paragraph);
        assertEquals(List.of("B", "A"), painted);
    }

    @Test
    void atLevelBetweenRendersAffectsNextRender() throws IOException {
        var b = new Recording("B", 200);
        var paragraph = new Paragraph("box").with(new Recording("A", 100)).with(b);
        render(paragraph);
        assertEquals(List.of("A", "B"), painted);

        painted.clear();
        b.atLevel(50);
        render(paragraph);
        assertEquals(List.of("B", "A"), painted);
    }

    @Test
    void containerKeepsInsertionOrderRegardlessOfLevel() throws IOException {
        Container bordered = Unbox.column().add(new Paragraph("box"));
        bordered.with(border(1f, Color.BLACK)).with(Unbox.background(Color.LIGHT_GRAY));
        assertEquals("Sf", fillStrokeOrder(bordered));

        Container recorded = Unbox.column().add(new Paragraph("box"));
        recorded.with(new Recording("high", 1000)).with(new Recording("low", 1000).atLevel(0));
        render(recorded);
        assertEquals(List.of("high", "low"), painted);
    }

    @Test
    void tiesKeepInsertionOrderAfterEarlierResort() throws IOException {
        var b = new Recording("B", 5);
        var paragraph = new Paragraph("box").with(new Recording("A", 10)).with(b);
        render(paragraph);
        assertEquals(List.of("B", "A"), painted);

        painted.clear();
        b.atLevel(10);
        render(paragraph);
        assertEquals(List.of("A", "B"), painted);
    }

    @Test
    void extremeLevelsCompareWithoutOverflow() {
        var low = new Recording("LOW", 0).atLevel(Integer.MIN_VALUE);
        var high = new Recording("HIGH", 0).atLevel(Integer.MAX_VALUE);
        var one = new Recording("ONE", 1);
        assertTrue(low.compareTo(high) < 0);
        assertTrue(high.compareTo(low) > 0);
        assertTrue(low.compareTo(one) < 0);
        assertTrue(one.compareTo(low) > 0);
    }

    @Test
    void extremeLevelsPaintLowFirstInEitherInsertionOrder() throws IOException {
        var low = new Recording("LOW", 0).atLevel(Integer.MIN_VALUE);
        var high = new Recording("HIGH", 0).atLevel(Integer.MAX_VALUE);

        render(new Paragraph("box").with(high).with(low));
        assertEquals(List.of("LOW", "HIGH"), painted);

        painted.clear();
        render(new Paragraph("box").with(low).with(high));
        assertEquals(List.of("LOW", "HIGH"), painted);
    }

    // -- helpers

    private static void render(PdfElement element) throws IOException {
        try (Document document = new Document()) {
            element.render(document, BOUNDS);
        }
    }

    /**
     * Renders the element and returns the order of the first fill ({@code f}) and first
     * stroke ({@code S}) operator, e.g. {@code "fS"} when the background paints first.
     */
    private static String fillStrokeOrder(PdfElement element) throws IOException {
        try (Document document = new Document()) {
            element.render(document, BOUNDS);
            PDFStreamParser parser = new PDFStreamParser(document.finish().getPage(0));
            parser.parse();
            StringBuilder order = new StringBuilder();
            for (Object token : parser.getTokens()) {
                if (token instanceof Operator operator) {
                    String name = operator.getName();
                    if ((name.equals("f") || name.equals("S")) && order.indexOf(name) < 0) {
                        order.append(name);
                    }
                }
            }
            return order.toString();
        }
    }

    /**
     * Records its name into {@link #painted} when it is painted.
     */
    private final class Recording extends Decorator {

        private final String name;

        Recording(String name, int level) {
            super(level);
            this.name = name;
        }

        @Override
        public float decorate(Document document, Bounds viewPort) {
            painted.add(name);
            return 0;
        }
    }
}
