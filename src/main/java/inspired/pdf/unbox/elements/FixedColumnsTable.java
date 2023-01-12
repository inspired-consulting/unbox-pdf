package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Font;
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

    public Table withHeader() {
        addHeader(TableRow.header(model));
        return this;
    }

    public Table withHeader(Font font) {
        addHeader(TableRow.header(model, font));
        return this;
    }

    public TableModel getModel() {
        return model;
    }

    public TableRow addRow() {
        return addRow(new TableRow(model));
    }

    @Override
    protected float renderRow(Document document, TableRow row) {
        float rowHeight = super.renderRow(document, row);
        var bounds = effectiveViewport(document).height(rowHeight);
        ColumnModel<?> columns = model.scaleToSize(bounds.width());
        drawColumnLines(document, columns, bounds);
        return rowHeight;
    }

}
