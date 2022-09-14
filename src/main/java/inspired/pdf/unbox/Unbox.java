package inspired.pdf.unbox;

import inspired.pdf.unbox.decorators.BackgroundDecorator;
import inspired.pdf.unbox.elements.Column;
import inspired.pdf.unbox.elements.Paragraph;
import inspired.pdf.unbox.elements.Row;

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

    public static Row row() {
        return new Row();
    }

    public static Column column() {
        return new Column();
    }

}
