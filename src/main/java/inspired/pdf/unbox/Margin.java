package inspired.pdf.unbox;

/**
 * The margin describes the space around the element.
 * @param top
 * @param right
 * @param bottom
 * @param left
 */
public record Margin(float top, float right, float bottom, float left) {

    public static Margin of(float m) {
        return new Margin(m, m, m, m);
    }

    public static Margin of(float vertical, float horizontal) {
        return new Margin(vertical, horizontal, vertical, horizontal);
    }

    public static Margin top(float top) {
        return new Margin(top, 0,0,0);
    }

    public static Margin right(float right) {
        return new Margin(0, right,0,0);
    }

    public static Margin bottom(float bottom) {
        return new Margin(0, 0,bottom,0);
    }

    public static Margin left(float left) {
        return new Margin(0, 0,0, left);
    }

    public static Margin none() {
        return Margin.of(0);
    }

    public float horizontal() {
        return left + right;
    }

    public float vertical() {
        return top + bottom;
    }

}
