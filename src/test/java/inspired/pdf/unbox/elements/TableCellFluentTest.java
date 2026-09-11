package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.elements.internal.AbstractTableCell;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static inspired.pdf.unbox.Unbox.background;
import static inspired.pdf.unbox.Unbox.column;
import static inspired.pdf.unbox.Unbox.paragraph;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Verifies fluent cell types at compile time and that configuration retains the cell instance.
 */
class TableCellFluentTest {

    @Test
    void textCellConfigurationPreservesConcreteTypeAndExplicitPadding() {
        TextCell original = new TextCell("value");
        Padding padding = Padding.of(3);
        TextCell configured = original
            .with(background(Color.WHITE))
            .with(padding)
            .withDefaultPadding(Padding.of(8))
            .withAlign(Align.RIGHT);

        assertSame(original, configured);
        assertSame(padding, configured.padding());
        TableRow row = new TableRow();
        row.addCell(configured.with(background(Color.WHITE)));
        assertEquals(1, row.size());
    }

    @Test
    void configuredContainerCellStillExposesAdd() {
        ContainerCell original = new ContainerCell(column());
        ContainerCell configured = original.with(background(Color.WHITE))
                .with(Padding.of(3)).withDefaultPadding(Padding.of(8)).withAlign(Align.LEFT)
                .add(paragraph("value"));

        assertSame(original, configured);
        TableRow row = new TableRow();
        row.addCell(configured);
        assertEquals(1, row.size());
    }

    @Test
    void baseCellCanBeDecoratedAndAddedDirectly() {
        AbstractTableCell cell = new TextCell("value");
        TableRow row = new TableRow();

        row.addCell(cell.with(background(Color.WHITE)));

        assertEquals(1, row.size());
    }
}
