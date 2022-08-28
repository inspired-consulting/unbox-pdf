package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.elements.PdfElement;

public abstract class Decorator implements PdfElement {

    private PdfElement wrappedElement;

    public Decorator() {
        this.wrappedElement = null;
    }

    public Decorator wrap(PdfElement element) {
        this.wrappedElement = element;
        return this;
    }

    public abstract float decorate(LinearPDFWriter writer, Bounds viewPort);

    @Override
    public final float render(LinearPDFWriter writer, Bounds viewPort) {
        if (wrappedElement != null) {
            float height = wrappedElement.innerHeight(viewPort);
            decorate(writer, viewPort.apply(wrappedElement.margin()).height(height));
            return wrappedElement.render(writer, viewPort);
        } else {
            return decorate(writer, viewPort);
        }
    }

    @Override
    public Margin margin() {
        if (wrappedElement != null) {
            return wrappedElement.margin();
        } else {
            return Margin.none();
        }
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        if (wrappedElement != null) {
            return wrappedElement.innerHeight(viewPort);
        } else {
            return 0f;
        }
    }
}
