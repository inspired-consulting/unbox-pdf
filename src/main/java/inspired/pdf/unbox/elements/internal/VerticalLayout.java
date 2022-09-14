package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.PdfElement;

import java.util.List;

public class VerticalLayout implements ContainerLayout {

    @Override
    public float render(Document document, Bounds viewPort, Container container, List<PdfElement> elements) {
        Bounds bounds = viewPort.apply(container.margin()).apply(container.padding());
        float forward = 0f;
        for (PdfElement element : elements) {
            float elementHeight = element.render(document, bounds);
            bounds = bounds.moveDown(elementHeight);
            forward += elementHeight;
        }
        return forward + container.margin().vertical() + container.padding().vertical();
    }

    @Override
    public float innerHeight(Bounds viewPort, Container container, List<PdfElement> elements) {
        float elementsHeight = elements.stream()
                .map(e -> e.outerHeight(viewPort))
                .reduce(Float::sum).orElse(0f);
        return elementsHeight + container.padding().vertical();
    }

}
