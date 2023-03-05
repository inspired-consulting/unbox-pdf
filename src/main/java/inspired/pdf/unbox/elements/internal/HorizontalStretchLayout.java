package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.base.Column;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.PdfElement;

import java.util.List;

import static java.lang.Math.max;

public class HorizontalStretchLayout extends HorizontalLayout {

    public HorizontalStretchLayout() {
        this(null);
    }

    public HorizontalStretchLayout(ColumnModel columnModel) {
        super(columnModel);
    }

    @Override
    public float render(Document document, Bounds viewPort, Container container, List<PdfElement> elements) {
        var bounds = viewPort.apply(container.margin()).apply(container.padding());
        float forward = 0f;
        var columns = columnModel(elements.size(), bounds);
        float offsetX = bounds.left();
        float maxInnerHeight = 0f;
        for (int i = 0; i < elements.size(); i++) {
            PdfElement element = elements.get(i);
            Column column = columns.get(i);
            Bounds adjusted = bounds.width(column.width());
            maxInnerHeight = max(maxInnerHeight, element.innerHeight(adjusted));
        }
        for (int i = 0; i < elements.size(); i++) {
            PdfElement element = elements.get(i);
            Column column = columns.get(i);
            Bounds adjusted = bounds.width(column.width()).left(offsetX);
            element.renderingHints().addExtraPadding(Padding.bottom(maxInnerHeight - element.innerHeight(adjusted)));
            forward = max(element.render(document, adjusted), forward);
            offsetX += column.width();
        }
        return forward + container.margin().vertical() + container.padding().vertical();
    }

    @Override
    public boolean applyRenderingHintsToContainer() {
        return false;
    }
}
