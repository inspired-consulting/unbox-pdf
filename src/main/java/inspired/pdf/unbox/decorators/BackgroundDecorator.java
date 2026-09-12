package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;

/**
 * Add a colored background to an element, optionally with rounded corners.
 */
public class BackgroundDecorator extends Decorator {

    public static int LEVEL = 100;

    private final Color color;
    private final float radius;

    public BackgroundDecorator(Color color) {
        this(color, 0);
    }

    /**
     * @param color  The fill color
     * @param radius The corner radius; zero fills a plain rectangle. Use the same value as
     *               the border radius so that fill and outline share one shape.
     */
    public BackgroundDecorator(Color color, float radius) {
        super(LEVEL);
        if (radius < 0 || Float.isNaN(radius)) {
            throw new IllegalArgumentException("Background radius must not be negative, but was " + radius);
        }
        this.color = color;
        this.radius = radius;
    }

    @Override
    public float decorate(Document document, Bounds viewPort) {
        try {
            PDPageContentStream contentStream = document.getContentStream();
            contentStream.setNonStrokingColor(color);
            if (radius > 0) {
                RoundedRectangle.trace(contentStream, viewPort, radius);
            } else {
                contentStream.addRect(
                        viewPort.left(),
                        viewPort.bottom(),
                        viewPort.width(),
                        viewPort.height());
            }
            contentStream.fill();
            return 0;
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

}
