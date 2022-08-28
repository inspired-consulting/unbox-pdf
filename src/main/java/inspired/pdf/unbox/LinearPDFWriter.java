package inspired.pdf.unbox;

import inspired.pdf.unbox.elements.PdfElement;
import inspired.pdf.unbox.internal.PdfEventListener;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility to write linear PDFs from top to bottom. Supports to break page.
 */
public class LinearPDFWriter {

    private final Orientation orientation;

    private final Margin margin;
    private final Padding padding;

    private final PDDocument document;

    private PDPage page;
    private PDPageContentStream contentStream;

    private final List<PdfEventListener> eventListeners = new ArrayList<>();

    private Position position = new Position(0,0);

    public LinearPDFWriter() {
        this(Orientation.PORTRAIT);
    }

    public LinearPDFWriter(Orientation orientation) {
        this(orientation, Margin.of(20), Padding.of(32, 0, 14));
    }

    public LinearPDFWriter(Orientation orientation, Margin margin, Padding padding) {
        this.document = new PDDocument();
        this.orientation = orientation;
        this.margin = margin;
        this.padding = padding;
    }

    public LinearPDFWriter add(PdfEventListener listener) {
        this.eventListeners.add(listener);
        return this;
    }

    /**
     * Get current position of writer.
     * @return The current position.
     */
    public float getPosition() {
        getPage();
        return position.y();
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Margin getMargin() {
        return margin;
    }

    public Padding getPadding() {
        return padding;
    }

    public Bounds getPageBounds() {
        PDRectangle mediaBox = getPage().getMediaBox();
        return new Bounds(0, mediaBox.getHeight(), mediaBox.getWidth(), mediaBox.getHeight());
    }

    public Bounds getContentBounds() {
        return getPageBounds().apply(margin);
    }

    public Bounds getViewPort() {
        return getContentBounds().apply(padding);
    }

    public Bounds getCurrentViewPort() {
        return getViewPort().top(position.y()).height(getSpaceLeftOnPage());
    }

    public float getSpaceLeftOnPage() {
        return position.y() - margin.bottom() - padding.bottom();
    }

    public void render(PdfElement element) {
        float height = element.innerHeight(getCurrentViewPort());
        forwardPageIfNeeded(height);
        float forward = element.render(this, getCurrentViewPort());
        forward(forward);
    }

    public void forward(float amount) {
        getPage();
        position = position.moveDown(amount);
    }

    public PDPage addPage() {
        return addPage(orientation);
    }

    public PDPage addPage(Orientation orientation) {
        closeContentStream();
        PDRectangle rectangle = PDRectangle.A4;

        if (Orientation.LANDSCAPE.equals(orientation)) {
            page = new PDPage(new PDRectangle(rectangle.getHeight(), rectangle.getWidth()));
        } else {
            page = new PDPage(new PDRectangle(rectangle.getWidth(), rectangle.getHeight()));
        }

        document.addPage( page );
        float offsetY = page.getMediaBox().getHeight() - margin.top() - padding.top();
        position = new Position(position.x(), offsetY);
        onNewPage(page);
        return page;
    }

    public void forwardPageIfNeeded(float spaceNeeded) {
        if (getSpaceLeftOnPage() < spaceNeeded) {
            addPage();
        }
    }

    public PDDocument finish() {
        // ensure that at least one page has been written.
        getPage();
        closeContentStream();
        onFinish();
        return document;
    }

    public PDPageContentStream getContentStream() {
        if (contentStream == null) {
            try {
                contentStream = new PDPageContentStream(document, getPage());
            } catch (IOException e) {
                throw new PdfUnboxException(e);
            }
        }
        return contentStream;
    }

    public PDPage getPage() {
        if (page == null) {
            addPage();
        }
        return page;
    }

    public PDDocument getDocument() {
        return document;
    }

    // internal

    protected void onNewPage(PDPage page) {
        for (PdfEventListener listener : eventListeners) {
            listener.onNewPage(this);
        }
    }

    protected void onFinish() {
        for (PdfEventListener listener : eventListeners) {
            listener.onFinished(this);
        }
    }

    private void closeContentStream() {
        if (contentStream != null) {
            try {
                contentStream.close();
            } catch (IOException e) {
                throw new PdfUnboxException(e);
            }
            contentStream = null;
        }
    }


}
