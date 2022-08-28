package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.decorators.Decorator;

public interface PdfElement {

    float DONT_FORWARD = 0f;

    /**
     * Render a PDF element inside given view port bounds.
     * @param writer The current writer.
     * @param viewPort The viewport.
     * @return the distance, the writer shall be forwarded. Return 0 if the element handles forwarding itself.
     */
    float render(LinearPDFWriter writer, Bounds viewPort);

    /**
     * Calculate the inner height of the element regarding the given view port, i.e. without margin.
     * @param viewPort The view port.
     * @return The height.
     */
    float innerHeight(Bounds viewPort);

    default Margin margin() {
        return Margin.none();
    }

    /**
     * Add a decorator to this element.
     * @param decorator The decorator.
     * @return The element to continue with, either the wrapped element or the decorator.
     */
    default PdfElement with(Decorator decorator) {
        return decorator.wrap(this);
    }
}
