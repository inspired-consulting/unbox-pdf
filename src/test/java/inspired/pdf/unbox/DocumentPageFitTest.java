package inspired.pdf.unbox;

import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.FixedColumnsTable;
import inspired.pdf.unbox.elements.Paragraph;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the page-fit check of {@code Document.render()}: an element moves to a
 * new page when its inner height plus top margin exceeds the remaining body
 * space, while a bottom margin may extend past the page end.
 */
class DocumentPageFitTest {

    private static final String TEXT = "fit check";

    /** Surplus above float rounding, so that an exact fit is not decided by the last bit. */
    private static final float ROUNDING = 0.001f;

    @Test
    void topMarginThatDoesNotFitMovesElementToNextPage() throws IOException {
        try (Document document = new Document()) {
            Paragraph paragraph = new Paragraph(TEXT).with(Margin.top(30));
            float inner = paragraph.innerHeight(document.getCurrentViewPort());
            leaveSpace(document, inner + 20);

            document.render(paragraph);

            assertEquals(2, document.getDocument().getNumberOfPages());
            assertEquals(TEXT, textOfPage(document, 2));
            float bodyHeight = document.getViewPort().height();
            assertEquals(bodyHeight - 30 - inner, document.getSpaceLeftOnPage(), 0.01f);
        }
    }

    @Test
    void exactFitIncludingTopMarginStaysOnPage() throws IOException {
        try (Document document = new Document()) {
            Paragraph paragraph = new Paragraph(TEXT).with(Margin.top(30));
            float inner = paragraph.innerHeight(document.getCurrentViewPort());
            leaveSpace(document, inner + 30 + ROUNDING);

            document.render(paragraph);

            assertEquals(1, document.getDocument().getNumberOfPages());
            assertEquals(0, document.getSpaceLeftOnPage(), 0.01f);
        }
    }

    @Test
    void bottomMarginMayExtendPastPageEnd() throws IOException {
        try (Document document = new Document()) {
            Paragraph paragraph = new Paragraph(TEXT).with(Margin.bottom(30));
            float inner = paragraph.innerHeight(document.getCurrentViewPort());
            leaveSpace(document, inner + 10);

            document.render(paragraph);

            assertEquals(1, document.getDocument().getNumberOfPages());
            assertEquals(TEXT, textOfPage(document, 1));
            assertTrue(document.getSpaceLeftOnPage() < 0, "bottom margin should be absorbed by the page end");
        }
    }

    @Test
    void elementWithoutMarginBreaksOnlyWhenContentDoesNotFit() throws IOException {
        try (Document document = new Document()) {
            Paragraph fitting = new Paragraph(TEXT);
            float inner = fitting.innerHeight(document.getCurrentViewPort());
            leaveSpace(document, inner + ROUNDING);
            document.render(fitting);
            assertEquals(1, document.getDocument().getNumberOfPages());

            document.render(new Paragraph(TEXT));
            assertEquals(2, document.getDocument().getNumberOfPages());
        }
    }

    @Test
    void tableWithTopMarginNearPageEndStartsOnNextPage() throws IOException {
        try (Document document = new Document()) {
            TableModel model = new TableModel().add("Header");
            FixedColumnsTable table = new FixedColumnsTable(model).withHeader();
            table.with(Margin.top(30));
            table.addRow("body");
            leaveSpace(document, 20);

            document.render(table);

            assertEquals(2, document.getDocument().getNumberOfPages());
            assertEquals("", textOfPage(document, 1));
            assertEquals("Header\nbody", textOfPage(document, 2));
            assertEquals(document.getViewPort().top() - 30, table.getTableStartOnPage(), 0.01f);
        }
    }

    // -- helpers

    /** Positions the cursor so that the given body space remains on the first page. */
    private static void leaveSpace(Document document, float space) {
        document.getPage();
        document.forward(document.getSpaceLeftOnPage() - space);
    }

    private static String textOfPage(Document document, int page) throws IOException {
        PDDocument pdf = document.getDocument();
        // The content stream must be closed before the text can be extracted.
        document.finish();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setLineSeparator("\n");
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        return stripper.getText(pdf).strip();
    }
}
