package inspired.pdf.unbox.base;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents the columns of a layout.
 */
public class SimpleColumnModel implements ColumnModel<Column> {

    private final List<Column> columns = new ArrayList<>();

    public SimpleColumnModel() {
    }

    public SimpleColumnModel(List<Column> columns) {
        this.columns.addAll(columns);
    }

    public static SimpleColumnModel of(Integer[] sizes) {
        return new SimpleColumnModel(Stream.of(sizes)
                .map(Column::new)
                .toList());
    }

    public static SimpleColumnModel of(Float[] sizes) {
        return new SimpleColumnModel(Stream.of(sizes)
                .map(Column::new)
                .toList());
    }

    public static SimpleColumnModel of(float... width) {
        List<Column> columns = new ArrayList<>();
        for (float w : width) {
            columns.add(new Column(w));
        }
        return new SimpleColumnModel(columns);
    }

    public SimpleColumnModel add(Column column) {
        this.columns.add(column);
        return this;
    }

    public SimpleColumnModel add(float width) {
        return add(new Column(width));
    }

    public SimpleColumnModel append(SimpleColumnModel other) {
        this.columns.addAll(other.columns);
        return this;
    }

    @Override
    public int size() {
        return columns.size();
    }

    @Override
    public Column get(int index) {
        return columns.get(index);
    }
    @Override
    public float getOverallWidth() {
        return columns.stream().map(Column::width).reduce(Float::sum).orElse(0f);
    }

    public SimpleColumnModel scale(float scale) {
        List<Column> adapted = columns.stream().map(c -> c.scale(scale)).collect(Collectors.toList());
        return new SimpleColumnModel(adapted);
    }

    public SimpleColumnModel scaleToSize(float width) {
        return scale(width / getOverallWidth());
    }

    public List<Bounds> toBounds(Bounds viewPort) {
        List<Bounds> bounds = new ArrayList<>();
        float offset = viewPort.left();
        for (Column colum : columns) {
            bounds.add(new Bounds(offset, viewPort.top(), colum.width(), viewPort.height()));
            offset += colum.width();
        }
        return bounds;
    }

    @Override
    public Iterator<Column> iterator() {
        return columns.iterator();
    }


}
