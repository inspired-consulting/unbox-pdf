package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Overflow;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.VAlign;
import inspired.pdf.unbox.internal.SimpleFont;
import inspired.pdf.unbox.internal.TextWriter;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies paragraphs with several text runs: runs share a line and baseline, flow and
 * wrap as one text, get the height of the tallest run per line, and honor alignment,
 * line limits, and overflow like plain paragraphs.
 */
class TextRunTest {

    private static final Font BOLD = SimpleFont.helvetica_bold(10);
    private static final Font MUTED = new SimpleFont(org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA, 8, Color.GRAY);
    private static final Font BIG = SimpleFont.helvetica(20);
    private static final Bounds BOUNDS = new Bounds(50, 700, 200, 300);

    @Test
    void runsOnOneLineShareTheBaselineAndFollowEachOther() throws IOException {
        List<Shown> shown = render(new Paragraph("82 %", BOLD).add(" of rated load", MUTED).with(Padding.none()));

        assertEquals(2, shown.size());
        assertEquals("82 %", shown.get(0).text);
        assertEquals(" of rated load", shown.get(1).text);
        assertEquals(shown.get(0).y, shown.get(1).y, 0.001f, "same baseline");
        assertEquals(shown.get(0).x + BOLD.width("82 %"), shown.get(1).x, 0.001f, "second run starts where the first ends");
        assertEquals(new TextWriter(BOLD).baseline(BOUNDS, VAlign.TOP, 1), shown.get(0).y, 0.001f, "baseline of the tallest font");
    }

    @Test
    void wordsFlowAcrossRunsAndWrapAsOneText() throws IOException {
        String muted = "word ".repeat(80) + "END";
        Paragraph paragraph = new Paragraph("Main switchboard", BOLD).add(" " + muted, MUTED);

        String text = extract(paragraph);
        assertTrue(text.startsWith("Main switchboard word"), text);
        assertTrue(text.endsWith("END"), text);
        assertEquals(80, text.lines().flatMap(l -> java.util.Arrays.stream(l.split(" "))).filter("word"::equals).count());
        assertTrue(text.lines().count() > 1, "expected wrapping, got: " + text);
    }

    @Test
    void lineHeightIsTheTallestRunOnThatLine() {
        try (Document document = new Document()) {
            Paragraph mixed = new Paragraph("big", BIG).add(" small", MUTED).with(Padding.none());
            Paragraph small = new Paragraph("small", MUTED).with(Padding.none());

            assertEquals(BIG.lineHeight(), mixed.innerHeight(BOUNDS), 0.001f);
            assertEquals(MUTED.lineHeight(), small.innerHeight(BOUNDS), 0.001f);
        }
    }

    @Test
    void measuredHeightMatchesRenderedHeight() {
        try (Document document = new Document()) {
            Paragraph paragraph = new Paragraph("Main switchboard", BOLD)
                    .add(" " + "word ".repeat(20) + "END", MUTED);
            float measured = paragraph.innerHeight(BOUNDS);
            float rendered = paragraph.render(document, BOUNDS);
            assertEquals(measured, rendered, 0.001f);
        }
    }

    @Test
    void rightAlignmentUsesTheCombinedLineWidth() throws IOException {
        List<Shown> shown = render(new Paragraph("82 %", BOLD).add(" load", MUTED).align(Align.RIGHT).with(Padding.none()));

        float lineWidth = BOLD.width("82 %") + MUTED.width(" load");
        assertEquals((int) (BOUNDS.right() - lineWidth), shown.get(0).x, 0.001f);
    }

    @Test
    void lineLimitAndEllipsisApplyToRuns() throws IOException {
        Paragraph paragraph = new Paragraph("Main switchboard", BOLD)
                .add(" " + "word ".repeat(80) + "END", MUTED)
                .limit(1)
                .with(Overflow.ELLIPSIS);

        String text = extract(paragraph);
        assertEquals(1, text.lines().count(), text);
        assertTrue(text.endsWith("…"), text);
    }

    @Test
    void lineBreaksInsideARunStartNewLines() throws IOException {
        List<Shown> shown = render(new Paragraph("first", BOLD).add("\nsecond", MUTED).with(Padding.none()));

        assertEquals(2, shown.size());
        assertTrue(shown.get(0).y > shown.get(1).y);
        assertEquals(shown.get(0).x, shown.get(1).x, 0.001f);
    }

    @Test
    void addWithoutFontUsesTheParagraphFont() {
        Paragraph paragraph = new Paragraph("a", BOLD).add("b").add("c", MUTED);
        assertEquals(3, paragraph.runs().size());
        assertEquals(BOLD, paragraph.runs().get(1).font());
        assertEquals("Paragraph['abc']", paragraph.toString());
    }

    // -- helpers

    private record Shown(String text, float x, float y) { }

    /** Every shown text piece with its position on the first page. */
    private static List<Shown> render(Paragraph paragraph) throws IOException {
        try (Document document = new Document()) {
            paragraph.render(document, BOUNDS);
            PDFStreamParser parser = new PDFStreamParser(document.finish().getPage(0));
            parser.parse();
            List<Object> tokens = parser.getTokens();
            List<Shown> shown = new ArrayList<>();
            float x = 0;
            float y = 0;
            for (int i = 0; i < tokens.size(); i++) {
                if (tokens.get(i) instanceof Operator operator) {
                    if (operator.getName().equals("Td")) {
                        x = ((COSNumber) tokens.get(i - 2)).floatValue();
                        y = ((COSNumber) tokens.get(i - 1)).floatValue();
                    } else if (operator.getName().equals("Tj")) {
                        shown.add(new Shown(((COSString) tokens.get(i - 1)).getString(), x, y));
                    }
                }
            }
            return shown;
        }
    }

    private static String extract(Paragraph paragraph) throws IOException {
        try (Document document = new Document()) {
            document.render(paragraph);
            try (PDDocument pdf = document.finish()) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setLineSeparator("\n");
                return stripper.getText(pdf).strip();
            }
        }
    }

    @Test
    void verticalParagraphRejectsRuns() {
        VerticalParagraph vertical = new VerticalParagraph("only one run");
        assertThrows(UnsupportedOperationException.class, () -> vertical.add("more", MUTED));
        assertThrows(UnsupportedOperationException.class, () -> vertical.add("more"));
    }
}
