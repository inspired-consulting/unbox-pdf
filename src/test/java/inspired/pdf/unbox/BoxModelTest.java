package inspired.pdf.unbox;

import org.junit.jupiter.api.Test;

import static inspired.pdf.unbox.GeometryAssertions.assertBounds;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies margin and padding arithmetic used to derive content bounds.
 */
class BoxModelTest {

    @Test
    void marginAndPaddingTotalsUseOppositeEdges() {
        Margin margin = Margin.of(10, 20, 30, 40);
        Padding padding = Padding.of(1, 2, 3, 4);

        assertEquals(40, margin.vertical());
        assertEquals(60, margin.horizontal());
        assertEquals(4, padding.vertical());
        assertEquals(6, padding.horizontal());
        assertEquals(2, padding.horizontalShift());
    }

    @Test
    void asymmetricMarginShrinksBoundsInward() {
        Bounds bounds = new Bounds(100, 500, 300, 200);

        assertBounds(bounds.apply(Margin.of(10, 20, 30, 40)), 140, 490, 240, 160);
    }

    @Test
    void asymmetricPaddingShrinksBoundsInward() {
        Bounds bounds = new Bounds(100, 500, 300, 200);

        assertBounds(bounds.apply(Padding.of(10, 20, 30, 40)), 140, 490, 240, 160);
    }

    @Test
    void boxModelCanBeAppliedInLayers() {
        Bounds bounds = new Bounds(0, 100, 200, 100);

        Bounds content = bounds.apply(Margin.of(10)).apply(Padding.of(5, 10));

        assertBounds(content, 20, 85, 160, 70);
    }

    @Test
    void directionalFactoriesOnlyAffectTheirEdge() {
        assertEquals(new Margin(7, 0, 0, 0), Margin.top(7));
        assertEquals(new Margin(0, 9, 0, 0), Margin.right(9));
        assertEquals(new Margin(0, 0, 11, 0), Margin.bottom(11));
        assertEquals(new Margin(0, 0, 0, 13), Margin.left(13));
        assertEquals(new Padding(7, 0, 0, 0), Padding.top(7));
        assertEquals(new Padding(0, 9, 0, 0), Padding.right(9));
        assertEquals(new Padding(0, 0, 11, 0), Padding.bottom(11));
        assertEquals(new Padding(0, 0, 0, 13), Padding.left(13));
    }
}
