package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.Unbox;
import inspired.pdf.unbox.base.TableModel;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static inspired.pdf.unbox.Unbox.background;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_100;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that containers and container cells measure their children at the same
 * width they render them with. A wrong measurement does not lose text, because the
 * children size themselves while rendering, but it misplaces page breaks and draws
 * container decorations with the wrong height.
 */
class ContainerMeasurementTest {

    private static final String LONG_TEXT = "word ".repeat(30) + "END";
    private static final Margin MARGIN = Margin.of(0, 150, 0, 250);
    private static final Padding PADDING = Padding.of(5, 40);

    @Test
    void measuredHeightMatchesRenderedHeightForAllLayouts() throws IOException {
        try (Document document = new Document()) {
            Bounds viewPort = document.getCurrentViewPort();
            for (Container container : new Container[] {
                    Unbox.row().with(MARGIN).with(PADDING).add(new Paragraph(LONG_TEXT)),
                    Unbox.column().with(MARGIN).with(PADDING).add(new Paragraph(LONG_TEXT)),
                    Unbox.rowStretch().with(MARGIN).with(PADDING).add(new Paragraph(LONG_TEXT)),
                    Unbox.columnStretch().with(MARGIN).with(PADDING).add(new Paragraph(LONG_TEXT))}) {
                float measured = container.outerHeight(viewPort);
                float rendered = container.render(document, viewPort);
                assertEquals(measured, rendered, 0.01f, container.toString());
            }
        }
    }

    @Test
    void containerCellMeasuredHeightMatchesRenderedHeight() throws IOException {
        try (Document document = new Document()) {
            Bounds cellBounds = document.getCurrentViewPort().width(250);
            ContainerCell cell = new ContainerCell(Container.Layout.ROWS)
                    .with(Padding.of(5, 60))
                    .add(new Paragraph(LONG_TEXT));

            float measured = cell.innerHeight(cellBounds);
            float rendered = cell.render(document, cellBounds);
            assertEquals(measured, rendered, 0.01f);
        }
    }

    @Test
    void backgroundCoversRenderedContentOfColumnWithMargin() throws IOException {
        try (Document document = new Document()) {
            Bounds viewPort = document.getCurrentViewPort();
            Container column = Unbox.column().with(MARGIN).with(PADDING)
                    .with(background(GRAY_100))
                    .add(new Paragraph(LONG_TEXT));

            float rendered = column.render(document, viewPort);

            assertEquals(rendered - MARGIN.vertical(), rectangleHeight(document), 0.01f,
                    "background height should equal the rendered content height");
        }
    }

    @Test
    void tableRowWithContainerCellRendersCompleteText() throws IOException {
        FixedColumnsTable table = new FixedColumnsTable(TableModel.of(1f, 1f));
        table.addRow()
                .addCell(new ContainerCell(Container.Layout.ROWS).with(Padding.of(5, 60)).add(new Paragraph(LONG_TEXT)))
                .addCell("other");

        String text = render(table);
        assertTrue(text.contains("END"), text);
        assertEquals(30, text.lines().flatMap(l -> java.util.Arrays.stream(l.split(" "))).filter("word"::equals).count());
    }

    // -- helpers

    private static String render(PdfElement element) throws IOException {
        try (Document document = new Document()) {
            document.render(element);
            try (PDDocument pdf = document.finish()) {
                PDFTextStripper stripper = new PDFTextStripper();
                stripper.setLineSeparator("\n");
                return stripper.getText(pdf).strip();
            }
        }
    }

    /** The height operand of the single rectangle operator on the first page. */
    private static float rectangleHeight(Document document) throws IOException {
        PDFStreamParser parser = new PDFStreamParser(document.finish().getPage(0));
        parser.parse();
        List<Object> tokens = parser.getTokens();
        for (int i = 4; i < tokens.size(); i++) {
            if (tokens.get(i) instanceof Operator operator && operator.getName().equals("re")) {
                return ((COSNumber) tokens.get(i - 1)).floatValue();
            }
        }
        throw new AssertionError("no rectangle drawn");
    }
}
