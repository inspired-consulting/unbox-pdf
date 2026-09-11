package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.Unbox;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that stretch layouts set rendering hints per render instead of
 * accumulating them, so the same container renders identically every time.
 */
class StretchLayoutTest {

    private static final String TALL = "one\ntwo\nthree\nfour";

    @Test
    void repeatedRenderOfStretchRowIsIdempotent() {
        try (Document document = new Document()) {
            Paragraph shortChild = new Paragraph("short");
            Container row = Unbox.rowStretch().add(shortChild).add(new Paragraph(TALL));
            Bounds viewPort = document.getCurrentViewPort();

            float first = row.render(document, viewPort);
            Padding firstPadding = shortChild.renderingHints().getExtraPadding();
            float second = row.render(document, viewPort);
            Padding secondPadding = shortChild.renderingHints().getExtraPadding();

            assertTrue(firstPadding.bottom() > 0, "short child should be stretched");
            assertEquals(firstPadding, secondPadding);
            assertEquals(first, second, 0.001f);
        }
    }

    @Test
    void nestedStretchColumnRendersIdenticallyOnRepeat() {
        try (Document document = new Document()) {
            Paragraph inner = new Paragraph("inner");
            Container column = Unbox.columnStretch().add(inner);
            Container row = Unbox.rowStretch().add(column).add(new Paragraph(TALL));
            Bounds viewPort = document.getCurrentViewPort();

            float first = row.render(document, viewPort);
            Padding firstPadding = inner.renderingHints().getExtraPadding();
            float second = row.render(document, viewPort);
            Padding secondPadding = inner.renderingHints().getExtraPadding();

            assertTrue(firstPadding.bottom() > 0, "inner child should receive the column stretch");
            assertEquals(firstPadding, secondPadding);
            assertEquals(first, second, 0.001f);
        }
    }

    @Test
    void measurementIsStableAcrossRenders() {
        try (Document document = new Document()) {
            Container column = Unbox.columnStretch().add(new Paragraph("inner"));
            Container row = Unbox.rowStretch().add(column).add(new Paragraph(TALL));
            Bounds viewPort = document.getCurrentViewPort();

            float before = row.innerHeight(viewPort);
            row.render(document, viewPort);
            float after = row.innerHeight(viewPort);

            assertEquals(before, after, 0.001f);
        }
    }

    @Test
    void elementMovedToPlainRowLosesStretchPadding() {
        try (Document document = new Document()) {
            Paragraph moved = new Paragraph("moved");
            Bounds viewPort = document.getCurrentViewPort();
            Unbox.rowStretch().add(moved).add(new Paragraph(TALL)).render(document, viewPort);
            assertTrue(moved.renderingHints().getExtraPadding().bottom() > 0);

            Unbox.row().add(moved).add(new Paragraph(TALL)).render(document, viewPort);

            assertEquals(Padding.of(0), moved.renderingHints().getExtraPadding());
        }
    }
}
