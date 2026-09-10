package inspired.pdf.unbox;

import org.junit.jupiter.api.Assertions;

/**
 * Shared assertions for the coordinate and box-model tests.
 */
public final class GeometryAssertions {

    private static final float EPSILON = 0.0001f;

    private GeometryAssertions() {
    }

    public static void assertBounds(Bounds actual, float left, float top, float width, float height) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(left, actual.left(), EPSILON, "left"),
                () -> Assertions.assertEquals(top, actual.top(), EPSILON, "top"),
                () -> Assertions.assertEquals(width, actual.width(), EPSILON, "width"),
                () -> Assertions.assertEquals(height, actual.height(), EPSILON, "height")
        );
    }

    public static void assertPosition(Position actual, float x, float y) {
        Assertions.assertAll(
                () -> Assertions.assertEquals(x, actual.x(), EPSILON, "x"),
                () -> Assertions.assertEquals(y, actual.y(), EPSILON, "y")
        );
    }

    public static void assertClose(float expected, float actual) {
        Assertions.assertEquals(expected, actual, EPSILON);
    }
}
