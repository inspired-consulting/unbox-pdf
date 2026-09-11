package inspired.pdf.unbox.base;

public class Column {

    private final float width;

    public Column(float width) {
        if (Float.isNaN(width) || Float.isInfinite(width) || width < 0) {
            throw new IllegalArgumentException("Column width must be a finite, non-negative number, but was " + width);
        }
        this.width = width;
    }

    public float width() {
        return width;
    }

    public Column scale(float scale) {
        return new Column(width * scale);
    }

    @Override
    public String toString() {
        return String.valueOf(width);
    }
}
