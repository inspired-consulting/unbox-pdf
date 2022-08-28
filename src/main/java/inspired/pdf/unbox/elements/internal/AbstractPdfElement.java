package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.elements.PdfElement;

public abstract class AbstractPdfElement implements PdfElement {

    private Margin margin = Margin.none();

    public PdfElement with(Margin margin) {
        this.margin = margin;
        return this;
    }

    @Override
    public Margin margin() {
        return margin;
    }

}
