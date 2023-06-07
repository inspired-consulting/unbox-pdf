package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.elements.TableCell;

public abstract class AbstractTableCell extends AbstractDecoratable implements TableCell {

    protected Align align = Align.LEFT;
    private Padding padding;

    public Padding padding() {
        if (padding != null) {
            return padding;
        } else {
            return DEFAULT_CELL_PADDING;
        }
    }

    public AbstractTableCell with(Padding padding) {
        this.padding = padding;
        return this;
    }

    @Override
    public AbstractTableCell withDefaultPadding(Padding padding) {
        if (this.padding == null) {
            this.padding = padding;
        }
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
