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

    protected void finish(LinearPDFWriter writer, PDDocument document, PDPageTree allPages) throws IOException {
        int pageNum = 1;
        for (PDPage page : allPages) {
            PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true,true);
            finish(writer, contentStream, pageNum, allPages.getCount());
            contentStream.close();
            pageNum++;
        }
    }

    @Override
    public void onFinished(LinearPDFWriter writer) {
        PDDocument document = writer.getDocument();
        PDPageTree allPages = document.getDocumentCatalog().getPages();
        try {
            finish(writer, document, allPages);
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

}
