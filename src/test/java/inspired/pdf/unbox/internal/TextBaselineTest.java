package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.VAlign;
import inspired.pdf.unbox.elements.Paragraph;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the public baseline and cap height metrics against the position where
 * text is actually written, so drawn content can be aligned with text lines.
 */
class TextBaselineTest {

    private static final Font FONT = SimpleFont.helvetica(10);
    private static final Bounds BOUNDS = new Bounds(50, 700, 200, 100);

    @Test
    void baselineMatchesWrittenTextForEachVerticalAlignment() throws IOException {
        for (VAlign vAlign : VAlign.values()) {
            try (Document document = new Document()) {
                TextWriter writer = new TextWriter(FONT);
                writer.write(document.getContentStream(), BOUNDS, "one line", Align.LEFT, vAlign);

                assertEquals(writer.baseline(BOUNDS, vAlign, 1), firstTextY(document), 0.001f, vAlign.name());
            }
        }
    }

    @Test
    void baselineOfMultiLineBlockIsTheFirstLine() throws IOException {
        try (Document document = new Document()) {
            TextWriter writer = new TextWriter(FONT);
            writer.write(document.getContentStream(), BOUNDS, "first\nsecond\nthird", Align.LEFT, VAlign.BOTTOM);

            assertEquals(writer.baseline(BOUNDS, VAlign.BOTTOM, 3), firstTextY(document), 0.001f);
        }
    }

    @Test
    void paragraphTextStartsAtBaselineOfPaddedBounds() throws IOException {
        try (Document document = new Document()) {
            Paragraph paragraph = new Paragraph("42 %", FONT).with(Padding.of(3, 8));
            paragraph.render(document, BOUNDS);

            Bounds textBounds = BOUNDS.apply(paragraph.padding());
            float expected = new TextWriter(FONT).baseline(textBounds, VAlign.TOP, 1);
            assertEquals(expected, firstTextY(document), 0.001f);
        }
    }

    @Test
    void capHeightLiesBetweenZeroAndLineHeight() {
        assertTrue(FONT.capHeight() > 0);
        assertTrue(FONT.capHeight() < FONT.lineHeight());
        // Helvetica declares a cap height of 718 units.
        assertEquals(7.18f, FONT.capHeight(), 0.001f);
    }

    @Test
    void paragraphExposesDefaultAndConfiguredPadding() {
        assertEquals(Padding.of(2), new Paragraph("x").padding());
        assertEquals(Padding.of(5, 9), new Paragraph("x").with(Padding.of(5, 9)).padding());
    }

    // -- helpers

    /** The y coordinate of the first text positioning operator on the first page. */
    private static float firstTextY(Document document) throws IOException {
        PDFStreamParser parser = new PDFStreamParser(document.finish().getPage(0));
        parser.parse();
        List<Object> tokens = parser.getTokens();
        for (int i = 2; i < tokens.size(); i++) {
            if (tokens.get(i) instanceof Operator operator && operator.getName().equals("Td")) {
                return ((COSNumber) tokens.get(i - 1)).floatValue();
            }
        }
        throw new AssertionError("no text was written");
    }
}
