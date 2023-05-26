package inspired.pdf.unbox.elements;

import java.io.IOException;
import java.util.List;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.elements.internal.AbstractTableCell;
import inspired.pdf.unbox.internal.PdfUnboxException;
import inspired.pdf.unbox.internal.SimpleFont;
import inspired.pdf.unbox.internal.TextTokenizer;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/**
 * Cell containing text.
 */
public class TextCell extends AbstractTableCell {

    private final static int HEIGHT_CORRECTION = 2;

    private final Font font;

    private String text;

    public TextCell(String text) {
        this(text, null);
    }

    public TextCell(String text, Font font) {
        this(text, Align.LEFT, font);
    }

    public TextCell(String text, Align align, Font font) {
        this.font = font != null ? font : SimpleFont.helvetica(8);
        this.text = text != null ? text : "";
        this.align = align;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        float lineHeight = font.lineHeight();
        List<String> lines = chunk(viewPort.width() - padding().horizontal());
        int count = Math.max(1, lines.size());
        return count * lineHeight + padding().vertical() + HEIGHT_CORRECTION;
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            value = "";
        }
        this.text = value.toString();
    }

    @Override
    protected float renderCell(Document document, Bounds bounds) {
        applyDecorators(document, bounds);
        try {
            float lineHeight = font.lineHeight();
            PDPageContentStream contentStream = document.getContentStream();
            List<String> lines = chunk( bounds.width() - padding().horizontal());
            float y =  bounds.top() - lineHeight - padding().top() + 1;
            Align align = coalesce(this.align, Align.LEFT);
            for (String line : lines) {
                float startX = startX(bounds, align, line);
                contentStream.beginText();
                contentStream.setFont(font.getFont(), font.getSize());
                contentStream.setNonStrokingColor(font.getColor());
                contentStream.newLineAtOffset(startX, y);
                contentStream.showText(line);
                contentStream.endText();
                y -= lineHeight;
            }
            int count = Math.max(1, lines.size());
            return count * lineHeight + padding().vertical();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private List<String> chunk(float maxWidth) {
        return new TextTokenizer(font).chunkMultiLine(text, maxWidth);
    }

    private float startX(Bounds bounds, Align align, String text)  {
        if (text == null) {
            return bounds.left();
        }
        float textWidth = font.width(text);
        return switch (align) {
            case LEFT -> bounds.left() + padding().left();
            case CENTER -> (int) (bounds.left() + padding().horizontalShift() + (bounds.width() - textWidth) / 2);
            case RIGHT -> (int) (bounds.left() + bounds.width() - padding().right() - textWidth);
        };
    }

    private Align coalesce(Align... options) {
        for (Align option : options) {
            if (option != null) {
                return option;
            }
        }
        return null;
    }

}
