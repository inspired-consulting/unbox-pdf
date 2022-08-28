package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.elements.internal.AbstractPdfElement;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.*;
import java.io.IOException;

/**
 * A canvas gives direct access to the PDPageContentStream and allows painting arbitrarily items.
 */
public abstract class Canvas extends AbstractPdfElement {

    private final float innerHeight;

    public Canvas(float innerhHeight) {
        this.innerHeight = innerhHeight;
    }

    public abstract void paint(PDPageContentStream contentStream, Bounds viewPort) throws IOException;

    protected void drawCircle(PDPageContentStream contentStream, float cx, float cy, float r, Color color) throws IOException {
        final float k = 0.552284749831f;
        contentStream.setNonStrokingColor(color);
        contentStream.moveTo(cx - r, cy);
        contentStream.curveTo(cx - r, cy + k * r, cx - k * r, cy + r, cx, cy + r);
        contentStream.curveTo(cx + k * r, cy + r, cx + r, cy + k * r, cx + r, cy);
        contentStream.curveTo(cx + r, cy - k * r, cx + k * r, cy - r, cx, cy - r);
        contentStream.curveTo(cx - k * r, cy - r, cx - r, cy - k * r, cx - r, cy);
        contentStream.fill();
    }

    @Override
    public float render(LinearPDFWriter writer, Bounds viewPort) {
        try {
            paint(writer.getContentStream(), viewPort.height(innerHeight));
            return innerHeight + margin().vertical();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return innerHeight;
    }

}
