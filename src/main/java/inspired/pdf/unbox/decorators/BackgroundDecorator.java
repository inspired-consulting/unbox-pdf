package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;

/**
 * Add a colored background to an element.
 */
public class BackgroundDecorator extends Decorator {

    private final Color color;

    public BackgroundDecorator(Color color) {
        this.color = color;
    }

    @Override
    public float decorate(Document document, Bounds viewPort) {
        try {
            PDPageContentStream contentStream = document.getContentStream();
            contentStream.setNonStrokingColor(color);
            contentStream.addRect(
                    viewPort.left(),
                    viewPort.bottom(),
                    viewPort.width(),
                    viewPort.height());
            contentStream.fill();
            return 0;
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

}
