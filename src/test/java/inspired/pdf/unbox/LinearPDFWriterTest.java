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
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class CustomTableCell extends AbstractTableCell {

    @Override
    public float render(LinearPDFWriter writer, Bounds viewPort) {
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
        topLeftCell.render(writer, topViewPort);
        topCenterCell.render(writer, topViewPort);
        topRightCell.render(writer, topViewPort);


        var middleHeight = middleLeftCell.innerHeight(viewPort);
        var middleViewPort = viewPort.height(middleHeight).moveDown((viewPort.height() - middleHeight) / 2);
        middleLeftCell.render(writer, middleViewPort);
        middleCenterCell.render(writer, middleViewPort);
        middleRightCell.render(writer, middleViewPort);

        var bottomHeight = bottomLeftCell.innerHeight(viewPort);
        var bottomViewPort = viewPort.height(bottomHeight).moveDown(viewPort.height() - bottomHeight);
        bottomLeftCell.render(writer, bottomViewPort);
        bottomCenterCell.render(writer, bottomViewPort);
        bottomRightCell.render(writer, bottomViewPort);

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


class LinearPDFWriterTest {
    @TempDir
    Path folder;

    @Test
    void createEmptyPdf() {
        var writer = new LinearPDFWriter();
        var document = writer.finish();
        assertDocumentMatchesReference(document, "empty.pdf");
    }

    @Test
    void createTable() {
        var writer = new LinearPDFWriter();
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
        writer.render(table);

        var document = writer.finish();
        assertDocumentMatchesReference(document, "table.pdf");
    }

    @Test
    void useCustomTableCell() {
        var writer = new LinearPDFWriter();
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
        writer.render(table);

        var document = writer.finish();
        assertDocumentMatchesReference(document, "customTable.pdf");
    }


    @Test
    void tableWithAutomaticTableCells() {
        var writer = new LinearPDFWriter();
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
        writer.render(table);

        var document = writer.finish();
        assertDocumentMatchesReference(document, "tableAutomaticCells.pdf");
    }

    @Test
    void tableWithTableCellsDefinedInModel() {
        var writer = new LinearPDFWriter();
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
        writer.render(table);

        var document = writer.finish();
        assertDocumentMatchesReference(document, "tableTableModelCells.pdf");
    }

    @Test
    void tableWithMultiLineCells() {
        var writer = new LinearPDFWriter();
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
        writer.render(table);

        var document = writer.finish();
        assertDocumentMatchesReference(document, "tableMultiLineCells.pdf");
    }

    void assertDocumentMatchesReference(PDDocument document, String fileName) {
        try {
            var documentFilePath = folder.resolve(fileName);

            var resourceDirectory = Paths.get("src", "test", "resources");
            var referenceFilePath = resourceDirectory.resolve(fileName);

            document.setDocumentId(1L);
            document.save(documentFilePath.toString());
            document.close();

//            Files.copy(documentFilePath, referenceFilePath, REPLACE_EXISTING);
            long mismatchPosition = Files.mismatch(documentFilePath, referenceFilePath);
            assertEquals(-1L, mismatchPosition, "PDF document doesn't match reference.");
        } catch (IOException e) {
            fail("Error writing pdf", e);
        }
    }
}