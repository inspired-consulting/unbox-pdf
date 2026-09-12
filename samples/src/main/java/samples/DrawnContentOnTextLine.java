package samples;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.VAlign;
import inspired.pdf.unbox.base.SimpleColumnModel;
import inspired.pdf.unbox.elements.Canvas;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.Paragraph;
import inspired.pdf.unbox.internal.SimpleFont;
import inspired.pdf.unbox.internal.TextWriter;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Path;

import static inspired.pdf.unbox.Unbox.paragraph;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_300;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_600;
import static inspired.pdf.unbox.themes.UnboxTheme.KELLY_GREEN;
import static inspired.pdf.unbox.themes.UnboxTheme.RED_ORANGE;

/**
 * Demonstrates how to align drawn shapes with a text line. A bar chart cell is drawn
 * next to its percentage, and a colored legend dot sits in front of its label. Both
 * use the paragraph padding, the text baseline, and the cap height of the font to
 * find the vertical center of the letters, instead of copying internal constants.
 */
public class DrawnContentOnTextLine {

    private static final Font LABEL = helvetica(9);
    private static final Font VALUE = helvetica_bold(9);
    private static final Font LEGEND = new SimpleFont(PDType1Font.HELVETICA, 9, GRAY_600);

    public static void main(String[] args) throws IOException {
        try (Document document = new Document()) {
            document.render(paragraph("Load factors per switchboard", helvetica_bold(12)).with(Margin.bottom(6)));

            document.render(loadRow("Main switchboard", "2 × 1,250 kVA", 0.82f, RED_ORANGE));
            document.render(loadRow("Emergency switchboard", "400 kVA", 0.35f, KELLY_GREEN));
            document.render(loadRow("Galley distribution", "160 kVA", 0.58f, KELLY_GREEN));

            document.render(paragraph("").with(Margin.bottom(10)));
            document.render(legendRow(RED_ORANGE, "above 75 % of rated load"));
            document.render(legendRow(KELLY_GREEN, "within rated load"));

            document.finishTo(Path.of("samples", "out", "DrawnContentOnTextLine.pdf"));
        }
    }

    /**
     * Label with a muted rating in one paragraph of two runs, the percentage, and a bar that
     * is drawn on the same line as the percentage.
     */
    private static Container loadRow(String label, String rating, float share, Color color) {
        Paragraph value = paragraph(String.format("%.0f %%", share * 100), VALUE, Align.RIGHT);
        return Container.withColumnLayout(SimpleColumnModel.of(3f, 1f, 4f))
                .add(paragraph(label, LABEL).add("  " + rating, LEGEND))
                .add(value)
                .add(bar(value, share, color));
    }

    /** A bar whose vertical center is the center of the capital letters next to it. */
    private static Canvas bar(Paragraph reference, float share, Color color) {
        float height = VALUE.capHeight();
        return new Canvas(oneLineHeight(reference, VALUE)) {
            @Override
            public void paint(PDPageContentStream contentStream, Bounds viewPort) throws IOException {
                float center = capCenter(reference, VALUE, viewPort);
                Bounds track = viewPort.apply(reference.padding()).apply(Margin.left(6));
                contentStream.setNonStrokingColor(GRAY_300);
                contentStream.addRect(track.left(), center - height / 2, track.width(), height);
                contentStream.fill();
                contentStream.setNonStrokingColor(color);
                contentStream.addRect(track.left(), center - height / 2, track.width() * share, height);
                contentStream.fill();
            }
        };
    }

    /** A colored dot in front of a label, centered on the label's capital letters. */
    private static Container legendRow(Color color, String text) {
        Paragraph label = paragraph(text, LEGEND);
        Canvas dot = new Canvas(oneLineHeight(label, LEGEND)) {
            @Override
            public void paint(PDPageContentStream contentStream, Bounds viewPort) throws IOException {
                float radius = LABEL.capHeight() / 2;
                float center = capCenter(label, LEGEND, viewPort);
                drawCircle(contentStream, viewPort.left() + radius, center, radius, color);
            }
        };
        return Container.withColumnLayout(SimpleColumnModel.of(0.4f, 10f)).add(dot).add(label);
    }

    /** The height a single-line paragraph occupies: one line plus its padding. */
    private static float oneLineHeight(Paragraph paragraph, Font font) {
        return font.lineHeight() + paragraph.padding().vertical();
    }

    /**
     * The vertical center of the capital letters of the paragraph's first line, when the
     * paragraph is rendered in the given bounds: the baseline plus half the cap height.
     */
    private static float capCenter(Paragraph paragraph, Font font, Bounds bounds) {
        Bounds textBounds = bounds.apply(paragraph.padding());
        float baseline = new TextWriter(font).baseline(textBounds, VAlign.TOP, 1);
        return baseline + font.capHeight() / 2;
    }
}
