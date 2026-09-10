package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Bounds;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
