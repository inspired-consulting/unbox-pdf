package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.font.PDFont;

import java.awt.*;
import java.io.IOException;

/**
 * Interface providing information about a PDF font.
 */
public interface Font {

    PDFont getFont();

    float getSize();

    Color getColor();

    /**
     * Prepare text for this font: characters the font cannot encode are replaced or
     * rejected, depending on the implementation. Measurement and drawing both use it,
     * so they always agree. The default keeps the text unchanged.
     */
    default String encodable(String text) {
        return text;
    }

    default float width(String text) {
        try {
            return getFont().getStringWidth(encodable(text)) / 1000 * getSize();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    default float lineHeight() {
        return getFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * getSize();
    }

    /**
     * Height of capital letters above the baseline, scaled to the font size.
     * Use it to center drawn shapes on the visible letters of a text line.
     */
    default float capHeight() {
        return getFont().getFontDescriptor().getCapHeight() / 1000 * getSize();
    }

}
