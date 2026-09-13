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

    public static Margin of(Length length) {
        return of(length.points());
    }

    public static Margin of(float vertical, float horizontal) {
        return new Margin(vertical, horizontal, vertical, horizontal);
    }

    public static Margin of(Length vertical, Length horizontal) {
        return of(vertical.points(), horizontal.points());
    }

    public static Margin of(float top, float right, float bottom, float left) {
        return new Margin(top, right, bottom, left);
    }

    public static Margin of(Length top, Length right, Length bottom, Length left) {
        return of(top.points(), right.points(), bottom.points(), left.points());
    }

    public static Margin of(float top, float horizontal, float bottom) {
        return new Margin(top, horizontal, bottom, horizontal);
    }

    public static Margin of(Length top, Length horizontal, Length bottom) {
        return of(top.points(), horizontal.points(), bottom.points());
    }

    public static Margin top(float top) {
        return new Margin(top, 0,0,0);
    }

    public static Margin top(Length top) {
        return top(top.points());
    }

    public static Margin right(float right) {
        return new Margin(0, right,0,0);
    }

    public static Margin right(Length right) {
        return right(right.points());
    }

    public static Margin bottom(float bottom) {
        return new Margin(0, 0,bottom,0);
    }

    public static Margin bottom(Length bottom) {
        return bottom(bottom.points());
    }

    public static Margin left(float left) {
        return new Margin(0, 0,0, left);
    }

    public static Margin left(Length left) {
        return left(left.points());
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
