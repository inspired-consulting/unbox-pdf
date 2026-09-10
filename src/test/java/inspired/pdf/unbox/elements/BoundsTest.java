package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static inspired.pdf.unbox.GeometryAssertions.assertBounds;
import static inspired.pdf.unbox.GeometryAssertions.assertPosition;

/**
 * Verifies coordinate, edge, movement, and bounds-combination operations.
 */
public class BoundsTest {

    @Test
    public void can_extend_bounds() {
        Bounds bounds_1 = new Bounds(10, 300, 100, 100);
        Bounds bounds_2 = new Bounds(300, 200, 100, 100);

        assertEquals(bounds_1.extend(bounds_2), bounds_2.extend(bounds_1));

        Bounds extended = bounds_1.extend(bounds_2);

        assertEquals(10, extended.left());
        assertEquals(300, extended.top());
        assertEquals(400, extended.right());
        assertEquals(100, extended.bottom());
    }

    @Test
    public void can_join_bounds() {
        Bounds bounds_1 = new Bounds(400, 300, 100, 100);
        Bounds bounds_2 = new Bounds(300, 200, 100, 100);
        Bounds bounds_3 = new Bounds(50, 100, 500, 10);

        Bounds joined = Bounds.join(bounds_1, bounds_2, bounds_3);

        assertEquals(50, joined.left());
        assertEquals(300, joined.top());
        assertEquals(550, joined.right());
        assertEquals(90, joined.bottom());
    }

    @Test
    public void exposes_edges_and_center_positions() {
        Bounds bounds = new Bounds(10, 300, 100, 80);

        assertEquals(220, bounds.bottom());
        assertEquals(60, bounds.center());
        assertEquals(260, bounds.middle());
        assertPosition(bounds.topLeft(), 10, 300);
        assertPosition(bounds.topRight(), 110, 300);
        assertPosition(bounds.bottomLeft(), 10, 220);
        assertPosition(bounds.bottomRight(), 110, 220);
    }

    @Test
    public void moves_preserve_size_and_change_one_coordinate() {
        Bounds bounds = new Bounds(10, 300, 100, 80);

        assertBounds(bounds.moveRight(25), 35, 300, 100, 80);
        assertBounds(bounds.moveDown(25), 10, 275, 100, 80);
    }

}
