package inspired.pdf.unbox;

/**
 * Represents a bounded region inside a document.
 */
public record Bounds(float left, float top, float width, float height) {

    public float bottom() {
        return top - height;
    }

    public float center() {
        return left + width / 2;
    }

    public float right() { return left + width; }

    public float middle() {
        return top - height / 2;
    }

    public Position topLeft() {
        return new Position(left, top);
    }

    public Position topRight() {
        return new Position(right(), top);
    }

    public Position bottomLeft() {
        return new Position(left, bottom());
    }

    public Position bottomRight() {
        return new Position(right(), bottom());
    }

    public Bounds moveRight(float amount) {
        return new Bounds(left + amount, top, width, height);
    }

    public Bounds moveDown(float amount) {
        return new Bounds(left, top - amount, width, height);
    }

    public Bounds apply(Margin margin) {
        return new Bounds(left + margin.left(), top - margin.top(),
                width - margin.horizontal(), height - margin.vertical());
    }

    public Bounds apply(Padding padding) {
        return new Bounds(left + padding.left(), top - padding.top(),
                width - padding.horizontal(), height - padding.vertical());
    }

    public Bounds top(float top) {
        return new Bounds(left, top, width, height);
    }

    public Bounds left(float left) {
        return new Bounds(left, top, width, height);
    }

    public Bounds height(float height) {
        return new Bounds(left, top, width, height);
    }

    public Bounds width(float width) {
        return new Bounds(left, top, width, height);
    }


}
