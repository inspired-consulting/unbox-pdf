package inspired.pdf.unbox;

import inspired.pdf.unbox.elements.PdfElement;
import inspired.pdf.unbox.internal.PdfEventListener;

/**
 * Footer rendered on each page.
 */
public class DocumentFooter implements PdfEventListener {

    private PdfElement content;

    public DocumentFooter(PdfElement content) {
        this.content = content;
    }

    @Override
    public void onNewPage(Document document) {
        if (content != null) {
            content.render(document, document.getFooterBounds());
        }
    }

}
