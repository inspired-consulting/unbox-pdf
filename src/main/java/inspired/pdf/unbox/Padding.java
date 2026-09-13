package inspired.pdf.unbox;

/**
 * The padding describes the space inside the element.
 *
 * @param top    Padding top
 * @param right  Padding right
 * @param bottom Padding bottom
 * @param left   Padding left
 */
public record Padding(float top, float right, float bottom, float left) {

    public static Padding of(float m) {
        return new Padding(m, m, m, m);
    }

    public static Padding of(Length length) {
        return of(length.points());
    }

    public static Padding of(float vertical, float horizontal) {
        return new Padding(vertical, horizontal, vertical, horizontal);
    }

    public static Padding of(Length vertical, Length horizontal) {
        return of(vertical.points(), horizontal.points());
    }

    public static Padding of(float top, float right, float bottom, float left) {
        return new Padding(top, right, bottom, left);
    }

    public static Padding of(Length top, Length right, Length bottom, Length left) {
        return of(top.points(), right.points(), bottom.points(), left.points());
    }

    public static Padding of(float top, float horizontal, float bottom) {
        return new Padding(top, horizontal, bottom, horizontal);
    }

    public static Padding of(Length top, Length horizontal, Length bottom) {
        return of(top.points(), horizontal.points(), bottom.points());
    }

    public static Padding top(float top) {
        return new Padding(top, 0, 0, 0);
    }

    public static Padding top(Length top) {
        return top(top.points());
    }

    public static Padding right(float right) {
        return new Padding(0, right, 0, 0);
    }

    public static Padding right(Length right) {
        return right(right.points());
    }

    public static Padding bottom(float bottom) {
        return new Padding(0, 0, bottom, 0);
    }

    public static Padding bottom(Length bottom) {
        return bottom(bottom.points());
    }

    public static Padding left(float left) {
        return new Padding(0, 0, 0, left);
    }

    public static Padding left(Length left) {
        return left(left.points());
    }

    public static Padding none() {
        return Padding.of(0);
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

    public Padding add(Padding padding) {
        return Padding.of(top + padding.top, right + padding.right,
            bottom + padding.bottom, left + padding.left);
    }

}
