package samples;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.internal.TextWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

import static inspired.pdf.unbox.Unbox.*;
import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_200;

public class MultiPagePdf {

    public static void main(String[] args) throws IOException {
        LinearPDFWriter document = new LinearPDFWriter(Orientation.LANDSCAPE, Margin.of(40), Padding.of(30,0));

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

        PDDocument pdf = document.finish();
        pdf.save("./samples/out/MultiPagePdf.pdf");
        pdf.close();

    }

}
