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
    private Overflow overflow = Overflow.CLIP;


    /**
     * Create a new paragraph with the given text and overflow settings.
     *
     * @param text     The text of the paragraph
     */
    public Paragraph(String text) {
        this(text, SimpleFont.helvetica(8));
    }

    /**
     * Create a new paragraph with the given text and font.
     *
     * @param text The text of the paragraph
     * @param font The font to use
     */
    public Paragraph(String text, Font font) {
        this.textWriter = new TextWriter(font);
        this.text = text;
    }

    /**
     * Set the margin for this paragraph.
     *
     * @param margin The margin to use
     * @return The paragraph.
     */
    public Paragraph with(Margin margin) {
        this.margin = margin;
        return this;
    }

    /**
     * Set the padding for this paragraph.
     *
     * @param padding The padding to use
     * @return The paragraph.
     */
    public Paragraph with(Padding padding) {
        this.padding = padding;
        return this;
    }

    /**
     * The padding between the paragraph bounds and its text.
     * Apply it to the paragraph bounds to get the bounds the text is written into.
     */
    public Padding padding() {
        return padding;
    }

    /**
     * Specify what should happen to overflowing text.
     * @param overflow The overflow setting
     * @return The paragraph.
     */
    public Paragraph with(Overflow overflow) {
        this.overflow = java.util.Objects.requireNonNull(overflow, "overflow");
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
     *
     * @param height The inner height, i.e. without padding and margin.
     * @return The paragraph.
     */
    public Paragraph withInnerHeight(float height) {
        this.innerHeight = height;
        return this;
    }

    /**
     * Set the number of lines for which space is allocated, preserving the overflow mode.
     * CLIP and ELLIPSIS truncate to this limit; OVERFLOW draws beyond it.
     *
     * @param lineLimit The maximum number of allocated lines; must be positive.
     * @return The paragraph.
     */
    public Paragraph limit(int lineLimit) {
        if (lineLimit <= 0) {
            throw new IllegalArgumentException("Line limit must be greater than 0");
        }
        this.lineLimit = lineLimit;
        return this;
    }

    @Override
    public float render(Document document, Bounds viewPort) {
        float calculatedHeight = innerHeight(viewPort) + renderingHints().getExtraPadding().vertical();
        applyDecorators(document, viewPort.apply(margin).height(calculatedHeight));

        var bounds = effectiveBounds(viewPort, calculatedHeight);
        float actualHeight = textWriter.write(document.getContentStream(), bounds, text, align, vAlign, lineLimit, overflow);

        if (innerHeight > HEIGHT_UNDEFINED) {
            return innerHeight + margin.vertical();
        }
        if (overflow == Overflow.OVERFLOW) {
            return calculatedHeight + margin.vertical();
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
        return textWriter.calculateHeight(text, viewPort.apply(margin).apply(padding), lineLimit) + padding.vertical();
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
