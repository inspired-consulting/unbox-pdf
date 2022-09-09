package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.elements.internal.AbstractTableCell;


/**
 * Empty Cell
 */
public class EmptyCell extends AbstractTableCell {

    @Override
    public float render(LinearPDFWriter writer, Bounds bounds) {
        return 0.0f;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return 0.0f;
    }

    @Override
    public void setValue(Object value) {

    }
}
