package inspired.pdf.unbox;

import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.base.SimpleColumnModel;
import inspired.pdf.unbox.decorators.BackgroundDecorator;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.Paragraph;
import inspired.pdf.unbox.elements.PdfElement;
import inspired.pdf.unbox.elements.internal.AbstractPdfElement;

import java.awt.*;

/**
 * Entry point for Unbox toolbox providing helpers to create PDF elements.
 */
public class Unbox {

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

    public static BackgroundDecorator background(Color color) {
        return new BackgroundDecorator(color);
    }

    public static Container row() {
        return Container.withColumnLayout();
    }

    public static Container row(ColumnModel<?> model) {
        return Container.withColumnLayout(model);
    }

    public static Container row(float... columnSizes) {
        return Container.withColumnLayout(SimpleColumnModel.of(columnSizes));
    }

    public static Container column() {
        return Container.withRowLayout();
    }

    public static PdfElement empty() {
        return AbstractPdfElement.empty();
    }

}
