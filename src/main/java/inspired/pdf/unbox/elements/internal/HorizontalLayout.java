package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.base.Column;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.base.SimpleColumnModel;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.PdfElement;

import java.util.List;

import static java.lang.Math.max;

/**
 * Layout for elements in a horizontal row, i.e. in columns
 */
public class HorizontalLayout implements ContainerLayout {

    protected final ColumnModel<?> columnModel;

    public HorizontalLayout() {
        columnModel = null;
    }

    public HorizontalLayout(ColumnModel<?> columnModel) {
        this.columnModel = columnModel;
    }

    @Override
    public float render(Document document, Bounds viewPort, Container container, List<PdfElement> elements) {
        var bounds = viewPort.apply(container.margin()).apply(container.padding());
        float forward = 0f;
        var columns = columnModel(elements.size(), bounds);
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
        var bounds = viewPort.apply(container.padding());
        var columns = columnModel(elements.size(), bounds);
        float maxHeight = 0f;
        for (int i = 0; i < elements.size(); i++) {
            PdfElement element = elements.get(i);
            Column column = columns.get(i);
            Bounds adjusted = bounds.width(column.width());
            maxHeight = max(element.innerHeight(adjusted), maxHeight);
        }
        return maxHeight + container.padding().vertical();
    }

    protected ColumnModel<?> columnModel(int columns, Bounds bounds) {
        if (this.columnModel == null) {
            return SimpleColumnModel.uniform(columns).scaleToSize(bounds.width());
        } else if (this.columnModel.size() < columns) {
            throw new IllegalStateException("More elements than columns defined: " + columns);
        } else {
            return this.columnModel.scaleToSize(bounds.width());
        }
    }

    @Override
    public Container.Layout layoutType() {
        return Container.Layout.COLUMNS;
    }

}
