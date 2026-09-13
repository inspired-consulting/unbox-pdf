package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfUnboxException;
import inspired.pdf.unbox.internal.SimpleFont;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.Color;

/**
 * A font embedded into one document, from which fonts of any size and color are derived.
 * Obtain it from {@link Document#loadFont(java.io.InputStream)}; it is only valid for that document.
 * Unsupported characters are replaced with a question mark by default. Use {@link #strict()}
 * to derive fonts that reject unsupported text instead.
 */
public final class FontFace {

    private final PDFont font;
    private final boolean strict;

    FontFace(PDFont font) {
        this(font, false);
    }

    private FontFace(PDFont font, boolean strict) {
        this.font = font;
        this.strict = strict;
    }

    /**
     * Create a face whose derived fonts fail with a {@link PdfUnboxException} when
     * measuring or drawing a character the font cannot encode. The exception names
     * the unsupported code point and font.
     * This face and any fonts already derived from it remain unchanged.
     *
     * @return A strict face sharing the same embedded font and document ownership.
     */
    public FontFace strict() {
        return new FontFace(font, true);
    }

    public Font at(float size) {
        return at(size, Color.BLACK);
    }

    public Font at(float size, Color color) {
        return new SimpleFont(font, size, color, strict ? null : SimpleFont.DEFAULT_REPLACEMENT);
    }

    public PDFont getFont() {
        return font;
    }

    @Override
    public String toString() {
        return "FontFace[" + font.getName() + "]";
    }

}
