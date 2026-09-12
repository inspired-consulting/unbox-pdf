package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Stroke;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.internal.AbstractTable;

/**
 * A more flexible table, where each row can have its own model.
 */
public class FlexTable extends AbstractTable {

    public FlexTable withHeader(TableModel model, Font font, Decorator... decorators) {
        addHeader(TableRow.header(model, font, decorators));
        return this;
    }

    @Override
    public FlexTable with(Decorator decorator) {
        super.with(decorator);
        return this;
    }

    @Override
    public FlexTable with(Margin margin) {
        super.with(margin);
        return this;
    }

    @Override
    public FlexTable with(Stroke stroke) {
        super.with(stroke);
        return this;
    }

    @Override
    public FlexTable withRowStroke(Stroke stroke) {
        super.withRowStroke(stroke);
        return this;
    }

    @Override
    public FlexTable withColumnStroke(Stroke stroke) {
        super.withColumnStroke(stroke);
        return this;
    }

    @Override
    protected float renderRow(Document document, TableRow row) {
        float rowHeight = super.renderRow(document, row);
        var bounds = effectiveViewport(document).height(rowHeight);
        ColumnModel<?> columns = row.columnModel().scaleToSize(bounds.width());
        drawColumnLines(document, columns, bounds);
        return rowHeight;
    }

}
