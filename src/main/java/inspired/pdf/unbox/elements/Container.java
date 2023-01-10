package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.internal.AbstractPdfElement;
import inspired.pdf.unbox.elements.internal.ContainerLayout;
import inspired.pdf.unbox.elements.internal.HorizontalLayout;
import inspired.pdf.unbox.elements.internal.VerticalLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for PDF multiple elements.
 */
public class Container extends AbstractPdfElement {

    public enum Layout {
        COLUMNS,
        ROWS
    }

    public static Container withColumnLayout() {
        return new Container(Layout.COLUMNS);
    }

    public static Container withColumnLayout(ColumnModel<?> model) {
        return new Container(model);
    }

    public static Container withRowLayout() {
        return new Container(Layout.ROWS);
    }

    private final List<PdfElement> elements = new ArrayList<>();
    private final List<Decorator> decorators = new ArrayList<>();
    private final ContainerLayout layout;


    public Container(ContainerLayout layout) {
        this.layout = layout;
    }

    public Container(Layout layout) {
        this.layout = switch (layout) {
            case COLUMNS -> new HorizontalLayout();
            case ROWS -> new VerticalLayout();
        };
    }

    public Container(ColumnModel<?> model) {
        this(new HorizontalLayout(model));
    }

    public Container add(PdfElement element) {
        elements.add(element);
        return this;
    }

    @Override
    public Container with(Margin margin) {
        super.with(margin);
        return this;
    }

    @Override
    public Container with(Padding padding) {
        super.with(padding);
        return this;
    }

    @Override
    public float render(Document document, Bounds viewPort) {
        applyDecorators(document, viewPort);
        return layout.render(document, viewPort, this, elements);
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return layout.innerHeight(viewPort, this, elements);
    }

    @Override
    public Container with(Decorator decorator) {
        this.decorators.add(decorator);
        return this;
    }

    public List<PdfElement> getElements() {
        return elements;
    }

    protected void applyDecorators(Document document, Bounds viewPort) {
        var bounds = viewPort
                .height(outerHeight(viewPort))
                .apply(margin());
        for (Decorator decorator : decorators) {
            decorator.render(document, bounds);
        }
    }

    @Override
    public String toString() {
        return "Container[" + layout.layoutType() + "]{" +
                "elements=" + elements +
                '}';
    }
}
