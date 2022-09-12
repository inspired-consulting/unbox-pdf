package inspired.pdf.unbox.elements.internal;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.LinearPDFWriter;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Position;
import inspired.pdf.unbox.base.Column;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.Table;
import inspired.pdf.unbox.elements.TableRow;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

/**
 * Abstract base for tables.
 */
public abstract class AbstractTable implements Table {

    private final static float GRID_STROKE = 0.4f;

    private final List<TableRow> headers = new ArrayList<>();
    private final List<TableRow> rows = new ArrayList<>();
    private final List<Decorator> decorators = new ArrayList<>();
    private Margin margin = Margin.of(0);
    private boolean repeatHeader = true;
    private float tableStartOnPage;

    @Override
    public TableRow addRow() {
        return addRow(new TableRow());
    }

    @Override
    public TableRow addRow(TableRow row) {
        this.rows.add(row);
        return row;
    }

    @Override
    public TableRow addHeader(TableRow row) {
        this.headers.add(row);
        return row;
    }

    @Override
    public TableRow addRow(Object... values) {
        var row = this.addRow();
        row.withValues(values);
        return row;
    }

    @Override
    public Table with(Decorator decorator) {
        this.decorators.add(decorator);
        return this;
    }

    public Table with(Margin margin) {
        this.margin = margin;
        return this;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return 0;
    }

    /**
     * Specify if the header shall be repeated on each page.
     */
    public Table repeatHeader(boolean repeat) {
        this.repeatHeader = repeat;
        return this;
    }

    // base for implementations

    @Override
    public float render(LinearPDFWriter writer, Bounds viewPort) {
        try {
            writer.forward(margin.top());
            tableStartOnPage = writer.getPosition();
            renderRows(writer, headers);
            renderRows(writer, rows);
            applyDecorators(writer);
            return margin.bottom();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    protected void onBeforeNewPage(LinearPDFWriter writer) {
    }

    protected void onAfterNewPage(LinearPDFWriter writer) {
    }

    protected Margin getMargin() {
        return margin;
    }

    protected float renderRow(LinearPDFWriter writer, TableRow row) {
        float rowHeight = row.innerHeight(writer.getViewPort());
        checkPageBreak(writer, rowHeight);

        Bounds bounds = writer.getCurrentViewPort().height(rowHeight);
        row.render(writer, bounds);
        drawRowLines(writer, rowHeight);
        return rowHeight;
    }

    public float getTableStartOnPage() {
        return tableStartOnPage;
    }

    // internal

    private float renderRows(LinearPDFWriter writer, Iterable<TableRow> rows) throws IOException {
        float height = DONT_FORWARD;
        for (TableRow row : rows) {
            float rowHeight = renderRow(writer, row);
            writer.forward(rowHeight);
            height += rowHeight;
        }
        return height;
    }

    private void drawRowLines(LinearPDFWriter writer, float rowHeight) {
        try {
            PDPageContentStream contentStream = writer.getContentStream();
            contentStream.setLineWidth(GRID_STROKE);
            contentStream.setStrokingColor(Color.DARK_GRAY);

            Bounds bounds = writer.getCurrentViewPort().height(rowHeight);

            contentStream.moveTo(bounds.left(), bounds.top());
            contentStream.lineTo(bounds.right(), bounds.top());
            contentStream.stroke();

            contentStream.moveTo(bounds.left(), bounds.bottom());
            contentStream.lineTo(bounds.right(), bounds.bottom());
            contentStream.stroke();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    protected void drawColumnLines(LinearPDFWriter writer, ColumnModel<?> columns, Bounds bounds) {
        try {
            PDPageContentStream contentStream = writer.getContentStream();
            contentStream.setLineWidth(GRID_STROKE);
            contentStream.setStrokingColor(Color.DARK_GRAY);
            boolean first = true;
            for (Column col : columns) {
                if (first) {
                    first = false;
                } else {
                    drawLine(contentStream, bounds.topLeft(), bounds.bottomLeft());
                }
                bounds = bounds.moveRight(col.width());
            }
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private void drawLine(PDPageContentStream contentStream, Position start, Position end) {
        try {
            contentStream.moveTo(start.x(), start.y());
            contentStream.lineTo(end.x(), end.y());
            contentStream.stroke();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private void checkPageBreak(LinearPDFWriter writer, float minSpace) {
        try {
            if (writer.getSpaceLeftOnPage() < minSpace) {
                applyDecorators(writer);
                onBeforeNewPage(writer);
                writer.addPage();
                writer.forward(margin.top());
                tableStartOnPage = writer.getPosition();
                onAfterNewPage(writer);
                if (repeatHeader) {
                    renderRows(writer, headers);
                }
            }
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private void applyDecorators(LinearPDFWriter writer) throws IOException {
        float height = tableStartOnPage - writer.getPosition();
        Bounds bounds = writer.getViewPort().top(tableStartOnPage).height(height);
        for (Decorator decorator : decorators) {
            decorator.render(writer, bounds);
        }
    }

}
