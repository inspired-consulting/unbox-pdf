package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Document;

/**
 * Listener for PDF events.
 */
public interface PdfEventListener {

    default void onNewPage(Document document) {}

    default void onFinished(Document document) {}

}
