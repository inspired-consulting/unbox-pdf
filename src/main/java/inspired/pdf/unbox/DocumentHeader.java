package inspired.pdf.unbox;

import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.PdfElement;
import inspired.pdf.unbox.elements.internal.AbstractDecoratable;
import inspired.pdf.unbox.internal.PdfEventListener;

/**
 * Header rendered on each page inside the dedicated header place.
 */
public class DocumentHeader extends AbstractDecoratable implements PdfEventListener {

    private final PdfElement content;

    /**
     * Create new header
     * @param content The content of the header.
     */
    public DocumentHeader(PdfElement content) {
        this.content = content;
    }

    @Override
    public DocumentHeader with(Decorator decorator) {
        super.with(decorator);
        return this;
    }

    @Override
    public void onNewPage(Document document) {
        Bounds headerBounds = document.getHeaderBounds();
        applyDecorators(document, headerBounds);
        if (content != null) {
            content.render(document, headerBounds);
        }
    }

}
