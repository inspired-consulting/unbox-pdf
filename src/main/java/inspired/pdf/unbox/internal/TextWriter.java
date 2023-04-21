package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.*;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.List;
import java.util.Arrays;

/**
 * Utility to render text that may span over several lines.
 */
public class TextWriter {

    public static final float CORRECTION_FACTOR = 0.25f;

    private final Font font;

    public TextWriter(Font font) {
        this.font = font;
    }

    public float calculateHeight(String text, Bounds viewPort, Integer lineLimit) {
        List<String> lines = chunk(text, viewPort.width());
        int count = Math.max(1, lines.size());
        if(lineLimit != null && count > lineLimit) {
            count = lineLimit;
        }
        return count * font.lineHeight();
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
        if(lineLimit != null && lineLimit > 0 && lineLimit < chunks.size()) {
            chunks = chunks.subList(0, lineLimit);
        }
        float y = offsetY(bounds, vAlign, chunks.size());
        return write(chunks, bounds, align, stream, y);
    }

    private float write(List<String> chunks, Bounds bounds, Align align, PDPageContentStream stream, float y) {
        for (String chunk : chunks) {
            float x = offsetX(bounds, chunk, align);
            writeLine(stream, chunk, x, y);
            y -= font.lineHeight();
        }
        int count = Math.max(1, chunks.size());
        return count * font.lineHeight();
    }

    private List<String> chunk(String text, float maxWidth)  {
        return new TextTokenizer(font).chunk(text, maxWidth);
    }

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
        float correction = font.lineHeight() * CORRECTION_FACTOR;
        return switch (vAlign) {
            case TOP -> bounds.top() - font.lineHeight() + correction;
            case MIDDLE -> bounds.top() - bounds.height() / 2f + numLines * font.lineHeight() / 2f - font.lineHeight() + correction;
            case BOTTOM -> bounds.top() - bounds.height() + numLines * font.lineHeight() - font.lineHeight() + correction;
        };
    }
}
