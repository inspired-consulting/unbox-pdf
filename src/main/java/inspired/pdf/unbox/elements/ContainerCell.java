package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.elements.internal.AbstractTableCell;

/**
 * This cell allows to add arbitrary elements to a table.
 */
public class ContainerCell extends AbstractTableCell {

    private final Container container;

    public ContainerCell(Container.Layout layout) {
        this(new Container(layout));
    }

    public ContainerCell(Container container) {
        this.container = container;
    }

    public ContainerCell add(PdfElement element) {
        container.add(element);
        return this;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return container.innerHeight(viewPort) + padding().vertical();
    }

    @Override
    public void setValue(Object value) {
        // ignore
    }

    @Override
    protected float renderCell(Document document, Bounds viewPort) {
        return container.render(document, viewPort.apply(padding())) + padding().vertical();
    }

}
