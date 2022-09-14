package inspired.pdf.unbox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.FixedColumnsTable;
import inspired.pdf.unbox.elements.Table;
import inspired.pdf.unbox.elements.TableRow;
import inspired.pdf.unbox.elements.TextCell;
import inspired.pdf.unbox.elements.internal.AbstractTableCell;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static inspired.pdf.unbox.Unbox.background;
import static inspired.pdf.unbox.decorators.BorderDecorator.border;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class CustomTableCell extends AbstractTableCell {

    @Override
    public float render(Document document, Bounds viewPort) {
        var topLeftCell = new TextCell("top-left", Align.LEFT, null);
        var topCenterCell = new TextCell("top-center", Align.CENTER, null);
        var topRightCell = new TextCell("top-right", Align.RIGHT, null);
        var middleLeftCell = new TextCell("middle-left", Align.LEFT, null);
        var middleCenterCell = new TextCell("middle-center", Align.CENTER, null);
        var middleRightCell = new TextCell("middle-right", Align.RIGHT, null);
        var bottomLeftCell = new TextCell("bottom-left", Align.LEFT, null);
        var bottomCenterCell = new TextCell("bottom-center", Align.CENTER, null);
        var bottomRightCell = new TextCell("bottom-right", Align.RIGHT, null);

        var topHeight = topLeftCell.innerHeight(viewPort);
        var topViewPort = viewPort.height(topHeight);
        topLeftCell.render(document, topViewPort);
        topCenterCell.render(document, topViewPort);
        topRightCell.render(document, topViewPort);


        var middleHeight = middleLeftCell.innerHeight(viewPort);
        var middleViewPort = viewPort.height(middleHeight).moveDown((viewPort.height() - middleHeight) / 2);
        middleLeftCell.render(document, middleViewPort);
        middleCenterCell.render(document, middleViewPort);
        middleRightCell.render(document, middleViewPort);

        var bottomHeight = bottomLeftCell.innerHeight(viewPort);
        var bottomViewPort = viewPort.height(bottomHeight).moveDown(viewPort.height() - bottomHeight);
        bottomLeftCell.render(document, bottomViewPort);
        bottomCenterCell.render(document, bottomViewPort);
        bottomRightCell.render(document, bottomViewPort);

        return viewPort.height();
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return 100;
    }

    @Override
    public void setValue(Object value) {

    }
}


class DocumentTest {
    @TempDir
    Path folder;

    @Test
    void createEmptyPdf() {
        var document = new Document();
        var pdf = document.finish();
        assertDocumentMatchesReference(pdf, "empty.pdf");
    }

    @Test
    void createTable() {
        var document = new Document();
        var tableModel = new TableModel()
                .add("Article", 2f)
                .add("Size")
                .add("Price", Align.RIGHT);
        Table table = new FixedColumnsTable(tableModel)
                .with(Margin.of(10))
                .with(border(1, GRAY_500));
        table.addHeader(TableRow.header(tableModel, helvetica_bold(8)).with(background(GRAY_100)));
        table.addRow()
                .addCell("SmartTV 200+")
                .addCell("55", Align.LEFT, helvetica_bold(8, RED_ORANGE))
                .addCell("200.12 EUR");
        table.addRow().withCells("SmartPhone", "5,5", "320.00 EUR");
        document.render(table);

        var pdf = document.finish();
        assertDocumentMatchesReference(pdf, "table.pdf");
    }

    @Test
    void useCustomTableCell() {
        var document = new Document();
        var tableModel = new TableModel()
                .add("Article", 2f)
                .add("Custom")
                .add("Price", Align.RIGHT);
        Table table = new FixedColumnsTable(tableModel)
                .with(Margin.of(10))
                .with(border(1, GRAY_500));
        table.addHeader(TableRow.header(tableModel, helvetica_bold(8)).with(background(GRAY_100)));
        table.addRow()
                .addCell("SmartTV 200+")
                .addCell(new CustomTableCell())
                .addCell("200.12 EUR");
        document.render(table);

        var pdf = document.finish();
        assertDocumentMatchesReference(pdf, "customTable.pdf");
    }


    @Test
    void tableWithAutomaticTableCells() {
        var document = new Document();
        var tableModel = new TableModel()
                .add("Article", 2f)
                .add("Custom")
                .add("Price", Align.RIGHT);
        Table table = new FixedColumnsTable(tableModel)
                .with(Margin.of(10))
                .with(border(1, GRAY_500));
        table.addHeader(TableRow.header(tableModel, helvetica_bold(8)).with(background(GRAY_100)));
        table.addRow("String field", 123456, 14.7f);
        table.addRow("Other field", null, 9999.99f);
        document.render(table);

        var pdf = document.finish();
        assertDocumentMatchesReference(pdf, "tableAutomaticCells.pdf");
    }

    @Test
    void tableWithTableCellsDefinedInModel() {
        var document = new Document();
        var tableModel = new TableModel()
                .add("Article", 2f, new TextCell("", Align.CENTER, null))
                .add("Custom")
                .add("Price", new TextCell("", Align.LEFT, null));
        Table table = new FixedColumnsTable(tableModel)
                .with(Margin.of(10))
                .with(border(1, GRAY_500));
        table.addHeader(TableRow.header(tableModel, helvetica_bold(8)).with(background(GRAY_100)));
        table.addRow("String field", 123456, 14.7f);
        table.addRow("Other field", null, 9999.99f);
        document.render(table);

        var pdf = document.finish();
        assertDocumentMatchesReference(pdf, "tableTableModelCells.pdf");
    }

    @Test
    void tableWithMultiLineCells() {
        var document = new Document();
        var tableModel = new TableModel()
                .add("Article", 2f, new TextCell("", Align.LEFT, null))
                .add("Custom", new TextCell("", Align.CENTER, null))
                .add("Price", new TextCell("", Align.RIGHT, null));
        Table table = new FixedColumnsTable(tableModel)
                .with(Margin.of(10))
                .with(border(1, GRAY_500));
        table.addHeader(TableRow.header(tableModel, helvetica_bold(8)).with(background(GRAY_100)));
        table.addRow("These\nare\nfour\nlines.", "With\n\n\n\nmany\nmore", 14.7f);
        table.addRow("And\nhere\nthree.", null, 9999.99f);
        document.render(table);

        var pdf = document.finish();
        assertDocumentMatchesReference(pdf, "tableMultiLineCells.pdf");
    }

    void assertDocumentMatchesReference(PDDocument pdf, String fileName) {
        try {
            var documentFilePath = folder.resolve(fileName);

            var resourceDirectory = Paths.get("src", "test", "resources");
            var referenceFilePath = resourceDirectory.resolve(fileName);

            pdf.setDocumentId(1L);
            pdf.save(documentFilePath.toString());
            pdf.close();

//            Files.copy(documentFilePath, referenceFilePath, REPLACE_EXISTING);
            long mismatchPosition = Files.mismatch(documentFilePath, referenceFilePath);
            assertEquals(-1L, mismatchPosition, "PDF document doesn't match reference.");
        } catch (IOException e) {
            fail("Error writing pdf", e);
        }
    }
}