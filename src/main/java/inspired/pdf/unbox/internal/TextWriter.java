package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;
import java.util.List;

/**
 * Utility to render text that may span over several lines.
 */
public class TextWriter {

    public static final float CORRECTION_FACTOR = 0.25f;

    private final Font font;

    private boolean overflow;

    /**
     * Create a new text writer with the given font.
     * @param font  The font to use
     */
    public TextWriter(Font font) {
        this.font = font;
    }

    /**
     * If overflow is true, the text will be written even if it does not fit the bounds.
     */
    public TextWriter withOverflow(boolean overflow) {
        this.overflow = true;
        return this;
    }

    public float calculateHeight(String text, Bounds viewPort, Integer lineLimit) {
        List<String> lines = chunk(text, viewPort.width());
        int count = Math.max(1, lines.size());
        if (lineLimit != null && count > lineLimit) {
            count = lineLimit;
        }
        return count * font.lineHeight();
    }

    public float calculateHeightVerticalText(String text) {
        return font.width(text);
    }

    public float write(PDPageContentStream contentStream, Bounds bounds, String text) {
        return write(contentStream, bounds, text, Align.LEFT, VAlign.TOP);
    }

    public float write(PDPageContentStream stream, Bounds bounds, String text, Align align) {
        return write(stream, bounds, text, align, VAlign.TOP);
    }

    public float write(PDPageContentStream stream, Bounds bounds, String text, Align align, VAlign vAlign) {
        return write(stream, bounds, text, align, vAlign, null);
    }

    public float write(PDPageContentStream stream, Bounds bounds, String text, Align align, VAlign vAlign, Integer lineLimit) {
        List<String> chunks = chunk(text, bounds.width());
        if (lineLimit != null && lineLimit > 0 && lineLimit < chunks.size()) {
            chunks = chunks.subList(0, lineLimit);
        }
        float y = offsetY(bounds, vAlign, chunks.size());
        return write(chunks, bounds, align, stream, y);
    }

    public float writeVerticalText(PDPageContentStream contentStream, Bounds bounds, String text) {
        float textLength = calculateHeightVerticalText(text);
        float correction = font.lineHeight() * CORRECTION_FACTOR;
        float offsetX = (bounds.width() + font.lineHeight() - correction) / 2f;
        float offsetY = (bounds.height() + textLength) / 2f;
        try {
            contentStream.beginText();
            contentStream.setNonStrokingColor(font.getColor());
            contentStream.setFont(font.getFont(), font.getSize());
            contentStream.newLineAtOffset(offsetX, offsetY);

            var matrix = Matrix.getRotateInstance(Math.toRadians(90), bounds.left() + offsetX, bounds.top() - offsetY);
            contentStream.setTextMatrix(matrix);
            contentStream.showText(text);
            contentStream.endText();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
        return textLength;
    }

    private float write(List<String> chunks, Bounds bounds, Align align, PDPageContentStream stream, float y) {
        if (chunks.isEmpty()) {
            return 0;
        }
        int index = 0;
        while (index < chunks.size() && enoughSpace(bounds, index)) {
            String chunk = chunks.get(index++);
            float x = offsetX(bounds, chunk, align);
            writeLine(stream, chunk, x, y);
            y -= font.lineHeight();
        }
        return index * font.lineHeight();
    }

    private boolean enoughSpace(final Bounds bounds, final int index) {
        // 0.99f is a correction factor to avoid text being cut off due to rounding differences
        float lineHeight = font.lineHeight() * 0.99f;
        return overflow || ((index + 1) * lineHeight <= bounds.height());
    }

    private List<String> chunk(String text, float maxWidth) {
        return new TextTokenizer(font).chunkMultiLine(text, maxWidth);
    }

    /**
     * @param contentStream The content stream to write to
     * @param text          The text to write
     * @param x             Lower left corner of the text
     * @param y             Lower left corner of the text
     */
    private void writeLine(PDPageContentStream contentStream, String text, float x, float y) {
        try {
            contentStream.beginText();
            contentStream.setNonStrokingColor(font.getColor());
            contentStream.setFont(font.getFont(), font.getSize());
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private float offsetX(Bounds bounds, String text, Align align) {
        float textWidth = font.width(text);
        return switch (align) {
            case LEFT -> bounds.left();
            case CENTER -> (int) (bounds.center() - textWidth / 2);
            case RIGHT -> (int) (bounds.left() + bounds.width() - textWidth);
        };
    }

    private float offsetY(Bounds bounds, VAlign vAlign, int numLines) {
        float lineHeight = font.lineHeight();
        float correction = lineHeight * CORRECTION_FACTOR;
        return switch (vAlign) {
            case TOP -> bounds.top() - lineHeight + correction;
            case MIDDLE ->
                bounds.top() - bounds.height() / 2f + numLines * lineHeight / 2f - lineHeight + correction;
            case BOTTOM ->
                bounds.top() - bounds.height() + numLines * lineHeight - lineHeight + correction;
        };
    }

}
