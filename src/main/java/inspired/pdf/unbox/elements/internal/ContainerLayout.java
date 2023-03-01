package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.Container.Layout;
import inspired.pdf.unbox.elements.PdfElement;

import java.util.List;

/**
 * Layout for a Container.
 */
public interface ContainerLayout {

    /**
     * Render the container and it's elements.
     * @param document The document.
     * @param viewPort The view port to render the container in.
     * @param container The container.
     * @param elements The elements of the container.
     * @return The height of the rendered elements.
     */
    float render(Document document, Bounds viewPort, Container container, List<PdfElement> elements);

    float innerHeight(Bounds viewPort, Container container, List<PdfElement> elements);

    /**
     * Get the layout type.
     * @return The layout type.
     */
    Layout layoutType();

    default boolean applyRenderingHintsToContainer() { return true; }
}
