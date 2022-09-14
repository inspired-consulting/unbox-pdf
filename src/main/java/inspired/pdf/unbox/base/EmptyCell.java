package inspired.pdf.unbox.base;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.elements.internal.AbstractTableCell;


/**
 * Empty Cell
 */
class EmptyCell extends AbstractTableCell {

    @Override
    public float render(Document document, Bounds bounds) {
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
