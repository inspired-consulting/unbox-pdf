package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
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
        ColumnModel<?> columns = row.columnModel().scaleToSize(document.getViewPort().width());
        drawColumnLines(document, columns, rowHeight);
        return rowHeight;
    }

    private void drawColumnLines(Document document, ColumnModel<?> columns, float height) {
        Bounds viewPort = document.getViewPort();
        Bounds bounds = new Bounds(viewPort.left(), document.getPosition(), viewPort.width(), height);
        drawColumnLines(document, columns, bounds);
    }

}
