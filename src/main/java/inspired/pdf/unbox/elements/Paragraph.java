package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.internal.SimpleFont;
import inspired.pdf.unbox.internal.TextWriter;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class Paragraph implements PdfElement {

    private final TextWriter textWriter;
    private final String text;
    private final Align align;

    private Padding padding = Padding.of(4);
    private Margin margin = Margin.none();

    public static Paragraph paragraph(String text) {
        return new Paragraph(text);
    }

    public static Paragraph paragraph(String text, Align align) {
        return new Paragraph(text, align);
    }

    public static Paragraph paragraph(String text, Align align, Font font) {
        return new Paragraph(text, align, font);
    }

    public Paragraph(String text) {
        this(text, Align.LEFT);
    }

    public Paragraph(String text, Font font) {
        this(text, Align.LEFT, font);
    }

    public Paragraph(String text, Align align) {
        this(text, align, new SimpleFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8));
    }

    public Paragraph(String text, Align align, Font font) {
        this.textWriter = new TextWriter(font);
        this.text = text;
        this.align = align;
    }

    public Paragraph with(Margin margin) {
        this.margin = margin;
        return this;
    }

    public Paragraph with(Padding padding) {
        this.padding = padding;
        return this;
    }

    @Override
    public float render(LinearPDFWriter writer, Bounds viewPort)  {
        Bounds bounds = viewPort.apply(margin).apply(padding);
        float height = textWriter.write(writer.getContentStream(), bounds, text, align);
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
