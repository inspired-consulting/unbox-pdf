package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.elements.internal.AbstractTable;

/**
 * Table with flexible rows and columns.
 */
public class FlexTable extends AbstractTable {

    @Override
    protected float renderRow(LinearPDFWriter writer, TableRow row) {
        float rowHeight = super.renderRow(writer, row);
        ColumnModel<?> columns = row.columnModel().scaleToSize(writer.getViewPort().width());
        drawColumnLines(writer, columns, rowHeight);
        return rowHeight;
    }

    private void drawColumnLines(LinearPDFWriter writer, ColumnModel<?> columns, float height) {
        Bounds viewPort = writer.getViewPort();
        Bounds bounds = new Bounds(viewPort.left(), writer.getPosition(), viewPort.width(), height);
        drawColumnLines(writer, columns, bounds);
    }

}
