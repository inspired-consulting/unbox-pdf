package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.elements.internal.AbstractTable;

/**
 * Table with flexible rows and columns.
 */
public class FlexTable extends AbstractTable {

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
