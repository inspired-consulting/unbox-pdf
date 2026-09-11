package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Font;
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
    protected float renderRow(Document document, TableRow row) {
        float rowHeight = super.renderRow(document, row);
        var bounds = effectiveViewport(document).height(rowHeight);
        ColumnModel<?> columns = row.columnModel().scaleToSize(bounds.width());
        drawColumnLines(document, columns, bounds);
        return rowHeight;
    }

}
