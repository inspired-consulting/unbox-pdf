package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.base.Column;
import inspired.pdf.unbox.base.SimpleColumnModel;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.PdfElement;

import java.util.List;

import static java.lang.Math.max;

public class HorizontalLayout implements ContainerLayout {

    @Override
    public float render(Document document, Bounds viewPort, Container container, List<PdfElement> elements) {
        Bounds bounds = viewPort.apply(container.margin()).apply(container.padding());
        float forward = 0f;
        SimpleColumnModel columns = SimpleColumnModel.uniform(elements.size()).scaleToSize(bounds.width());
        float offsetX = bounds.left();
        for (int i = 0; i < elements.size(); i++) {
            PdfElement element = elements.get(i);
            Column column = columns.get(i);
            Bounds adjusted = bounds.width(column.width()).left(offsetX);
            forward = max(element.render(document, adjusted), forward);
            offsetX += column.width();
        }
        return forward + container.margin().vertical() + container.padding().vertical();
    }

    @Override
    public float innerHeight(Bounds viewPort, Container container, List<PdfElement> elements) {
        float elementsHeight = elements.stream().map(e -> e.innerHeight(viewPort)).max(Float::compareTo).orElse(0f);
        return elementsHeight + container.padding().vertical();
    }

}
