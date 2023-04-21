package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.elements.internal.AbstractDecoratable;
import inspired.pdf.unbox.internal.SimpleFont;
import inspired.pdf.unbox.internal.TextWriter;

public class Paragraph extends AbstractDecoratable implements PdfElement {

    private final static float HEIGHT_UNDEFINED = -1f;

    private final TextWriter textWriter;
    private final String text;

    private Align align = Align.LEFT;
    private VAlign vAlign = VAlign.TOP;
    private float innerHeight = HEIGHT_UNDEFINED;

    private Padding padding = Padding.of(2);
    private Margin margin = Margin.none();

    private Integer lineLimit = null;

    public Paragraph(String text) {
        this(text, SimpleFont.helvetica(8));
    }

    public Paragraph(String text, Font font) {
        this.textWriter = new TextWriter(font);
        this.text = text;
    }

    public Paragraph with(Margin margin) {
        this.margin = margin;
        return this;
    }

    public Paragraph with(Padding padding) {
        this.padding = padding;
        return this;
    }

    public Paragraph align(Align align) {
        this.align = align;
        return this;
    }

    public Paragraph align(VAlign vAlign) {
        this.vAlign = vAlign;
        return this;
    }

    public Paragraph align(Align align, VAlign vAlign) {
        this.align = align;
        this.vAlign = vAlign;
        return this;
    }

    public Paragraph withInnerHeight(float height) {
        this.innerHeight = height;
        return this;
    }

    public Paragraph limit(int lineLimit) {
        if(lineLimit <= 0) {
            return this;
        }
        this.lineLimit = lineLimit;
        return this;
    }

    @Override
    public float render(Document document, Bounds viewPort)  {
        float calculatedHeight = innerHeight(viewPort) + renderingHints().getExtraPadding().vertical();
        applyDecorators(document, viewPort.apply(margin).height(calculatedHeight));

        var bounds = effectiveBounds(viewPort, calculatedHeight);
        float actualHeight = textWriter.write(document.getContentStream(), bounds, text, align, vAlign, lineLimit);

        if (innerHeight > HEIGHT_UNDEFINED) {
            return innerHeight + margin.vertical();
        }
        return actualHeight + padding.vertical() + margin.vertical() + renderingHints().getExtraPadding().vertical();
    }

    @Override
    public Margin margin() {
        return margin;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        if (innerHeight > HEIGHT_UNDEFINED) {
            return innerHeight;
        }
        return textWriter.calculateHeight(text, viewPort.apply(padding), lineLimit) + padding.vertical();
    }

    @Override
    public float outerHeight(Bounds viewPort) {
        return super.outerHeight(viewPort);
    }

    private Bounds effectiveBounds(Bounds viewPort, float calculatedHeight) {
        if (innerHeight > HEIGHT_UNDEFINED) {
            return viewPort.apply(margin).height(innerHeight).apply(padding);
        } else {
            return viewPort.apply(margin).apply(padding).height(calculatedHeight - padding.bottom());
        }
    }

    @Override
    public String toString() {
        return "Paragraph['" + text + "']";
    }

}
