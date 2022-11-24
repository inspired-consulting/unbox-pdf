package inspired.pdf.unbox.base;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.elements.TableCell;
import inspired.pdf.unbox.elements.TextCell;

/**
 * Represents the model of a table, i.e. the columns.
 */
public class TableModel implements ColumnModel<TableModel.TableColumn> {

    public static final float DEFAULT_WIDTH = 1f;
    public static TableColumn DEFAULT_COLUMN = new TableColumn("", DEFAULT_WIDTH, Align.LEFT, null, null);

    private final List<TableColumn> columns = new ArrayList<>();
    private final Map<Class<?>, TableCell> defaultCells = new LinkedHashMap<>();

    private static final TableCell emptyCell = new EmptyCell();

    public TableModel() {
        this.defaultCells.put(Number.class, new TextCell("", Align.RIGHT, null));
        this.defaultCells.put(Object.class, new TextCell(""));
    }

    private TableModel(List<TableColumn> columns) {
        this();
        this.columns.addAll(columns);
    }

    public static TableModel of(TableColumn ... columns) {
        return new TableModel(List.of(columns));
    }

    public static TableModel of(float... widths) {
        List<TableColumn> columns = new ArrayList<>();
        for (float w : widths) {
            columns.add(new TableColumn(w));
        }
        return new TableModel(columns);
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
        return add(new TableColumn(title, width, Align.LEFT, null, null));
    }

    public TableModel add(String title, Align align) {
        return add(new TableColumn(title, DEFAULT_WIDTH, align, null, null));
    }

    public TableModel add(String title, float width, Align align) {
        return add(new TableColumn(title, width, align, null, null));
    }

    public TableModel add(String title, TableCell cell) {
        return add(new TableColumn(title, DEFAULT_WIDTH, Align.LEFT, null, cell));
    }

    public TableModel add(String title, float width, TableCell cell) {
        return add(new TableColumn(title, width, Align.LEFT, null, cell));
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

    @Override
    public TableModel scale(float scale) {
        List<TableColumn> adapted = columns.stream().map(c -> c.scale(scale)).collect(Collectors.toList());
        return new TableModel(adapted);
    }

    @Override
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

    public TableCell getDefaultCellFor(Object value) {
        if(value == null) {
            return emptyCell;
        }
        for (var entry : defaultCells.entrySet()){
            if(entry.getKey().isInstance(value)) {
                return entry.getValue();
            }
        }
        throw new IllegalArgumentException("No cell found for value "+ value);
    }

    public static class TableColumn extends Column {

        private final float width;

        private final Align align;

        private final String title;

        private final Font font;

        private final TableCell cell;

        public TableColumn(String title) {
            this(title, DEFAULT_WIDTH, Align.LEFT, null, null);
        }

        public TableColumn(float width) {
            this("", width, Align.LEFT, null, null);
        }

        public TableColumn(String title, float width, Align align, Font font, TableCell cell) {
            super(width);
            this.width = width;
            this.align = align;
            this.title = title;
            this.font = font;
            this.cell = cell;
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

        public TableCell cell() {
            return cell;
        }

        public TableColumn scale(float scale) {
            return new TableColumn(title, width * scale, align, font, cell);
        }

    }

}
