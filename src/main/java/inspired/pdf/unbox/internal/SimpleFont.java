package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.awt.*;

/**
 * Simple immutable font info.
 */
public class SimpleFont implements Font {

    private final PDFont font;
    private final float size;
    private final Color color;

    public static SimpleFont helvetica(float size) {
        return new SimpleFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), size);
    }

    public static Font helvetica_bold(float size) {
        return new SimpleFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), size);
    }

    public static Font helvetica_bold(float size, Color color) {
        return new SimpleFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), size, color);
    }

    public SimpleFont(PDFont font, float size) {
        this(font, size, Color.BLACK);
    }

    public SimpleFont(PDFont font, float size, Color color) {
        this.font = font;
        this.size = size;
        this.color = color;
    }



    @Override
    public PDFont getFont() {
        return font;
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public Color getColor() {
        return color;
    }

}
