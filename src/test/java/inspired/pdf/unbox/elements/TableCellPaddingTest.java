package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.base.TableModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that the table cell padding applies to rows and headers added before it,
 * while padding set on a row still wins.
 */
class TableCellPaddingTest {

    private static final Bounds VIEW_PORT = new Bounds(0, 800, 400, 800);
    private static final Padding LARGE = Padding.of(20);

    @Test
    void tablePaddingAppliesToRowsAddedBefore() {
        FixedColumnsTable table = new FixedColumnsTable(TableModel.of(1f, 1f));
        TableRow header = table.addHeader(new TableRow(TableModel.of(1f, 1f)).withCells("a", "b"));
        TableRow row = table.addRow("value", 42);

        table.withCellPadding(LARGE);

        assertEquals(heightWithPadding(LARGE), row.innerHeight(VIEW_PORT));
        assertEquals(heightWithPadding(LARGE), header.innerHeight(VIEW_PORT));
    }

    @Test
    void rowPaddingWinsOverTablePadding() {
        FixedColumnsTable table = new FixedColumnsTable(TableModel.of(1f, 1f));
        TableRow row = table.addRow("value", 42).withCellPadding(TableCell.DEFAULT_CELL_PADDING);

        table.withCellPadding(LARGE);

        assertEquals(heightWithPadding(TableCell.DEFAULT_CELL_PADDING), row.innerHeight(VIEW_PORT));
    }

    private float heightWithPadding(Padding padding) {
        return new TableRow(TableModel.of(1f, 1f)).withValues("value", 42)
                .withCellPadding(padding).innerHeight(VIEW_PORT);
    }
}
