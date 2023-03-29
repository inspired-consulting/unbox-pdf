package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.elements.PdfElement;
import inspired.pdf.unbox.elements.internal.RenderingHints;

/**
 * A decorator can be applied to a PdfElement and extend its visualization, e.g. draw a background or border.
 */
public abstract class Decorator implements PdfElement, Comparable<Decorator> {

    private PdfElement wrappedElement;
    private final RenderingHints renderingHints = new RenderingHints();

    private final int level;

    public Decorator() {
        this(0);
    }

    public Decorator(int level) {
        this.wrappedElement = null;
        this.level = level;
    }

    public static Decorator pass() {
        return new VoidDecorator();
    }

    public PdfElement wrap(PdfElement element) {
        this.wrappedElement = element;
        return this;
    }

    public abstract float decorate(Document document, Bounds viewPort);

    @Override
    public final float render(Document document, Bounds viewPort) {
        if (wrappedElement != null) {
            float height = wrappedElement.innerHeight(viewPort);
            decorate(document, viewPort.apply(wrappedElement.margin()).height(height));
            return wrappedElement.render(document, viewPort);
        } else {
            return decorate(document, viewPort);
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

    @Override
    public int compareTo(Decorator other) {
        return level - other.level;
    }

    @Override
    public RenderingHints renderingHints() {
        return renderingHints;
    }

    private static class VoidDecorator extends Decorator {

        @Override
        public float decorate(Document document, Bounds viewPort) {
            return 0;
        }

    }
}
