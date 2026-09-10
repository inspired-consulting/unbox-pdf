package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.base.TableModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Protects row cell creation at model boundaries and default-column expansion.
 */
class TableRowTest {

    @Test
    void canAddTextCellToEmptyRow() {
        TableRow row = new TableRow();

        assertSame(row, row.addCell("hello"));
        assertEquals(1, row.size());
        assertEquals(1, row.columnModel().size());
    }

    @Test
    void canAddTextCellBeyondDefinedColumns() {
        TableModel model = new TableModel().add("Amount", 2f, Align.RIGHT);
        TableRow row = new TableRow(model);

        row.addCell("42");
        row.addCell("extra");

        assertEquals(2, row.size());
        assertEquals(2, model.size());
        assertEquals(2f, model.get(0).width());
        assertEquals(Align.RIGHT, model.get(0).align());
        assertEquals(TableModel.DEFAULT_WIDTH, model.get(1).width());
    }
}
