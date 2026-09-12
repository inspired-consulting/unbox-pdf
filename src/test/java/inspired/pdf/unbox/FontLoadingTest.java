package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static inspired.pdf.unbox.Unbox.paragraph;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Verifies embedding a TrueType font into a document and rendering text outside the
 * standard encoding with it. The rendering cases use a TrueType font installed on the
 * machine and are skipped when none of the known locations exists.
 */
class FontLoadingTest {

    /** Common locations of a Unicode-capable TrueType font on Linux, macOS, and Windows. */
    private static final List<Path> CANDIDATES = List.of(
            Path.of("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
            Path.of("/usr/share/fonts/dejavu/DejaVuSans.ttf"),
            Path.of("/Library/Fonts/Arial Unicode.ttf"),
            Path.of("/System/Library/Fonts/Supplemental/Arial Unicode.ttf"),
            Path.of("/System/Library/Fonts/Supplemental/Arial.ttf"),
            Path.of("C:/Windows/Fonts/arial.ttf"));

    @Test
    void embeddedFontRendersCharactersOutsideStandardEncoding() throws IOException {
        Path fontFile = systemFont();
        try (Document document = new Document()) {
            FontFace face = document.loadFont(fontFile);
            document.render(paragraph("φ ≥ Ā ok", face.at(10)));

            try (PDDocument pdf = document.finish()) {
                assertEquals("φ ≥ Ā ok", new PDFTextStripper().getText(pdf).strip());
            }
        }
    }

    @Test
    void faceDerivesFontsOfDifferentSizes() throws IOException {
        Path fontFile = systemFont();
        try (Document document = new Document()) {
            FontFace face = document.loadFont(fontFile);
            Font small = face.at(8);
            Font large = face.at(16);
            assertEquals(small.width("Wide") * 2, large.width("Wide"), 0.01f);
            assertEquals(face.getFont(), large.getFont());
        }
    }

    @Test
    void invalidFontDataFailsWithLibraryException() {
        try (Document document = new Document()) {
            assertThrows(PdfUnboxException.class,
                    () -> document.loadFont(new ByteArrayInputStream("not a font".getBytes())));
        }
    }

    @Test
    void loadingIntoClosedDocumentIsRejected() {
        Document document = new Document();
        document.close();
        assertThrows(IllegalStateException.class,
                () -> document.loadFont(new ByteArrayInputStream(new byte[0])));
    }

    private static Path systemFont() {
        Optional<Path> font = CANDIDATES.stream().filter(Files::isRegularFile).findFirst();
        assumeTrue(font.isPresent(), "no TrueType font found in known locations");
        return font.get();
    }
}
