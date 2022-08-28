package inspired.pdf.unbox.base;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.internal.SimpleFont;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents the columns of a layout.
 */
public class TableModel implements ColumnModel<TableModel.TableColumn> {

    public static final float DEFAULT_WIDTH = 1f;
    public static TableColumn DEFAULT_COLUMN = new TableColumn("", DEFAULT_WIDTH, Align.LEFT, null);

    private final List<TableColumn> columns = new ArrayList<>();

    public TableModel() {
    }

    public TableModel(List<TableColumn> columns) {
        this.columns.addAll(columns);
    }

    public static TableModel of(TableColumn ... columns) {
        return new TableModel(List.of(columns));
    }

    public TableModel add(TableColumn column) {
        this.columns.add(column);
        return this;
    }

    public TableModel add(TableColumn... column) {
        this.columns.addAll(List.of(column));
        return this;
    }

    public TableModel add(String title) {
        return add(new TableColumn(title));
    }

    public TableModel add(String title, float width) {
        return add(new TableColumn(title, width, Align.LEFT, null));
    }

    public TableModel add(String title, Align align) {
        return add(new TableColumn(title, DEFAULT_WIDTH, align, null));
    }

    public TableModel add(String title, float width, Align align) {
        return add(new TableColumn(title, width, align, null));
    }

    public TableModel append(TableModel other) {
        this.columns.addAll(other.columns);
        return this;
    }

    public TableColumn get(int index) {
        return columns.get(index);
    }

    public Align align(int index) {
        if (index >= columns.size()) {
            return Align.LEFT;
        }
        return get(index).align;
    }

    public float getOverallWidth() {
        return columns.stream().map(TableColumn::width).reduce(Float::sum).orElse(0f);
    }

    public TableModel scale(float scale) {
        List<TableColumn> adapted = columns.stream().map(c -> c.scale(scale)).collect(Collectors.toList());
        return new TableModel(adapted);
    }

    public TableModel scaleToSize(float tableWidth) {
        return scale(tableWidth / getOverallWidth());
    }

    public List<Bounds> toBounds(Bounds viewPort) {
        List<Bounds> bounds = new ArrayList<>();
        float offset = viewPort.left();
        for (TableColumn colum : columns) {
            bounds.add(new Bounds(offset, viewPort.top(), colum.width, viewPort.height()));
            offset += colum.width;
        }
        return bounds;
    }

    @Override
    public int size() {
        return columns.size();
    }

    @Override
    public Iterator<TableColumn> iterator() {
        return columns.iterator();
    }

    public static class TableColumn extends Column {

        private final float width;

        private final Align align;

        private final String title;

        private final Font font;

        public TableColumn(String title) {
            this(title, DEFAULT_WIDTH, Align.LEFT, null);
        }

        public TableColumn(String title, float width, Align align, Font font) {
            super(width);
            this.width = width;
            this.align = align;
            this.title = title;
            this.font = font;
        }

        public float width() {
            return width;
        }

        public Align align() {
            return align;
        }

        public Font font() {
            return font;
        }

        public String title() {
            return title;
        }

        public TableColumn scale(float scale) {
            return new TableColumn(title, width * scale, align, font);
        }

    }

}
