package samples;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.FixedColumnsTable;
import inspired.pdf.unbox.internal.TextWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

import static inspired.pdf.unbox.Unbox.*;
import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static inspired.pdf.unbox.elements.TableRow.header;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.*;

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


        var table = new FixedColumnsTable(model).with(border(0.5f, GRAY_700));
        table.addHeader(header(model, helvetica_bold(10)).with(background(GRAY_100)));
        for (int i = 0; i < 200; i++) {
            table.addRow()
                    .addCell("Name " + i)
                    .addCell("Label " + i)
                    .addCell(Integer.toString(i));
        }
        document.render(table);

        var pdf = document.finish();
        pdf.save("./samples/out/MultiPagePdf.pdf");
        pdf.close();

    }

}
