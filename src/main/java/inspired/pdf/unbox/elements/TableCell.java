package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Padding;

/**
 * Interface for cells in PDF tables.
 */
public interface TableCell extends PdfElement {

    Padding DEFAULT_CELL_PADDING = Padding.of(4, 4);

    /**
     * Set the value that should be rendered by the cell.
     * @param value Can be `null`, if a cell has been configured as
     *              part of a column of the TableModel and the value in
     *              a row is `null`.
     */
    void setValue(Object value);


    /**
     * Set the default cell padding to be applied if no custom padding is specified.
     * @param padding The default padding of the table or table row.
     * @return This.
     */
    TableCell withDefaultPadding(Padding padding);

}
