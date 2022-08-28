package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.base.Column;
import inspired.pdf.unbox.base.SimpleColumnModel;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.max;

public class Row implements PdfElement {

    private final List<PdfElement> elements = new ArrayList<>();
    private final SimpleColumnModel columnModel = new SimpleColumnModel();

    private Margin margin = Margin.none();

    public Row add(PdfElement element) {
        elements.add(element);
        columnModel.add(1f);
        return this;
    }

    public Row with(Margin margin) {
        this.margin = margin;
        return this;
    }

    @Override
    public Margin margin() {
        return margin;
    }

    @Override
    public float render(LinearPDFWriter writer, Bounds viewPort) {
        Bounds bounds = viewPort.apply(margin);
        float forward = 0f;
        SimpleColumnModel columns = columnModel.scaleToSize(bounds.width());
        float offsetX = bounds.left();
        for (int i = 0; i < elements.size(); i++) {
            PdfElement element = elements.get(i);
            Column column = columns.get(i);
            Bounds adjusted = bounds.width(column.width()).left(offsetX);
            forward = max(element.render(writer, adjusted), forward);
            offsetX += column.width();
        }
        return forward + margin.vertical();
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return elements.stream().map(e -> e.innerHeight(viewPort)).max(Float::compareTo).orElse(0f);
    }
}
