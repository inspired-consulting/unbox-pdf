package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Border;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Position;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;

/**
 * Draws a border around elements.
 * The border size will not change the elements size.
 */
public class BorderDecorator extends Decorator {

    private final Border border;
    private final Color color;

    public static BorderDecorator border(Border border, Color color) {
        return new BorderDecorator(border,color);
    }

    public static BorderDecorator border(float size, Color color) {
        return new BorderDecorator(Border.of(size), color);
    }

    public BorderDecorator(Border border) {
        this(border, Color.BLACK);
    }

    public BorderDecorator(Border border, Color color) {
        this.border = border;
        this.color = color;
    }

    @Override
    public float decorate(Document document, Bounds viewPort) {
        try {
            PDPageContentStream contentStream = document.getContentStream();
            contentStream.setStrokingColor(color);
            if (border.isUniform()) {
                contentStream.setLineWidth(border.top());
                renderRect(viewPort, contentStream);
            } else {
                renderLine(viewPort.topLeft(), viewPort.topRight(), contentStream, border.top());
                renderLine(viewPort.topLeft(), viewPort.bottomLeft(), contentStream, border.left());
                renderLine(viewPort.bottomLeft(), viewPort.bottomRight(), contentStream, border.bottom());
                renderLine(viewPort.bottomRight(), viewPort.topRight(), contentStream, border.right());
            }
            return 0;
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private void renderLine(Position start, Position end, PDPageContentStream contentStream, float lineWidth) throws IOException {
        if (lineWidth > 0) {
            contentStream.setLineWidth(lineWidth);
            contentStream.moveTo(start.x(), start.y());
            contentStream.lineTo(end.x(), end.y());
            contentStream.stroke();
        }
    }

    private void renderRect(Bounds viewPort, PDPageContentStream contentStream) throws IOException {
        contentStream.addRect(
                viewPort.left(),
                viewPort.bottom(),
                viewPort.width(),
                viewPort.height());
        contentStream.stroke();
    }

}
