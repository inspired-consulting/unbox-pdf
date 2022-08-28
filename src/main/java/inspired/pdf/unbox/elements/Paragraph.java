package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.internal.SimpleFont;
import inspired.pdf.unbox.internal.TextWriter;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class Paragraph implements PdfElement {

    private final TextWriter textWriter;
    private final String text;

    private Align align = Align.LEFT;
    private VAlign vAlign = VAlign.TOP;

    private Padding padding = Padding.of(4);
    private Margin margin = Margin.none();

    public static Paragraph paragraph(String text) {
        return new Paragraph(text);
    }

    public static Paragraph paragraph(String text, Font font) {
        return new Paragraph(text, font);
    }

    public static Paragraph paragraph(String text, Align align) {
        return new Paragraph(text).align(align);
    }

    public static Paragraph paragraph(String text, Font font, Align align) {
        return new Paragraph(text, font).align(align);
    }

    public Paragraph(String text) {
        this(text, new SimpleFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8));
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

    @Override
    public float render(LinearPDFWriter writer, Bounds viewPort)  {
        Bounds bounds = viewPort.apply(margin).apply(padding);
        float height = textWriter.write(writer.getContentStream(), bounds, text, align, vAlign);
        return height + margin.vertical() + padding.vertical();
    }

    @Override
    public Margin margin() {
        return margin;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return textWriter.calculateHeight(text, viewPort) + padding.vertical();
    }

}
