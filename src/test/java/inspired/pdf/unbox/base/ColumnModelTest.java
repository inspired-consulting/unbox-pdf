package inspired.pdf.unbox.base;

import inspired.pdf.unbox.Bounds;
import org.junit.jupiter.api.Test;

import java.util.List;

import static inspired.pdf.unbox.GeometryAssertions.assertBounds;
import static inspired.pdf.unbox.GeometryAssertions.assertClose;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies relative column scaling and the bounds produced for each column.
 */
class ColumnModelTest {

    @Test
    void scaledColumnsPreserveRelativeWidthsAndFillViewport() {
        SimpleColumnModel model = SimpleColumnModel.of(1f, 2f, 1f).scaleToSize(400);

        assertEquals(3, model.size());
        assertClose(100, model.width(0));
        assertClose(200, model.width(1));
        assertClose(100, model.width(2));
        assertClose(400, model.getOverallWidth());
    }

    @Test
    void columnBoundsAreAdjacentAndRetainVerticalGeometry() {
        Bounds viewport = new Bounds(50, 700, 400, 250);
        List<Bounds> columns = SimpleColumnModel.of(1f, 2f, 1f)
                .scaleToSize(viewport.width())
                .toBounds(viewport);

        assertBounds(columns.get(0), 50, 700, 100, 250);
        assertBounds(columns.get(1), 150, 700, 200, 250);
        assertBounds(columns.get(2), 350, 700, 100, 250);
        assertClose(viewport.right(), columns.get(2).right());
    }

    @Test
    void uniformModelCreatesRequestedNumberOfEqualColumns() {
        SimpleColumnModel model = SimpleColumnModel.uniform(4).scaleToSize(200);

        assertEquals(4, model.size());
        for (int i = 0; i < model.size(); i++) {
            assertClose(50, model.width(i));
        }
    }

    @Test
    void appendKeepsColumnOrder() {
        SimpleColumnModel model = SimpleColumnModel.of(2f).append(SimpleColumnModel.of(3f, 4f));

        assertEquals(3, model.size());
        assertEquals(2f, model.width(0));
        assertEquals(3f, model.width(1));
        assertEquals(4f, model.width(2));
    }

    @Test
    void negativeAndNonFiniteWidthsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Column(-1f));
        assertThrows(IllegalArgumentException.class, () -> new Column(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> SimpleColumnModel.of(1f, Float.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> new TableModel.TableColumn(-2f));
        assertThrows(IllegalArgumentException.class, () -> TableModel.of(1f, Float.NaN));
    }

    @Test
    void allZeroWidthsFailWithDescriptiveMessage() {
        IllegalArgumentException simple = assertThrows(IllegalArgumentException.class,
                () -> SimpleColumnModel.of(0f, 0f).scaleToSize(100));
        assertTrue(simple.getMessage().contains("positive"), simple.getMessage());

        IllegalArgumentException table = assertThrows(IllegalArgumentException.class,
                () -> TableModel.of(0f, 0f).scaleToSize(100));
        assertTrue(table.getMessage().contains("positive"), table.getMessage());
    }

    @Test
    void singleZeroWidthColumnAmongPositiveColumnsScales() {
        SimpleColumnModel model = SimpleColumnModel.of(1f, 0f, 3f).scaleToSize(400);

        assertClose(100, model.width(0));
        assertClose(0, model.width(1));
        assertClose(300, model.width(2));
    }

    @Test
    void emptyModelScalesToEmptyModel() {
        assertEquals(0, new SimpleColumnModel().scaleToSize(100).size());
        assertEquals(0, new TableModel().scaleToSize(100).size());
    }
}
