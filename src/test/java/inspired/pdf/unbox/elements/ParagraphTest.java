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
        String clip = render(new Paragraph(WRAPPING_TEXT, SimpleFont.helvetica(9)).limit(1, Overflow.CLIP));
        assertEquals(clip, plain);
        assertTrue(!plain.contains("…"));
    }

    @Test
    void limitWithEllipsisAppendsMarkerWhenTruncated() throws IOException {
        String text = render(new Paragraph(WRAPPING_TEXT, SimpleFont.helvetica(9)).limit(1, Overflow.ELLIPSIS));
        assertTrue(text.endsWith("…"), "expected truncated paragraph to end with an ellipsis: " + text);
    }

    @Test
    void limitRejectsNonPositiveValues() {
        assertThrows(IllegalArgumentException.class, () -> new Paragraph("text").limit(0, Overflow.ELLIPSIS));
    }

    private String render(Paragraph paragraph) throws IOException {
        Document document = new Document();
        Bounds bounds = document.getPageBounds().width(WRAPPING_MAX_WIDTH);
        paragraph.render(document, bounds);
        PDDocument pdf = document.finish();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setLineSeparator("\n");
        return stripper.getText(pdf).strip();
    }
}
