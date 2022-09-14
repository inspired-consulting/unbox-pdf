package inspired.pdf.unbox;

import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.PdfElement;
import inspired.pdf.unbox.elements.internal.AbstractDecoratable;
import inspired.pdf.unbox.internal.PdfEventListener;

/**
 * Footer rendered on each page.
 */
public class DocumentFooter extends AbstractDecoratable implements PdfEventListener {

    private final PdfElement content;

    /**
     * Create new footer.
     * @param content The content of the footer.
     */
    public DocumentFooter(PdfElement content) {
        this.content = content;
    }

    @Override
    public DocumentFooter with(Decorator decorator) {
        super.with(decorator);
        return this;
    }

    @Override
    public void onNewPage(Document document) {
        Bounds footerBounds = document.getFooterBounds();
        applyDecorators(document, footerBounds);
        if (content != null) {
            content.render(document, footerBounds);
        }
    }

}
