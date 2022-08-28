package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;

public class BackgroundDecorator extends Decorator {

    private final Color color;

    public static final BackgroundDecorator background(Color color) {
        return new BackgroundDecorator(color);
    }

    public BackgroundDecorator(Color color) {
        this.color = color;
    }

    @Override
    public float decorate(LinearPDFWriter writer, Bounds viewPort) {
        try {
            PDPageContentStream contentStream = writer.getContentStream();
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
