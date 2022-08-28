package inspired.pdf.unbox.base;

public class Column {

    private final float width;

    public Column(float width) {
        this.width = width;
    }

    public float width() {
        return width;
    }

    public Column scale(float scale) {
        return new Column(width * scale);
    }

}
