package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfEventListener;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;

import java.io.IOException;

/**
 * Appends content to each page after rendering, with access to the final page count.
 * Each callback's content stream is closed even if the callback fails.
 */
public abstract class DocumentFinisher implements PdfEventListener {

    public abstract void finish(DocumentContext context, PDPageContentStream contentStream, int pageNumber, int pageCount);

    protected void finish(Document document, PDDocument pdf, PDPageTree allPages) throws IOException {
        int pageNum = 1;
        for (PDPage page : allPages) {
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    pdf, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                finish(document, contentStream, pageNum, allPages.getCount());
            }
            pageNum++;
        }
    }

    @Override
    public void onFinished(Document document) {
        PDDocument pdf = document.getDocument();
        PDPageTree allPages = pdf.getDocumentCatalog().getPages();
        try {
            finish(document, pdf, allPages);
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

}
