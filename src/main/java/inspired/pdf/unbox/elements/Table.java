package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Stroke;
import inspired.pdf.unbox.decorators.Decorator;

/**
 * A Table displays data as rows and columns.
 * There are two types of rows:
 * <ol>
 *     <li>Header rows: They will be rendered again on each new page after a page break</li>
 *     <li>Standard rows: They contain the actual data and will only be rendered once</li>
 * </ol>
 */
public interface Table extends PdfElement {

    /**
     * Add a new, empty row.
     * @return The created row.
     */
    TableRow addRow();

    /**
     * Add the given row.
     * @param row The row.
     * @return The row.
     */
    TableRow addRow(TableRow row);

    /**
     * Add a new row for the given values. For each value a new text cell will be appended to the row.
     * @param values The values to be added to the row.
     * @return The created row.
     */
    TableRow addRow(Object... values);

    /**
     * Add a header row that will be repeated on each page.
     * @param row The header row
     * @return The created row.
     */
    TableRow addHeader(TableRow row);


    /**
     * Set the margin of the table.
     * @param margin The margin.
     * @return The table object for chaining.
     */
    Table with(Margin margin);

    /**
     * Set the stroke for the grid.
     * @param stroke The stroke.
     * @return The table object for chaining.
     */
    Table with(Stroke stroke);

    /**
     * Add a decorator to the table.
     * @param decorator The decorator.
     * @return The table object for chaining.
     */
    @Override
    Table with(Decorator decorator);

}
