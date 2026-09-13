package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.io.IOException;
import java.util.List;

import static inspired.pdf.unbox.Unbox.paragraph;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies face-level strict encoding and default replacement using a standard font,
 * so the policy tests run without installed font files.
 */
class FontFaceTest {

    @Test
    void strictFontsRejectUnsupportedTextDuringMeasurementAndRendering() {
        FontFace face = new FontFace(PDType1Font.HELVETICA).strict();
        for (Font font : List.of(face.at(10), face.at(16, Color.BLUE))) {
            PdfUnboxException measurement = assertThrows(PdfUnboxException.class,
                    () -> font.width("aφb"));
            assertTrue(measurement.getMessage().contains("U+03C6"), measurement.getMessage());
            assertTrue(measurement.getMessage().contains("Helvetica"), measurement.getMessage());

            try (Document document = new Document()) {
                PdfUnboxException rendering = assertThrows(PdfUnboxException.class,
                        () -> document.render(paragraph("aφb", font)));
                assertEquals(measurement.getMessage(), rendering.getMessage());
            }
        }
    }

    @Test
    void strictFontsRenderSupportedTextAndRetainSizeAndColor() throws IOException {
        FontFace face = new FontFace(PDType1Font.HELVETICA).strict();
        Font small = face.at(10);
        Font large = face.at(20, Color.BLUE);
        assertEquals(10f, small.getSize());
        assertEquals(Color.BLACK, small.getColor());
        assertEquals(20f, large.getSize());
        assertEquals(Color.BLUE, large.getColor());
        assertEquals(2 * small.width("Größe"), large.width("Größe"));
        assertSame(face.getFont(), small.getFont());
        assertSame(face.getFont(), large.getFont());

        try (Document document = new Document()) {
            document.render(paragraph("Größe: 5 °C – ok", small).add(" groß", large));
            try (PDDocument pdf = document.finish()) {
                assertEquals("Größe: 5 °C – ok groß", new PDFTextStripper().getText(pdf).strip());
            }
        }
    }

    @Test
    void strictFaceSharesFontWithoutChangingOriginalFaceOrExistingFonts() throws IOException {
        FontFace original = new FontFace(PDType1Font.HELVETICA);
        Font existing = original.at(10);
        FontFace strict = original.strict();
        assertSame(original.getFont(), strict.getFont());
        assertThrows(PdfUnboxException.class, () -> strict.at(10).width("φ"));

        for (Font font : List.of(existing, original.at(12), original.at(16, Color.BLUE))) {
            assertEquals("a?b", font.encodable("aφb"));
            assertEquals(font.width("a?b"), font.width("aφb"));
            try (Document document = new Document()) {
                document.render(paragraph("aφb", font));
                try (PDDocument pdf = document.finish()) {
                    assertEquals("a?b", new PDFTextStripper().getText(pdf).strip());
                }
            }
        }
    }

}
