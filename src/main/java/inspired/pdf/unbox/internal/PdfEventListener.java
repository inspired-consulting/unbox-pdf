package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.LinearPDFWriter;

/**
 * Listener for PDF events.
 */
public interface PdfEventListener {

    default void onNewPage(LinearPDFWriter writer) {};

    default void onFinished(LinearPDFWriter writer) {};

}
