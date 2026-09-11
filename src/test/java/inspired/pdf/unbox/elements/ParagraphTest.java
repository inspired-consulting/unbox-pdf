package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Overflow;
import inspired.pdf.unbox.internal.SimpleFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the {@code Paragraph.limit(...)} truncation contract, including the
 * {@code Overflow.ELLIPSIS} opt-in.
 */
class ParagraphTest {

    private static final String WRAPPING_TEXT =
            "The term Autokatalogaufkleberentfernungsettikettendruckerhersteller is too long for one line.";
    private static final float WRAPPING_MAX_WIDTH = 100;

    @Test
    void limitWithoutOverflowKeepsPlainClipBehavior() throws IOException {
        String plain = render(new Paragraph(WRAPPING_TEXT, SimpleFont.helvetica(9)).limit(1));
        String clip = render(new Paragraph(WRAPPING_TEXT, SimpleFont.helvetica(9)).limit(1).with(Overflow.CLIP));
        assertEquals(clip, plain);
        assertFalse(plain.contains("…"));
    }

    @Test
    void limitWithEllipsisAppendsMarkerWhenTruncated() throws IOException {
        String text = render(new Paragraph(WRAPPING_TEXT, SimpleFont.helvetica(9)).limit(1).with(Overflow.ELLIPSIS));
        assertTrue(text.endsWith("…"), "expected truncated paragraph to end with an ellipsis: " + text);
    }

    @Test
    void limitRejectsNonPositiveValues() {
        assertThrows(IllegalArgumentException.class, () -> new Paragraph("text").limit(0));
    }

    @Test
    void configurationOrderAndModeReplacementAreConsistent() throws IOException {
        String expected = render(new Paragraph("first\nsecond").limit(1).with(Overflow.ELLIPSIS));
        assertEquals(expected, render(new Paragraph("first\nsecond").with(Overflow.ELLIPSIS).limit(1)));
        assertEquals("first", render(new Paragraph("first\nsecond").limit(1)
                .with(Overflow.ELLIPSIS).with(Overflow.CLIP)));
        assertEquals("first", render(new Paragraph("first\nsecond").limit(1)
                .with(Overflow.OVERFLOW).with(Overflow.CLIP)));
    }

    @Test
    void heightAndLineLimitUseSmallerCapacity() throws IOException {
        float lineHeight = SimpleFont.helvetica(8).lineHeight();
        for (Overflow mode : Overflow.values()) {
            String expected = switch (mode) {
                case CLIP -> "first";
                case ELLIPSIS -> "first…";
                case OVERFLOW -> "first\nsecond\nthird";
            };
            assertEquals(expected, render(new Paragraph("first\nsecond\nthird")
                    .limit(2).withInnerHeight(lineHeight + 4).with(mode)));
            assertEquals(expected, render(new Paragraph("first\nsecond\nthird")
                    .limit(1).withInnerHeight(lineHeight * 2 + 4).with(mode)));
        }
    }

    @Test
    void noCompleteLineFitsWithoutOverflow() throws IOException {
        for (Overflow mode : new Overflow[]{Overflow.CLIP, Overflow.ELLIPSIS}) {
            assertEquals("", render(new Paragraph("first\nsecond").withInnerHeight(4).with(mode)));
        }
    }

    @Test
    void overflowingTextDoesNotIncreaseCursorAdvancement() throws IOException {
        for (boolean fixedHeight : new boolean[]{false, true}) {
            Paragraph paragraph = new Paragraph("first\nsecond\nthird").limit(1).with(Overflow.OVERFLOW);
            if (fixedHeight) {
                paragraph.withInnerHeight(20);
            }
            Document document = new Document();
            try {
                Bounds bounds = document.getPageBounds().width(WRAPPING_MAX_WIDTH);
                float allocated = paragraph.innerHeight(bounds);
                assertEquals(allocated, paragraph.render(document, bounds), 0.001f);
                try (PDDocument pdf = document.finish()) {
                    assertTrue(new PDFTextStripper().getText(pdf).contains("third"));
                }
            } finally {
                document.getDocument().close();
            }
        }
    }

    private String render(Paragraph paragraph) throws IOException {
        Document document = new Document();
        Bounds bounds = document.getPageBounds().width(WRAPPING_MAX_WIDTH);
        paragraph.render(document, bounds);
        try (PDDocument pdf = document.finish()) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setLineSeparator("\n");
            return stripper.getText(pdf).strip();
        }
    }
}
