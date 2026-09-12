package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.SimpleFont;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.Color;

/**
 * A font embedded into one document, from which fonts of any size and color are derived.
 * Obtain it from {@link Document#loadFont(java.io.InputStream)}; it is only valid for that document.
 */
public final class FontFace {

    private final PDFont font;

    FontFace(PDFont font) {
        this.font = font;
    }

    public Font at(float size) {
        return at(size, Color.BLACK);
    }

    public Font at(float size, Color color) {
        return new SimpleFont(font, size, color);
    }

    public PDFont getFont() {
        return font;
    }

    @Override
    public String toString() {
        return "FontFace[" + font.getName() + "]";
    }

}
