package samples;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.*;
import inspired.pdf.unbox.themes.UnboxTheme;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.*;
import java.io.IOException;

import static inspired.pdf.unbox.decorators.BackgroundDecorator.background;
import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static inspired.pdf.unbox.elements.Paragraph.paragraph;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.*;

public class SimplePdf {

    public static void main(String[] args) throws IOException {
        LinearPDFWriter writer = new LinearPDFWriter();

        writer.render(new Paragraph("Hello, World!", Align.CENTER)
                .with(Padding.of(20))
                .with(border(2, KELLY_GREEN))
                .with(background(GRAY_200)));

        writer.render(new Row().with(Margin.of(10,0))
                .add(paragraph("Col 1").with(background(NEON_GREEN)))
                .add(paragraph("Col 2", Align.CENTER).with(background(Color.ORANGE)))
                .add(paragraph("Col 3", Align.RIGHT).with(background(GRAY_100)))
        );

        writer.render(paragraph("Hello, Again!", Align.LEFT)
                .with(Margin.top(20))
                .with(Padding.of(10, 30))
                .with(border(2, UnboxTheme.GREEN))
                .with(background(NEON_GREEN)));

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

        writer.render(paragraph("Done!", Align.RIGHT, helvetica_bold(12))
                .with(Padding.of(20))
                .with(Margin.right(50))
                .with(border(2, RED_ORANGE))
                .with(background(GRAY_100)));

        PDDocument document = writer.finish();
        document.save("./samples/out/SimplePdf.pdf");
        document.close();
    }

}
