package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.base.Column;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.Table;
import inspired.pdf.unbox.elements.TableRow;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_700;

/**
 * Abstract base for tables.
 */
public abstract class AbstractTable extends AbstractDecoratable implements Table {

    private final List<TableRow> headers = new ArrayList<>();
    private final List<TableRow> rows = new ArrayList<>();
    private Margin margin = Margin.of(0);
    private boolean repeatHeader = true;
    private float tableStartOnPage;

    private Stroke stroke = new Stroke(GRAY_700,0.4f);

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
        super.with(decorator);
        return this;
    }

    @Override
    public Table with(Margin margin) {
        this.margin = margin;
        return this;
    }

    @Override
    public Table with(Stroke stroke) {
        this.stroke = stroke;
        return this;
    }

    public Table withHeader(TableModel model) {
        addHeader(TableRow.header(model));
        return this;
    }

    @Override
    public float innerHeight(Bounds viewPort) {
        return 0;
    }

    @Override
    public Margin margin() {
        return margin;
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
    public float render(Document document, Bounds viewPort) {
        try {
            document.forward(margin.top());
            tableStartOnPage = document.getPosition();
            renderRows(document, headers);
            renderRows(document, rows);
            applyDecorators(document);
            return margin.bottom();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    protected void onBeforeNewPage(Document document) {
    }

    protected void onAfterNewPage(Document document) {
    }

    protected Margin getMargin() {
        return margin;
    }

    protected float renderRow(Document document, TableRow row) {
        Bounds viewPort = document.getCurrentViewPort().apply(horizontalMargin());
        float rowHeight = row.innerHeight(viewPort);
        checkPageBreak(document, rowHeight);

        Bounds bounds = viewPort.height(rowHeight);
        row.render(document, bounds);
        drawRowLines(document, rowHeight);
        return rowHeight;
    }

    public float getTableStartOnPage() {
        return tableStartOnPage;
    }

    // internal

    private float renderRows(Document document, Iterable<TableRow> rows) throws IOException {
        float height = DONT_FORWARD;
        for (TableRow row : rows) {
            float rowHeight = renderRow(document, row);
            document.forward(rowHeight);
            height += rowHeight;
        }
        return height;
    }

    private void drawRowLines(Document document, float rowHeight) {
        if (stroke.isEmpty()) {
            return;
        }

        try {
            var contentStream = document.getContentStream();
            contentStream.setLineWidth(stroke.width());
            contentStream.setStrokingColor(stroke.color());

            var bounds = document.getCurrentViewPort()
                    .apply(horizontalMargin())
                    .height(rowHeight);

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

    protected void drawColumnLines(Document document, ColumnModel<?> columns, Bounds bounds) {
        if (stroke.isEmpty()) {
            return;
        }

        try {
            var contentStream = document.getContentStream();
            contentStream.setLineWidth(stroke.width());
            contentStream.setStrokingColor(stroke.color());
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

    /**
     * Get the effective view port, with horizontal margins applied.
     */
    protected Bounds effectiveViewport(Document document) {
        return document.getCurrentViewPort().apply(horizontalMargin());
    }

    /**
     * Get the horizontal margins, i.e. only left and right set.
     */
    protected Margin horizontalMargin() {
        return Margin.of(0, margin.right(), 0, margin.left());
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

    private void checkPageBreak(Document document, float minSpace) {
        try {
            if (document.getSpaceLeftOnPage() < minSpace) {
                applyDecorators(document);
                onBeforeNewPage(document);
                document.addPage();
                document.forward(margin.top());
                tableStartOnPage = document.getPosition();
                onAfterNewPage(document);
                if (repeatHeader) {
                    renderRows(document, headers);
                }
            }
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private void applyDecorators(Document document) throws IOException {
        float height = tableStartOnPage - document.getPosition();
        var bounds = document.getViewPort()
                .apply(horizontalMargin())
                .top(tableStartOnPage)
                .height(height);
        super.applyDecorators(document, bounds);
    }

}
