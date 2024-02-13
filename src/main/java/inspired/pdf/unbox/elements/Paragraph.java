package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.elements.internal.AbstractDecoratable;
import inspired.pdf.unbox.internal.SimpleFont;
import inspired.pdf.unbox.internal.TextWriter;

/**
 * A paragraph consists of text that can span multiple lines and may be aligned horizontally and vertically.
 */
public class Paragraph extends AbstractDecoratable implements PdfElement {

    protected final static float HEIGHT_UNDEFINED = -1f;

    protected final TextWriter textWriter;
    protected final String text;

    protected Align align = Align.LEFT;
    protected VAlign vAlign = VAlign.TOP;
    protected float innerHeight = HEIGHT_UNDEFINED;

    protected Padding padding = Padding.of(2);
    protected Margin margin = Margin.none();

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

    /**
     * Specify the inner height instead of calculation.
     * @param height The inner height, i.e. without padding and margin.
     * @return The paragraph.
     */
    public Paragraph withInnerHeight(float height) {
        this.innerHeight = height;
        return this;
    }

    /**
     * Set the maximum number of lines for this paragraph. If the text is longer it will be truncated.
     * @param lineLimit The maximum number of lines. May be null.
     * @return The paragraph.
     */
    public Paragraph limit(int lineLimit) {
        if (lineLimit <= 0) {
            return this;
        }
        this.lineLimit = lineLimit;
        return this;
    }

    @Override
    public float render(Document document, Bounds viewPort) {
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

    @Override
    public String toString() {
        return "Paragraph['" + text + "']";
    }

    protected Bounds effectiveBounds(Bounds viewPort, float calculatedHeight) {
        if (innerHeight > HEIGHT_UNDEFINED) {
            return viewPort.apply(margin).height(innerHeight).apply(padding);
        } else {
            return viewPort.apply(margin).height(calculatedHeight).apply(padding);
        }
    }

}
