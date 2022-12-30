package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.elements.TableCell;

public abstract class AbstractTableCell extends AbstractDecoratable implements TableCell {

    protected Padding padding = Padding.of(4, 4);
    protected Align align = Align.LEFT;

    public AbstractTableCell withPadding(Padding padding) {
        this.padding = padding;
        return this;
    }

    public AbstractTableCell withAlign(Align align) {
        this.align = align;
        return this;
    }

    @Override
    public final float render(Document document, Bounds viewPort) {
        applyDecorators(document, viewPort);
        return renderCell(document, viewPort);
    }

    protected abstract float renderCell(Document document, Bounds viewPort);

}
