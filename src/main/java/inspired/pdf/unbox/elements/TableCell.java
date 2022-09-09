package inspired.pdf.unbox.elements;

/**
 * Interface for cells in PDF tables.
 */
public interface TableCell extends PdfElement {

    /**
     * Set the value that should be rendered by the cell.
     * @param value Can be `null`, if a cell has been configured as
     *              part of a column of the TableModel and the value in
     *              a row is `null`.
     */
    void setValue(Object value);

}
