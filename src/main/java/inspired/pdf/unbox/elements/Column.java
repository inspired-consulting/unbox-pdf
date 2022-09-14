package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.base.SimpleColumnModel;
import inspired.pdf.unbox.elements.internal.AbstractPdfElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.Math.max;

/**
 * Container for elements with a vertical layout, i.e. the elements are rendered one below the other.
 */
public class Column extends Container {

    @Override
    public float render(Document document, Bounds viewPort) {
        Bounds bounds = viewPort.apply(margin()).apply(padding());
        float forward = 0f;
        for (int i = 0; i < elements.size(); i++) {
            PdfElement element = elements.get(i);
            forward += element.render(document, bounds);
        }
        return forward + margin().vertical() + padding().vertical();
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return elements.stream()
                .map(e -> e.innerHeight(viewPort))
                .reduce(Float::sum).orElse(0f);
    }

}
