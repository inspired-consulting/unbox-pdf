package inspired.pdf.unbox;

/**
 * Represents a border that can be added to an element.
 * @param top Thickness of top border
 * @param right Thickness of right border
 * @param bottom Thickness of bottom border
 * @param left Thickness of left border
 * @param radius Corner radius; zero draws square corners. A positive radius requires uniform thickness.
 */
public record Border(float top, float right, float bottom, float left, float radius) {

    public Border {
        if (radius < 0 || Float.isNaN(radius)) {
            throw new IllegalArgumentException("Border radius must not be negative, but was " + radius);
        }
        if (radius > 0 && !(top == right && top == bottom && top == left)) {
            throw new IllegalArgumentException("A border radius requires a uniform thickness, but was "
                    + top + ", " + right + ", " + bottom + ", " + left);
        }
    }

    public Border(float top, float right, float bottom, float left) {
        this(top, right, bottom, left, 0);
    }

    public static Border of(float m) {
        return new Border(m, m, m, m);
    }

    public static Border of(float vertical, float horizontal) {
        return new Border(vertical, horizontal, vertical, horizontal);
    }

    public static Border of(float top, float right, float bottom, float left) {
        return new Border(top, right, bottom, left);
    }

    public static Border of(float top, float vertical, float bottom) {
        return new Border(top, vertical, bottom, vertical);
    }

    public static Border top(float top) {
        return new Border(top, 0,0,0);
    }

    public static Border bottom(float bottom) {
        return new Border(0,0,bottom, 0);
    }

    public static Border left(float left) {
        return new Border(0,0,0, left);
    }

    public static Border right(float right) {
        return new Border(0,right,0, 0);
    }

    /**
     * Create a copy with rounded corners of the given radius.
     */
    public Border withRadius(float radius) {
        return new Border(top, right, bottom, left, radius);
    }

    public boolean isUniform() {
        return top == right && top == bottom && top == left;
    }

    public boolean isRounded() {
        return radius > 0;
    }

}
