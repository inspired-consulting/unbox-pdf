package inspired.pdf.unbox;

import inspired.pdf.unbox.elements.PdfElement;
import inspired.pdf.unbox.internal.PdfEventListener;

/**
 * Header rendered on each page.
 */
public class DocumentHeader implements PdfEventListener {

    private final PdfElement content;

    public DocumentHeader(PdfElement content) {
        this.content = content;
    }

    @Override
    public void onNewPage(LinearPDFWriter writer) {
        content.render(writer, writer.getHeaderBounds());
    }

}
