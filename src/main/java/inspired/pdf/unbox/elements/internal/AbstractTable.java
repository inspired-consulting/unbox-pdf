package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.base.Column;
import inspired.pdf.unbox.base.ColumnModel;
import inspired.pdf.unbox.decorators.Decorator;
import inspired.pdf.unbox.elements.Table;
import inspired.pdf.unbox.elements.TableCell;
import inspired.pdf.unbox.elements.TableRow;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_700;

/**
 * Shared row pagination, header handling, and decoration for tables.
 */
public abstract class AbstractTable extends AbstractDecoratable implements Table {

    private final List<TableRow> headers = new ArrayList<>();
    private final List<TableRow> rows = new ArrayList<>();
    private Margin margin = Margin.of(0);

    private Padding cellPadding = TableCell.DEFAULT_CELL_PADDING;
    private boolean repeatHeader = true;
    private boolean repeatingHeaders;
    private static final float FIT_TOLERANCE = 0.01f;
    private float tableStartOnPage;

    private static final Stroke DEFAULT_STROKE = new Stroke(GRAY_700, 0.4f);

    private Stroke rowStroke = DEFAULT_STROKE;
    private Stroke columnStroke = DEFAULT_STROKE;

    @Override
    public TableRow addRow() {
        return addRow(new TableRow());
    }

    @Override
    public TableRow addRow(TableRow row) {
        row.withDefaultCellPadding(cellPadding);
        this.rows.add(row);
        return row;
    }

    @Override
    public TableRow addHeader(TableRow row) {
        row.withDefaultCellPadding(cellPadding);
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
        this.rowStroke = stroke;
        this.columnStroke = stroke;
        return this;
    }

    @Override
    public Table withRowStroke(Stroke stroke) {
        this.rowStroke = stroke;
        return this;
    }

    @Override
    public Table withColumnStroke(Stroke stroke) {
        this.columnStroke = stroke;
        return this;
    }

    public Table withCellPadding(Padding cellPadding) {
        this.cellPadding = cellPadding;
        headers.forEach(h -> h.withDefaultCellPadding(cellPadding));
        rows.forEach(r -> r.withDefaultCellPadding(cellPadding));
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
            if (repeatHeader) {
                requireHeadersFitOnPage(document, document.getViewPort().height());
            }
            if (!startFitsOnPage(document)) {
                breakPage(document);
                if (!repeatHeader) {
                    // The initial headers are required even when later pages do not repeat them.
                    renderRows(document, headers);
                }
            } else {
                renderRows(document, headers);
            }
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
        float rowHeight = row.innerHeight(effectiveViewport(document));
        checkPageBreak(document, rowHeight);

        var bounds = effectiveViewport(document).height(rowHeight);
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
        if (rowStroke.isEmpty()) {
            return;
        }

        try {
            var contentStream = document.getContentStream();
            contentStream.setLineWidth(rowStroke.width());
            contentStream.setStrokingColor(rowStroke.color());

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
        if (columnStroke.isEmpty()) {
            return;
        }

        try {
            var contentStream = document.getContentStream();
            contentStream.setLineWidth(columnStroke.width());
            contentStream.setStrokingColor(columnStroke.color());
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
        if (repeatingHeaders) {
            // The header group was verified to fit the fresh page; never break again while repeating it.
            return;
        }
        float spaceLeft = document.getSpaceLeftOnPage();
        if (spaceLeft >= minSpace) {
            return;
        }
        if (spaceLeft >= freshPageSpace(document) - FIT_TOLERANCE) {
            // A new page would not give more room: the row is oversized and is rendered overflowing.
            return;
        }
        breakPage(document);
    }

    /**
     * Whether the table should start on the current page. It starts here when the header
     * group and the first body row fit together, so a header is never left alone at the end
     * of a page, and also when they would not fit a fresh page either.
     */
    private boolean startFitsOnPage(Document document) {
        float firstRow = rows.isEmpty() ? 0f : rows.get(0).innerHeight(effectiveViewport(document));
        float required = headersHeight(document) + firstRow;
        return document.getSpaceLeftOnPage() >= required || required > document.getViewPort().height();
    }

    /** The space a body row gets on a fresh page, after the repeated headers. */
    private float freshPageSpace(Document document) {
        float body = document.getViewPort().height();
        return repeatHeader ? body - headersHeight(document) : body;
    }

    /**
     * Finish the current page segment, start a new page, and repeat the headers if enabled.
     * The headers are rendered at most once per page; a header group that does not fit a
     * fresh page fails with a {@link PdfUnboxException} instead of creating more pages.
     */
    private void breakPage(Document document) {
        try {
            applyDecorators(document);
            onBeforeNewPage(document);
            document.addPage();
            tableStartOnPage = document.getPosition();
            onAfterNewPage(document);
            if (repeatHeader) {
                repeatHeaders(document);
            }
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private void repeatHeaders(Document document) throws IOException {
        requireHeadersFitOnPage(document, document.getSpaceLeftOnPage());
        repeatingHeaders = true;
        try {
            renderRows(document, headers);
        } finally {
            repeatingHeaders = false;
        }
    }

    /**
     * Repeated headers must fit a page, otherwise every page break would need another page.
     * Fails before anything is drawn, so the caller gets a clear message instead of a broken document.
     */
    private void requireHeadersFitOnPage(Document document, float available) {
        float required = headersHeight(document);
        if (required > available) {
            throw new PdfUnboxException(String.format(
                    "Table header of %.1f points does not fit on a page with %.1f points of space", required, available));
        }
    }

    private float headersHeight(Document document) {
        float height = 0f;
        for (TableRow header : headers) {
            height += header.innerHeight(effectiveViewport(document));
        }
        return height;
    }

    private void applyDecorators(Document document) throws IOException {
        float height = tableStartOnPage - document.getPosition();
        if (height <= 0) {
            // Nothing of the table was rendered on this page yet.
            return;
        }
        var bounds = document.getViewPort()
                .apply(horizontalMargin())
                .top(tableStartOnPage)
                .height(height);
        super.applyDecorators(document, bounds);
    }

}
