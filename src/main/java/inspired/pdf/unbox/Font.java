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

    default float width(String text) {
        try {
            return getFont().getStringWidth(text) / 1000 * getSize();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    default float lineHeight() {
        return getFont().getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * getSize();
    }

}
