package inspired.pdf.unbox;

/**
 * The margin describes the space inside the element.
 * @param top
 * @param right
 * @param bottom
 * @param left
 */
public record Padding(float top, float right, float bottom, float left) {

    public static Padding of(float m) {
        return new Padding(m, m, m, m);
    }

    public static Padding of(float vertical, float horizontal) {
        return new Padding(vertical, horizontal, vertical, horizontal);
    }

    public static Padding of(float top, float right, float bottom, float left) {
        return new Padding(top, right, bottom, left);
    }

    public static Padding of(float top, float vertical, float bottom) {
        return new Padding(top, vertical, bottom, vertical);
    }

    public float horizontal() {
        return left + right;
    }

    public float vertical() {
        return top + bottom;
    }

    public float horizontalShift() {
        return left - right;
    }

}
