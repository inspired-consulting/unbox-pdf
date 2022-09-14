package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.elements.internal.AbstractPdfElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for PDF multiple elements.
 */
public abstract class Container extends AbstractPdfElement {

    protected final List<PdfElement> elements = new ArrayList<>();

    public Container add(PdfElement element) {
        elements.add(element);
        return this;
    }

}
