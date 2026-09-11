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
 * Creates PDFs by rendering elements from top to bottom with page-break support.
 * Owns the PDF and content stream; use try-with-resources and save the result of
 * {@link #finish()} before closing. Closing releases resources without finishing rendering.
 */
public class Document implements DocumentContext, AutoCloseable {

    private final Orientation orientation;

    private final Margin margin;
    private final Padding padding;

    private final PDDocument document;

    private PDPage page;
    private PDPageContentStream contentStream;
    private boolean closed;

    private final List<PdfEventListener> eventListeners = new ArrayList<>();

    private Position position = new Position(0,0);

    public Document() {
        this(Orientation.PORTRAIT);
    }

    public Document(Orientation orientation) {
        this(orientation, Margin.of(20), Padding.of(30, 0, 20));
    }

    public Document(Orientation orientation, Margin margin, Padding padding) {
        this.document = new PDDocument();
        this.orientation = orientation;
        this.margin = margin;
        this.padding = padding;
    }

    public Document add(PdfEventListener listener) {
        this.eventListeners.add(listener);
        return this;
    }

    /**
     * Get current position in document.
     * @return The current position.
     */
    public float getPosition() {
        getPage();
        return position.y();
    }

    public float getPositionY() {
        getPage();
        return position.y();
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public Margin getMargin() {
        return margin;
    }

    @Override
    public Padding getPadding() {
        return padding;
    }

    // bounds

    @Override
    public Bounds getPageBounds() {
        PDRectangle mediaBox = getPage().getMediaBox();
        return new Bounds(0, mediaBox.getHeight(), mediaBox.getWidth(), mediaBox.getHeight());
    }

    @Override
    public Bounds getViewPort() {
        return getContentBounds().apply(padding);
    }

    public Bounds getCurrentViewPort() {
        return getViewPort().top(position.y()).height(getSpaceLeftOnPage());
    }

    @Override
    public Bounds getHeaderBounds() {
        float headerHeight = padding.top();
        return getContentBounds().height(headerHeight);
    }

    @Override
    public Bounds getFooterBounds() {
        float footerHeight = padding.bottom();
        Bounds contentBounds = getContentBounds();
        return new Bounds(contentBounds.left(), contentBounds.bottom() + footerHeight,
                contentBounds.width(), footerHeight);
    }

    // util

    public float getSpaceLeftOnPage() {
        return position.y() - margin.bottom() - padding.bottom();
    }

    public void render(PdfElement element) {
        // The top margin is occupied before the content; the bottom margin may be
        // absorbed by the page end, so it does not count towards the fit check.
        float height = element.innerHeight(getCurrentViewPort()) + element.margin().top();
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
        ensureOpen();
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

    /**
     * Complete rendering and return the PDF for saving. The PDF remains open until
     * this document or the returned PDF is closed.
     * @return The completed PDF.
     */
    public PDDocument finish() {
        // ensure that at least one page has been written.
        getPage();
        closeContentStream();
        onFinish();
        return document;
    }

    public PDPageContentStream getContentStream() {
        ensureOpen();
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
        ensureOpen();
        if (page == null) {
            addPage();
        }
        return page;
    }

    public PDDocument getDocument() {
        return document;
    }

    /**
     * Release the content stream and PDF, without completing rendering or invoking
     * finish listeners. Repeated calls have no effect. Save the PDF before closing.
     */
    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        // Always close the PDF, including when closing the content stream fails.
        try (PDDocument pdf = document) {
            closeContentStream();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    // internal

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Document is closed");
        }
    }

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
