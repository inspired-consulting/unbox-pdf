package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.decorators.Decoratable;
import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.PdfElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for elements that can be decorated. Decorators are applied in ascending level order,
 * with ties kept in insertion order.
 */
public abstract class AbstractDecoratable implements PdfElement, Decoratable {

    private final List<Decorator> decorators = new ArrayList<>();
    private final RenderingHints renderingHints = new RenderingHints();

    @Override
    public PdfElement with(Decorator decorator) {
        this.decorators.add(decorator);
        return this;
    }

    public RenderingHints renderingHints() {
        return renderingHints;
    }

    protected void applyDecorators(Document document, Bounds viewPort) {
        for (Decorator decorator : decorators()) {
            decorator.render(document, viewPort);
        }
    }

    /**
     * Returns a copy of the attached decorators, stably sorted by level. The stored list keeps insertion
     * order, so equal levels always paint in the order they were added.
     */
    protected List<Decorator> decorators() {
        List<Decorator> sorted = new ArrayList<>(decorators);
        Collections.sort(sorted);
        return sorted;
    }

}
