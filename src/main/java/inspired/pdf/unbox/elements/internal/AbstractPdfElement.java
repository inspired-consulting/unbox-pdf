package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.elements.PdfElement;

public abstract class AbstractPdfElement implements PdfElement {

    public static PdfElement empty() {
        return new EmptyElement();
    }

    private Margin margin = Margin.none();
    private Padding padding = Padding.none();

    public AbstractPdfElement with(Margin margin) {
        this.margin = margin;
        return this;
    }

    public AbstractPdfElement with(Padding padding) {
        this.padding = padding;
        return this;
    }

    @Override
    public Margin margin() {
        return margin;
    }

    public Padding padding() {
        return padding;
    }

    private static class EmptyElement extends AbstractPdfElement {
        @Override
        public float render(Document document, Bounds viewPort) {
            return 0;
        }

        @Override
        public float innerHeight(Bounds viewPort) {
            return 0;
        }
    }

}
