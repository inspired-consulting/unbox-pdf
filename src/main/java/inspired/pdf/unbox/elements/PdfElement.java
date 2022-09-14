package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.decorators.Decoratable;
import inspired.pdf.unbox.decorators.Decorator;

/**
 * Basic element for Unbox PDF documents.
 */
public interface PdfElement extends Decoratable {

    float DONT_FORWARD = 0f;

    /**
     * Render a PDF element inside given view port bounds.
     * @param document The current document.
     * @param viewPort The viewport.
     * @return the distance, the position in document shall be forwarded. Return 0 if the element handles forwarding itself.
     */
    float render(Document document, Bounds viewPort);

    /**
     * Calculate the inner height of the element regarding the given view port, i.e. height without margin.
     * @param viewPort The view port.
     * @return The height.
     */
    float innerHeight(Bounds viewPort);

    /**
     * Calculate the outer height of the element regarding the given view port, i.e. height with margin.
     * @param viewPort The view port.
     * @return The height.
     */
    default float outerHeight(Bounds viewPort) {
        return innerHeight(viewPort) + margin().vertical();
    }

    /**
     * Get the element's margin.
     * @return The margin.
     */
    default Margin margin() {
        return Margin.none();
    }

    /**
     * Add a decorator to this element.
     * @param decorator The decorator.
     * @return The element to continue with, either the wrapped element or the decorator.
     */
    @Override
    default PdfElement with(Decorator decorator) {
        return decorator.wrap(this);
    }
}
