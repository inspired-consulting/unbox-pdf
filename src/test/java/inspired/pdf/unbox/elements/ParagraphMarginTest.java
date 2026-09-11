package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.Unbox;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that paragraph measurement uses the same effective width as rendering
 * when horizontal margins are set, so wrapped text is neither cut off nor
 * overlapped by the following element.
 */
class ParagraphMarginTest {

    private static final String LONG_TEXT = "word ".repeat(30) + "END";

    @Test
    void leftMarginKeepsCompleteText() throws IOException {
        String text = renderInDocument(new Paragraph(LONG_TEXT).with(Margin.left(450)));
        assertTrue(text.endsWith("END"), "expected complete text, got: " + text);
        assertEquals(30, countWords(text));
    }

    @Test
    void rightMarginKeepsCompleteText() throws IOException {
        String text = renderInDocument(new Paragraph(LONG_TEXT).with(Margin.right(450)));
        assertTrue(text.endsWith("END"), "expected complete text, got: " + text);
        assertEquals(30, countWords(text));
    }

    @Test
    void marginAndPaddingKeepCompleteText() throws IOException {
        String text = renderInDocument(new Paragraph(LONG_TEXT)
                .with(Margin.of(0, 200, 0, 200))
                .with(Padding.of(5, 50)));
        assertTrue(text.endsWith("END"), "expected complete text, got: " + text);
        assertEquals(30, countWords(text));
    }

    @Test
    void marginInsideContainerKeepsCompleteText() throws IOException {
        String text = renderInDocument(Unbox.column()
                .add(new Paragraph(LONG_TEXT).with(Margin.of(0, 200, 0, 250))));
        assertTrue(text.endsWith("END"), "expected complete text, got: " + text);
        assertEquals(30, countWords(text));
    }

    @Test
    void measuredHeightMatchesRenderedHeight() throws IOException {
        try (Document document = new Document()) {
            Paragraph paragraph = new Paragraph(LONG_TEXT).with(Margin.of(0, 100, 0, 350));
            Bounds viewPort = document.getCurrentViewPort();
            float measured = paragraph.innerHeight(viewPort);
            float rendered = paragraph.render(document, viewPort);
            assertEquals(measured, rendered, 0.01f);

            // The same text at the effective width measures the same height without margin.
            Paragraph unmargined = new Paragraph(LONG_TEXT);
            assertEquals(unmargined.innerHeight(viewPort.apply(Margin.of(0, 100, 0, 350))), measured, 0.01f);
        }
    }

    @Test
    void nextElementIsPlacedAfterCompleteParagraph() throws IOException {
        try (Document document = new Document()) {
            Paragraph paragraph = new Paragraph(LONG_TEXT).with(Margin.of(0, 100, 0, 350));
            float expected = paragraph.outerHeight(document.getCurrentViewPort());
            float before = document.getPosition();
            document.render(paragraph);
            assertEquals(expected, before - document.getPosition(), 0.01f);
        }
    }

    @Test
    void lineLimitStillTruncatesWithMargin() throws IOException {
        String text = renderInDocument(new Paragraph(LONG_TEXT).with(Margin.left(450)).limit(2));
        assertEquals(2, text.split("\n").length);
    }

    @Test
    void fixedInnerHeightIsUnaffectedByMargin() {
        try (Document document = new Document()) {
            Paragraph paragraph = new Paragraph(LONG_TEXT).withInnerHeight(40).with(Margin.left(450));
            assertEquals(40, paragraph.innerHeight(document.getCurrentViewPort()), 0.01f);
        }
    }

    // -- helpers

    private String renderInDocument(PdfElement element) throws IOException {
        try (Document document = new Document()) {
            document.render(element);
            try (PDDocument pdf = document.finish()) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setLineSeparator("\n");
                return stripper.getText(pdf).strip();
            }
        }
    }

    private int countWords(String text) {
        return (int) text.lines().flatMap(l -> java.util.Arrays.stream(l.split(" "))).filter("word"::equals).count();
    }
}
