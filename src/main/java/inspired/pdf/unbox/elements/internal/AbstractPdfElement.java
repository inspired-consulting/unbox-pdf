package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.elements.PdfElement;

public abstract class AbstractPdfElement implements PdfElement {

    private Margin margin = Margin.none();
    private Padding padding = Padding.none();

    public PdfElement with(Margin margin) {
        this.margin = margin;
        return this;
    }

    public PdfElement with(Padding padding) {
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

}
