package samples;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.FixedColumnsTable;
import inspired.pdf.unbox.elements.TableRow;
import inspired.pdf.unbox.internal.TextWriter;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

import static inspired.pdf.unbox.Unbox.background;
import static inspired.pdf.unbox.Unbox.paragraph;
import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.*;

/**
 * Demonstrates repeated page and table headers, page numbers, and a non-repeating
 * table header that must move with its first row when starting near a page end.
 */
public class MultiPagePdf {

    public static void main(String[] args) throws IOException {
        Document document = new Document(Orientation.LANDSCAPE, Margin.of(40), Padding.of(30,0));

        document.add(new DocumentHeader(
                paragraph("This header is rendered on each page", helvetica_bold(12))
                        .align(Align.CENTER, VAlign.MIDDLE)
                        .with(border(1, GRAY_200)))
                );

        document.add(new DocumentFooter(
                paragraph("This footer is rendered on each page", helvetica_bold(12))
                        .align(Align.CENTER, VAlign.MIDDLE)
                        .with(border(1, GRAY_200)))
        );

        document.add(new DocumentFinisher() {
            @Override
            public void finish(DocumentContext context, PDPageContentStream contentStream, int pageNumber, int pageCount) {
                String pageInfo = "Page " + pageNumber + " of " + pageCount;
                Bounds bounds = context.getFooterBounds().apply(Padding.right(5));
                new TextWriter(helvetica_bold(8)).write(contentStream, bounds, pageInfo, Align.RIGHT, VAlign.MIDDLE);
            }
        });

        // add a paragraph
        document.render(paragraph("Hello, World!", helvetica_bold(12)).with(Margin.of(10,0)));
        document.addPage();

        var model = new TableModel()
                .add("Name", 1f)
                .add("Label", 1f)
                .add("Number", 1f, Align.RIGHT);

        var table = new FixedColumnsTable(model)
                .withHeader(helvetica_bold(10), background(GRAY_100))
                .with(border(0.5f, GRAY_700));
        for (int i = 0; i < 200; i++) {
            table.addRow()
                    .addCell("Name " + i)
                    .addCell("Label " + i)
                    .addCell(Integer.toString(i));
        }
        document.render(table);

        nonRepeatingHeaderAtPageEnd(document);

        var pdf = document.finish();
        pdf.save("./samples/out/MultiPagePdf.pdf");
        pdf.close();

    }

    private static void nonRepeatingHeaderAtPageEnd(Document document) {
        document.addPage();
        document.render(paragraph("Pagination case: a table header shown only once", helvetica_bold(14))
                .with(Margin.bottom(12)));
        document.render(paragraph("The space below is intentional: the table starts near the page bottom. "
                + "Its header fits here, but its first row does not. Both must move to the next page. "
                + "The shaded 'One-time table header' must appear there once and be absent on continuation pages.")
                .with(Margin.bottom(12)));

        TableModel model = new TableModel()
                .add("One-time table header", 2f)
                .add("Value", 1f);
        TableRow header = TableRow.header(model, helvetica_bold(10), background(GRAY_100));
        FixedColumnsTable table = new FixedColumnsTable(model)
                .with(border(0.5f, GRAY_700));
        table.addHeader(header);
        table.repeatHeader(false);
        for (int i = 1; i <= 50; i++) {
            table.addRow("Non-repeating row " + i, "Value " + i);
        }

        // Leave room for the header alone, forcing the initial keep-together page break.
        float headerHeight = header.innerHeight(document.getCurrentViewPort());
        document.forward(document.getSpaceLeftOnPage() - headerHeight - 2);
        document.render(table);
    }

}
