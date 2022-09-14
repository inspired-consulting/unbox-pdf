package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfEventListener;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;

import java.io.IOException;

public abstract class DocumentFinisher implements PdfEventListener {

    public abstract void finish(DocumentContext context, PDPageContentStream contentStream, int pageNumber, int pageCount);

    protected void finish(Document document, PDDocument pdf, PDPageTree allPages) throws IOException {
        int pageNum = 1;
        for (PDPage page : allPages) {
            PDPageContentStream contentStream = new PDPageContentStream(pdf, page, PDPageContentStream.AppendMode.APPEND, true,true);
            finish(document, contentStream, pageNum, allPages.getCount());
            contentStream.close();
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
