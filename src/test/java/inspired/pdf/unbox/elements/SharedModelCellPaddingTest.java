package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.base.TableModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that shared model cells use the default padding of the row that measures them,
 * so a model reused by rows or tables with different cell paddings does not keep the first one.
 */
class SharedModelCellPaddingTest {

    private static final Bounds VIEW_PORT = new Bounds(0, 800, 400, 800);
    private static final Padding LARGE = Padding.of(20);

    @Test
    void defaultCellsUseThePaddingOfEachRow() {
        TableModel shared = TableModel.of(1f, 1f);
        float expectedDefault = valueRow(TableModel.of(1f, 1f), null).innerHeight(VIEW_PORT);
        float expectedLarge = valueRow(TableModel.of(1f, 1f), LARGE).innerHeight(VIEW_PORT);

        assertEquals(expectedLarge, valueRow(shared, LARGE).innerHeight(VIEW_PORT));
        assertEquals(expectedDefault, valueRow(shared, null).innerHeight(VIEW_PORT));
        assertEquals(expectedDefault, valueRow(shared, TableCell.DEFAULT_CELL_PADDING).innerHeight(VIEW_PORT));
    }

    @Test
    void columnCellPrototypesUseThePaddingOfEachRow() {
        TableModel shared = new TableModel().add("Name", new TextCell(""));
        float expectedDefault = valueRow(new TableModel().add("Name", new TextCell("")), null)
                .innerHeight(VIEW_PORT);

        valueRow(shared, LARGE).innerHeight(VIEW_PORT);

        assertEquals(expectedDefault, valueRow(shared, null).innerHeight(VIEW_PORT));
    }

    @Test
    void explicitPrototypePaddingWinsOverRowDefault() {
        Padding explicit = Padding.of(10);
        TextCell prototype = new TextCell("").with(explicit);
        TableModel shared = new TableModel().add("Name", prototype);

        float large = new TableRow(shared).withValues("value").withCellPadding(LARGE).innerHeight(VIEW_PORT);
        float standard = new TableRow(shared).withValues("value").innerHeight(VIEW_PORT);

        assertEquals(large, standard);
        assertEquals(explicit, prototype.padding());
    }

    private TableRow valueRow(TableModel model, Padding cellPadding) {
        TableRow row = new TableRow(model).withValues("value", 42);
        if (cellPadding != null) {
            row.withCellPadding(cellPadding);
        }
        return row;
    }
}
