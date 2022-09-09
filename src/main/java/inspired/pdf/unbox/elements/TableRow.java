package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.base.TableModel.TableColumn;
import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.internal.SimpleFont;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Represents a row in a table consisting of cells.
 */
public class TableRow implements PdfElement {

    private final List<Object> content = new ArrayList<>();
    private final List<TableCell> cells = new ArrayList<>();
    private final List<Decorator> decorators = new ArrayList<>();
    private final TableModel model;
    private final Font font;

    public static TableRow header(TableModel columns) {
        TableRow row = new TableRow(columns);
        for (TableColumn column : columns) {
            row.addCell(column.title(), column.align());
        }
        return row;
    }

    public static TableRow header(TableModel columns, Font font) {
        TableRow row = new TableRow(columns, font);
        for (TableColumn column : columns) {
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

    public TableRow(TableModel columns, Font font) {
        this.model = columns;
        this.font = font;
    }

    public int size() {
        return cells.size();
    }

    public ColumnModel<?> columnModel() {
        return model;
    }

    public TableCell getCell(int i) {
        return cells.get(i);
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

    @Override
    public TableRow with(Decorator decorator) {
        this.decorators.add(decorator);
        return this;
    }

    @Override
    public float render(LinearPDFWriter writer, Bounds bounds) {
        for (Decorator decorator : decorators) {
            decorator.render(writer, bounds);
        }
        float x = bounds.left();
        ColumnModel<?> columns = this.model.scaleToSize(bounds.width());
        float maxHeight = 0f;
        for (int i = 0; i < size(); i++) {
            TableCell cell = getCell(i);
            float width = columns.width(i);
            Bounds cellBounds = bounds.left(x).width(width);
            float height = cell.render(writer, cellBounds);
            maxHeight = Math.max(maxHeight, height);
            x+= width;
        }
        return maxHeight;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        ColumnModel<?> columns = this.model.scaleToSize(viewPort.width());
        float max = 0f;
        for (int i = 0; i < cells.size(); i++) {
            TableCell cell = cells.get(i);
            float width = columns.width(i);
            max = Math.max(max, cell.innerHeight(viewPort.width(width)));
        }
        return max;
    }

    private Align coalesce(Align... options) {
        return Stream.of(options).filter(Objects::nonNull).findFirst().orElse(Align.LEFT);
    }

    private Font coalesce(Font... options) {
        return Stream.of(options).filter(Objects::nonNull).findFirst().orElse(SimpleFont.helvetica(8));
    }

}
