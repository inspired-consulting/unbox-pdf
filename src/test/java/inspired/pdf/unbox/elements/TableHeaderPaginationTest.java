package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.internal.AbstractTableCell;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies bounded header pagination: oversized headers fail clearly instead of
 * creating pages without end, ordinary headers repeat once per page, and a table
 * keeps initial headers with the first row when they fit together on a fresh page.
 */
class TableHeaderPaginationTest {

    private static final TableModel MODEL = new TableModel().add("Header").add("Value");

    @Test
    void oversizedCustomHeaderCellFailsClearlyWithBoundedPages() {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL);
            table.addHeader(new TableRow(MODEL).addCell(new FixedHeightCell(2000)).addCell("Header"));
            table.addRow("a", "b");

            PdfUnboxException failure = assertThrows(PdfUnboxException.class, () -> document.render(table));
            assertTrue(failure.getMessage().contains("does not fit"), failure.getMessage());
            assertTrue(document.getDocument().getNumberOfPages() <= 2);
        }
    }

    @Test
    void oversizedWrappingTextHeaderFailsClearly() {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL);
            table.addHeader(new TableRow(MODEL).addCell("line\n".repeat(200)).addCell("Header"));
            table.addRow("a", "b");

            assertThrows(PdfUnboxException.class, () -> document.render(table));
            assertTrue(document.getDocument().getNumberOfPages() <= 2);
        }
    }

    @Test
    void headerGroupTooTallAsAWholeFailsClearly() {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL);
            for (int i = 0; i < 3; i++) {
                table.addHeader(new TableRow(MODEL).addCell(new FixedHeightCell(300)).addCell("Header " + i));
            }
            table.addRow("a", "b");

            assertThrows(PdfUnboxException.class, () -> document.render(table));
            assertTrue(document.getDocument().getNumberOfPages() <= 2);
        }
    }

    @Test
    void oversizedHeaderWithoutRepetitionRendersOnceAndOverflows() {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL);
            table.repeatHeader(false);
            table.addHeader(new TableRow(MODEL).addCell(new FixedHeightCell(2000)).addCell("Header"));
            table.addRow("a", "b");

            document.render(table);
            assertTrue(document.getDocument().getNumberOfPages() <= 3);
        }
    }

    @Test
    void ordinaryHeadersRepeatOncePerPage() throws IOException {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL).withHeader();
            for (int i = 0; i < 120; i++) {
                table.addRow("row " + i, "value");
            }
            document.render(table);

            try (PDDocument pdf = document.finish()) {
                int pages = pdf.getNumberOfPages();
                assertTrue(pages >= 3, "expected several pages, got " + pages);
                for (int page = 1; page <= pages; page++) {
                    assertEquals(1, occurrences(textOfPage(pdf, page), "Header"), "page " + page);
                }
            }
        }
    }

    @Test
    void tableNearPageEndStartsOnNextPageWithItsHeader() throws IOException {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL).withHeader();
            table.addRow("first", "row");
            float headerHeight = TableRow.header(MODEL).innerHeight(document.getCurrentViewPort());
            // Room for the header alone, but not for header and first row together.
            document.getPage();
            document.forward(document.getSpaceLeftOnPage() - headerHeight - 2);

            document.render(table);

            try (PDDocument pdf = document.finish()) {
                assertEquals(2, pdf.getNumberOfPages());
                assertFalse(textOfPage(pdf, 1).contains("Header"), "no orphan header on page 1");
                assertEquals(1, occurrences(textOfPage(pdf, 2), "Header"));
                assertTrue(textOfPage(pdf, 2).contains("first"));
            }
        }
    }

    @Test
    void nonRepeatingHeadersMoveWithFirstRowAndAppearOnlyOnce() throws IOException {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL).withHeader();
            TableRow secondHeader = table.addHeader(new TableRow(MODEL).withCells("Subheader", "Details"));
            table.repeatHeader(false);
            table.addRow("first", "row");
            for (int i = 0; i < 120; i++) {
                table.addRow("row " + i, "value");
            }
            float headerHeight = TableRow.header(MODEL).innerHeight(document.getCurrentViewPort())
                    + secondHeader.innerHeight(document.getCurrentViewPort());
            document.forward(document.getSpaceLeftOnPage() - headerHeight - 2);

            document.render(table);

            PDDocument pdf = document.finish();
            assertTrue(pdf.getNumberOfPages() >= 3);
            assertFalse(textOfPage(pdf, 1).contains("Header"));
            assertFalse(textOfPage(pdf, 1).contains("Subheader"));
            String firstTablePage = textOfPage(pdf, 2);
            assertEquals(1, occurrences(firstTablePage, "Header"));
            assertEquals(1, occurrences(firstTablePage, "Subheader"));
            assertTrue(firstTablePage.contains("first"));
            assertTrue(firstTablePage.indexOf("Header") < firstTablePage.indexOf("Subheader"));
            assertTrue(firstTablePage.indexOf("Subheader") < firstTablePage.indexOf("first"));
            for (int page = 3; page <= pdf.getNumberOfPages(); page++) {
                String text = textOfPage(pdf, page);
                assertFalse(text.contains("Header"), "no repeated header on page " + page);
                assertFalse(text.contains("Subheader"), "no repeated subheader on page " + page);
            }
            assertTrue(textOfPage(pdf, pdf.getNumberOfPages()).contains("row 119"));
        }
    }

    @Test
    void nonRepeatingHeaderOnlyTableMovesToNextPage() throws IOException {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL).withHeader();
            table.repeatHeader(false);
            float headerHeight = TableRow.header(MODEL).innerHeight(document.getCurrentViewPort());
            document.forward(document.getSpaceLeftOnPage() - headerHeight / 2);

            document.render(table);

            PDDocument pdf = document.finish();
            assertEquals(2, pdf.getNumberOfPages());
            assertFalse(textOfPage(pdf, 1).contains("Header"));
            assertEquals(1, occurrences(textOfPage(pdf, 2), "Header"));
        }
    }

    @Test
    void oversizedBodyRowRendersOnceAndOverflows() {
        try (Document document = new Document()) {
            FixedColumnsTable table = new FixedColumnsTable(MODEL).withHeader();
            table.addRow().addCell(new FixedHeightCell(2000)).addCell("tall");
            table.addRow("after", "row");

            document.render(table);
            assertTrue(document.getDocument().getNumberOfPages() <= 3);
        }
    }

    // -- helpers

    /** A cell that reports a fixed height and draws nothing. */
    private static final class FixedHeightCell extends AbstractTableCell {
        private final float height;

        FixedHeightCell(float height) {
            this.height = height;
        }

        @Override
        public float innerHeight(Bounds viewPort) {
            return height;
        }

        @Override
        protected float renderCell(Document document, Bounds viewPort) {
            return height;
        }

        @Override
        public void setValue(Object value) {
        }
    }

    private static String textOfPage(PDDocument pdf, int page) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setStartPage(page);
        stripper.setEndPage(page);
        return stripper.getText(pdf);
    }

    private static int occurrences(String text, String word) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(word, index)) >= 0) {
            count++;
            index += word.length();
        }
        return count;
    }
}
