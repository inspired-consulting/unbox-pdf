package inspired.pdf.unbox;

import java.util.Arrays;

/**
 * Represents a bounded region inside a document.
 */
public record Bounds(float left, float top, float width, float height) {

    public static Bounds join(Bounds... bounds) {
        float left = Arrays.stream(bounds).map(Bounds::left).min(Float::compareTo).orElse(0f);
        float top = Arrays.stream(bounds).map(Bounds::top).max(Float::compareTo).orElse(0f);
        float right = Arrays.stream(bounds).map(Bounds::right).max(Float::compareTo).orElse(0f);
        float bottom = Arrays.stream(bounds).map(Bounds::bottom).min(Float::compareTo).orElse(0f);
        return new Bounds(left, top, right - left, top - bottom);
    }

    public float bottom() {
        return top - height;
    }

    public float center() {
        return left + width / 2f;
    }

    public float right() { return left + width; }

    public float middle() {
        return top - height / 2f;
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

    public float top() {
        return top;
    }

    public float left() {
        return left;
    }

    public float height() {
        return height;
    }

    public float width() {
        return width;
    }

    public Bounds extend(Bounds bounds) {
        float left = Math.min(this.left, bounds.left);
        float top = Math.max(this.top, bounds.top);
        float right = Math.max(this.right(), bounds.right());
        float bottom = Math.min(this.bottom(), bounds.bottom());
        return new Bounds(left, top, right - left, top - bottom);
    }
}
