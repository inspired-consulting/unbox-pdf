package inspired.pdf.unbox.elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.base.TableModel.TableColumn;
import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.internal.AbstractDecoratable;
import inspired.pdf.unbox.internal.SimpleFont;

/**
 * Represents a row in a table consisting of cells.
 */
public class TableRow extends AbstractDecoratable implements PdfElement {

    private final List<Object> values = new ArrayList<>();
    private final List<TableCell> cells = new ArrayList<>();
    private final TableModel model;
    private final Font font;

    public static TableRow header(TableModel model) {
        TableRow row = new TableRow(model);
        for (TableColumn column : model) {
            row.addCell(column.title(), column.align());
        }
        return row;
    }

    public static TableRow header(TableModel model, Font font) {
        TableRow row = new TableRow(model, font);
        for (TableColumn column : model) {
            row.addCell(column.title(), column.align());
        }
        return row;
    }

    public TableRow() {
        this(new TableModel());
    }

    public TableRow(TableModel columns) {
        this(columns, null);
    }

    public TableRow(TableModel model, Font font) {
        this.model = model;
        this.font = font;
    }

    public int size() {
        return Math.max(cells.size(), values.size());
    }

    public ColumnModel<?> columnModel() {
        return model;
    }

    public TableRow addCell(String text) {
        return addCell(text, null);
    }

    public TableRow addCell(String text, Align align) {
        return addCell(text, align, null);
    }

    public TableRow addCell(String text, Align align, Font font) {
        if (model.size() >= cells.size()) {
            TableColumn column = model.get(cells.size());
            Align effectiveAlign = coalesce(align, column.align());
            Font effectiveFont = coalesce(font, column.font(), this.font);
            return addCell(new TextCell(text, effectiveAlign, effectiveFont));
        } else {
            Align effectiveAlign = coalesce(align);
            Font effectiveFont = coalesce(font, this.font);
            return addCell(new TextCell(text, effectiveAlign, effectiveFont));
        }
    }

    public <T extends TableCell> TableRow addCell(T cell) {
        cells.add(cell);
        if (model.size() < cells.size()) {
            model.add(TableModel.DEFAULT_COLUMN);
        }
        return this;
    }

    public TableRow withCells(String... contents) {
        for (String text : contents) {
            addCell(text);
        }
        return this;
    }

    public TableRow withValues(Object... values) {
        this.values.clear();
        this.values.addAll(Arrays.asList(values));
        return this;
    }

    @Override
    public TableRow with(Decorator decorator) {
        super.with(decorator);
        return this;
    }

    @Override
    public float render(Document document, Bounds bounds) {
        applyDecorators(document, bounds);
        float x = bounds.left();
        ColumnModel<?> columns = this.model.scaleToSize(bounds.width());
        float maxHeight = 0f;
        for (int i = 0; i < size(); i++) {
            TableCell cell = prepareCell(i);
            float width = columns.width(i);
            Bounds cellBounds = bounds.left(x).width(width);
            float height = cell.render(document, cellBounds);
            maxHeight = Math.max(maxHeight, height);
            x+= width;
        }
        return maxHeight;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        ColumnModel<?> columns = this.model.scaleToSize(viewPort.width());
        float max = 0f;
        for (int i = 0; i < size(); i++) {
            TableCell cell = prepareCell(i);
            float width = columns.width(i);
            max = Math.max(max, cell.innerHeight(viewPort.width(width)));
        }
        return max;
    }

    // -- internal

    private TableCell prepareCell(int i) {
        if (i >= 0 && i < cells.size() && cells.get(i) != null) {
            // The row contains a cell for this index, use it directly
            return cells.get(i);
        }

        // If the row doesn't contain a cell, get it from the model
        // by its column
        TableCell cell = this.model.get(i).cell();

        Object value = getValue(i);
        // otherwise fall back to a default cell
        if (cell == null) {
            cell = this.model.getDefaultCellFor(value);
        }

        cell.setValue(value);
        return cell;
    }

    private Object getValue(int index) {
        if(index >= 0 && index < this.values.size()) {
            return this.values.get(index);
        }
        return null;
    }

    private Align coalesce(Align... options) {
        return Stream.of(options).filter(Objects::nonNull).findFirst().orElse(Align.LEFT);
    }

    private Font coalesce(Font... options) {
        return Stream.of(options).filter(Objects::nonNull).findFirst().orElse(SimpleFont.helvetica(8));
    }

}
