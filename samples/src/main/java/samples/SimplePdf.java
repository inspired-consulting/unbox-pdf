package samples;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.*;
import inspired.pdf.unbox.elements.Canvas;
import inspired.pdf.unbox.themes.UnboxTheme;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static inspired.pdf.unbox.Unbox.*;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.*;

public class SimplePdf {

    public static void main(String[] args) throws IOException {
        LinearPDFWriter writer = new LinearPDFWriter();

        // add a paragraph
        writer.render(paragraph("Hello, World!", helvetica_bold(12)));

        // add a row with several paragraphs
        writer.render(row()
                .with(Margin.of(10,0))
                .with(Padding.of(10))
                .add(paragraph("Col 1").with(background(NEON_GREEN)))
                .add(paragraph("Col 2").align(Align.CENTER).with(background(GRAY_200)))
                .add(paragraph("Col 3").align(Align.RIGHT).with(background(RED_ORANGE.brighter())))
                .with(background(GRAY_100))
        );

        // Add paragraph with background
        writer.render(paragraph("Hello, Again!")
                .withHeight(100)
                .align(Align.RIGHT, VAlign.BOTTOM)
                .with(Margin.left(100))
                .with(Padding.of(15))
                .with(border(2, UnboxTheme.GREEN))
                .with(background(NEON_GREEN)));

        // Add a table with fixed column model
        TableModel tableModel = new TableModel()
                .add("Article", 2f)
                .add("Size")
                .add("Price", Align.RIGHT);
        Table table = new FixedColumnsTable(tableModel)
                .with(Margin.of(10))
                .with(border(1, GRAY_500));
        table.addHeader(TableRow.header(tableModel, helvetica_bold(8)).with(background(GRAY_100)));
        table.addRow()
                .addCell("SmartTV 200+")
                .addCell("55", Align.LEFT, helvetica_bold(8, RED_ORANGE))
                .addCell("200.12 EUR");
        table.addRow().withCells("SmartPhone", "5,5", "320.00 EUR");
        writer.render(table);

        // render some graphics on a canvas
        writer.render(new Canvas(100) {
            @Override
            public void paint(PDPageContentStream contentStream, Bounds viewPort) throws IOException {
                drawCircle(contentStream, viewPort.left() + 100, viewPort.top() -50, 50, NEON_GREEN);
            }
        }.with(Margin.bottom(10)));

        writer.render(paragraph("Done!", helvetica_bold(12), Align.CENTER)
                .with(Padding.of(20))
                .with(border(2, RED_ORANGE))
                .with(background(GRAY_100)));

        PDDocument document = writer.finish();
        document.save("./samples/out/SimplePdf.pdf");
        document.close();
    }

}
