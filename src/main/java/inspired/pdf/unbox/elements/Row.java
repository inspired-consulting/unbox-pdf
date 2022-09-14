package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.base.Column;
import inspired.pdf.unbox.base.SimpleColumnModel;
import inspired.pdf.unbox.elements.internal.AbstractPdfElement;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class Row extends AbstractPdfElement {

    private final List<PdfElement> elements = new ArrayList<>();
    private final SimpleColumnModel columnModel = new SimpleColumnModel();

    public Row add(PdfElement element) {
        elements.add(element);
        columnModel.add(1f);
        return this;
    }

    public Row with(Margin margin) {
        super.with(margin);
        return this;
    }

    public Row with(Padding padding) {
        super.with(padding);
        return this;
    }

    @Override
    public float render(Document document, Bounds viewPort) {
        Bounds bounds = viewPort.apply(margin()).apply(padding());
        float forward = 0f;
        SimpleColumnModel columns = columnModel.scaleToSize(bounds.width());
        float offsetX = bounds.left();
        for (int i = 0; i < elements.size(); i++) {
            PdfElement element = elements.get(i);
            Column column = columns.get(i);
            Bounds adjusted = bounds.width(column.width()).left(offsetX);
            forward = max(element.render(document, adjusted), forward);
            offsetX += column.width();
        }
        return forward + margin().vertical() + padding().vertical();
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return elements.stream().map(e -> e.innerHeight(viewPort)).max(Float::compareTo).orElse(0f) + padding().vertical();
    }

}
