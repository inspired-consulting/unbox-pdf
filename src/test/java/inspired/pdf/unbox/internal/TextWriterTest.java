package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Overflow;
import inspired.pdf.unbox.VAlign;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies bounded text rendering and the explicit overflow contract.
 */
class TextWriterTest {

    @Test
    void overflowIsDisabledByDefault() throws IOException {
        assertEquals("first", render(new TextWriter(SimpleFont.helvetica(10))));
    }

    @Test
    void explicitFalseClipsTextToBounds() throws IOException {
        assertEquals("first", render(new TextWriter(SimpleFont.helvetica(10)).withOverflow(false)));
    }

    @Test
    void explicitTrueAllowsTextBeyondBounds() throws IOException {
        assertEquals("first\nsecond", render(new TextWriter(SimpleFont.helvetica(10)).withOverflow(true)));
    }

    @Test
    void overflowCanBeDisabledAfterEnablingIt() throws IOException {
        TextWriter writer = new TextWriter(SimpleFont.helvetica(10)).withOverflow(true).withOverflow(false);
        assertEquals("first", render(writer));
    }

    // Fixture shared by the ellipsis tests below: with helvetica(9) and maxWidth 100, this
    // text wraps to exactly 5 lines, the second of which is a mid-word break produced by
    // TextTokenizer.breakUp() (see TextTokenizerTest#wordsWillBeBrokenIfTooLong).
    private static final String WRAPPING_TEXT =
            "The term Autokatalogaufkleberentfernungsettikettendruckerhersteller is too long for one line.";
    private static final float WRAPPING_MAX_WIDTH = 100;
    private static final Font WRAPPING_FONT = SimpleFont.helvetica(9);

    @Test
    void truncatingLineLimitAppendsEllipsis() throws IOException {
        String[] lines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 1, Overflow.ELLIPSIS);
        assertEquals(1, lines.length);
        String lastLine = lines[lines.length - 1];
        assertTrue(lastLine.endsWith("…"), "expected last line to end with an ellipsis: " + lastLine);
        assertTrue(WRAPPING_FONT.width(lastLine) <= WRAPPING_MAX_WIDTH);
    }

    @Test
    void nonTruncatingLineLimitLeavesOutputUnchanged() throws IOException {
        String[] ellipsisLines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 5, Overflow.ELLIPSIS);
        String[] clipLines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 5, Overflow.CLIP);
        assertEquals(String.join("\n", clipLines), String.join("\n", ellipsisLines));
        for (String line : ellipsisLines) {
            assertFalse(line.contains("…"), "did not expect an ellipsis in: " + line);
        }
    }

    @Test
    void truncationMidWordTrimsFurtherThanPlainClip() throws IOException {
        String[] ellipsisLines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 2, Overflow.ELLIPSIS);
        String[] clipLines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 2, Overflow.CLIP);

        String ellipsisLastLine = ellipsisLines[ellipsisLines.length - 1];
        String clipLastLine = clipLines[clipLines.length - 1];

        assertTrue(ellipsisLastLine.endsWith("…"), "expected last line to end with an ellipsis: " + ellipsisLastLine);
        assertTrue(WRAPPING_FONT.width(ellipsisLastLine) <= WRAPPING_MAX_WIDTH);
        assertTrue(ellipsisLastLine.length() < clipLastLine.length(),
                "expected ellipsis line to be trimmed shorter than the plain clip: "
                        + ellipsisLastLine + " vs " + clipLastLine);
    }

    @Test
    void defaultLimitKeepsTodaysOutputByteForByte() throws IOException {
        String[] legacyLines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 2, null);
        String[] clipLines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 2, Overflow.CLIP);
        assertEquals(String.join("\n", clipLines), String.join("\n", legacyLines));
    }

    @Test
    void pathologicalWidthReturnsBareEllipsis() {
        TextWriter writer = new TextWriter(SimpleFont.helvetica(10));
        String result = writer.withEllipsis("hello", 0.0001f);
        assertEquals("…", result);
    }

    @Test
    void ellipsisFitsWithoutTrimming() {
        Font font = SimpleFont.helvetica(10);
        TextWriter writer = new TextWriter(font);
        String result = writer.withEllipsis("Hi", 1000f);
        assertEquals("Hi…", result);
    }

    @Test
    void onlyLastKeptLineIsTouched() throws IOException {
        String[] ellipsisLines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 2, Overflow.ELLIPSIS);
        String[] clipLines = renderLines(WRAPPING_FONT, WRAPPING_MAX_WIDTH, WRAPPING_TEXT, 2, Overflow.CLIP);

        assertEquals(clipLines.length, ellipsisLines.length);
        for (int i = 0; i < clipLines.length - 1; i++) {
            assertEquals(clipLines[i], ellipsisLines[i]);
        }
        assertNotEquals(clipLines[clipLines.length - 1], ellipsisLines[ellipsisLines.length - 1]);
    }

    private String[] renderLines(Font font, float maxWidth, String text, Integer lineLimit, Overflow overflow) throws IOException {
        try (PDDocument pdf = new PDDocument()) {
            PDPage page = new PDPage();
            pdf.addPage(page);
            Bounds bounds = new Bounds(20, 400, maxWidth, font.lineHeight() * 10);
            try (PDPageContentStream stream = new PDPageContentStream(pdf, page)) {
                TextWriter writer = new TextWriter(font);
                if (overflow == null) {
                    writer.write(stream, bounds, text, Align.LEFT, VAlign.TOP, lineLimit);
                } else {
                    writer.write(stream, bounds, text, Align.LEFT, VAlign.TOP, lineLimit, overflow);
                }
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");
            return stripper.getText(pdf).strip().split("\n");
        }
    }

    private String render(TextWriter writer) throws IOException {
        try (PDDocument pdf = new PDDocument()) {
            PDPage page = new PDPage();
            pdf.addPage(page);
            Bounds bounds = new Bounds(20, 100, 200, SimpleFont.helvetica(10).lineHeight());
            try (PDPageContentStream stream = new PDPageContentStream(pdf, page)) {
                writer.write(stream, bounds, "first\nsecond");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");
            return stripper.getText(pdf).strip();
        }
    }
}
