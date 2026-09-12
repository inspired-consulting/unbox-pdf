package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.FixedColumnsTable;
import inspired.pdf.unbox.elements.PdfElement;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static inspired.pdf.unbox.Unbox.paragraph;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that characters outside a font's encoding are replaced instead of aborting
 * generation, that measurement matches the replaced text, and that the opt-out fails
 * with a clear message.
 */
class FontReplacementTest {

    @Test
    void supportedNonAsciiCharactersRenderUnchanged() throws IOException {
        assertEquals("Größe: 5 °C – ok", render(paragraph("Größe: 5 °C – ok")));
    }

    @Test
    void unsupportedCharactersAreReplacedByDefault() throws IOException {
        assertEquals("phi ? and ? and ? end", render(paragraph("phi φ and ≥ and 😀 end")));
    }

    @Test
    void replacementAppliesToTableCells() throws IOException {
        FixedColumnsTable table = new FixedColumnsTable(TableModel.of(1f, 1f));
        table.addRow("Ā", "中文");
        assertEquals("? ??", render(table));
    }

    @Test
    void customReplacementCharacterIsUsed() throws IOException {
        SimpleFont font = SimpleFont.helvetica(8).withReplacement('#');
        assertEquals("a#b", render(paragraph("aφb", font)));
    }

    @Test
    void measurementMatchesReplacedText() {
        SimpleFont font = SimpleFont.helvetica(10);
        assertEquals(font.width("?"), font.width("φ"), 0.0001f);
        assertEquals("a?b", font.encodable("aφb"));
    }

    @Test
    void optOutFailsWithClearMessageBeforeDrawing() {
        SimpleFont strict = SimpleFont.helvetica(8).withReplacement(null);
        try (Document document = new Document()) {
            PdfUnboxException failure = assertThrows(PdfUnboxException.class,
                    () -> document.render(paragraph("aφb", strict)));
            assertTrue(failure.getMessage().contains("U+03C6"), failure.getMessage());
            assertTrue(failure.getMessage().contains("Helvetica"), failure.getMessage());
        }
    }

    private static String render(PdfElement element) throws IOException {
        try (Document document = new Document()) {
            document.render(element);
            try (PDDocument pdf = document.finish()) {
                return new PDFTextStripper().getText(pdf).strip();
            }
        }
    }
}
