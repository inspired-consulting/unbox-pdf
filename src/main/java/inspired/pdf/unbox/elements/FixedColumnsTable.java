package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.internal.AbstractTable;

/**
 * Table with flexible rows and columns.
 */
public class FixedColumnsTable extends AbstractTable {

    private final TableModel model;

    public FixedColumnsTable(TableModel model) {
        this.model = model;
    }

    public FixedColumnsTable withHeaderFromModel() {
        addRow(TableRow.header(model));
        return this;
    }

    public TableRow addRow() {
        return addRow(new TableRow(model));
    }

    @Override
    protected float renderRow(Document document, TableRow row) {
        float rowHeight = super.renderRow(document, row);
        ColumnModel<?> columns = model.scaleToSize(document.getViewPort().width());
        drawColumnLines(document, columns, rowHeight);
        return rowHeight;
    }

    private void drawColumnLines(Document document, ColumnModel<?> columns, float height) {
        Bounds viewPort = document.getViewPort();
        Bounds bounds = new Bounds(viewPort.left(), document.getPosition(), viewPort.width(), height);
        drawColumnLines(document, columns, bounds);
    }

}
