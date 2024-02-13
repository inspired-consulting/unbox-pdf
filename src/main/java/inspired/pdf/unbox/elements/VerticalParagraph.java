package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.internal.SimpleFont;

/**
 * A paragraph that renders text vertically.
 * Caution: This Paragraph will render only one line of text and might bust your layout if this line gets too long.
 * Works best with Stretch-Layouts (rowStretch() or HorizontalStretchLayout)
 */
public class VerticalParagraph extends Paragraph  {

    public VerticalParagraph(String text) {
        this(text, SimpleFont.helvetica(8));
    }

    public VerticalParagraph(String text, Font font) {
        super(text, font);
    }

    @Override
    public float render(Document document, Bounds viewPort)  {
        float calculatedHeight = innerHeight(viewPort) + renderingHints().getExtraPadding().vertical();
        applyDecorators(document, viewPort.apply(margin).height(calculatedHeight));

        var bounds = effectiveBounds(viewPort, calculatedHeight);
        float actualHeight = textWriter.writeVerticalText(document.getContentStream(), bounds, text);

        if (innerHeight > HEIGHT_UNDEFINED) {
            return innerHeight + margin.vertical();
        }
        return actualHeight + padding.vertical() + margin.vertical() + renderingHints().getExtraPadding().vertical();
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        if (innerHeight > HEIGHT_UNDEFINED) {
            return innerHeight;
        }
        return textWriter.calculateHeightVerticalText(text) + padding.vertical();
    }

    @Override
    public String toString() {
        return "VerticalParagraph['" + text + "']";
    }
}
