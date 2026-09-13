package inspired.pdf.unbox;

import inspired.pdf.unbox.internal.PdfEventListener;
import inspired.pdf.unbox.internal.PdfUnboxException;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.awt.Color;
import java.io.IOException;
import java.util.Objects;

/**
 * Optional page background grid for inspecting physical layout. Register it before
 * other page listeners when the grid should appear behind headers and footers.
 */
public final class Ruler implements PdfEventListener {

    private static final Color DEFAULT_MINOR_COLOR = new Color(225, 225, 225);
    private static final Color DEFAULT_MAJOR_COLOR = new Color(200, 200, 200);

    private final Length minorStep;
    private final int majorEvery;
    private Color minorColor = DEFAULT_MINOR_COLOR;
    private Color majorColor = DEFAULT_MAJOR_COLOR;
    private float minorWidth = 0.2f;
    private float majorWidth = 0.35f;

    /** Create a ruler with the given minor step and major-line interval. */
    public Ruler(Length minorStep, int majorEvery) {
        this.minorStep = Objects.requireNonNull(minorStep, "minorStep");
        if (!(minorStep.points() > 0)) {
            throw new IllegalArgumentException("Ruler step must be positive, but was " + minorStep);
        }
        if (majorEvery <= 0) {
            throw new IllegalArgumentException("Ruler major interval must be positive, but was " + majorEvery);
        }
        this.majorEvery = majorEvery;
    }

    /** Create a millimetre ruler with a stronger line every centimetre. */
    public static Ruler mm() {
        return new Ruler(Length.of(1, Unit.MM), 10);
    }

    public Ruler withMinorColor(Color color) {
        minorColor = Objects.requireNonNull(color, "color");
        return this;
    }

    public Ruler withMajorColor(Color color) {
        majorColor = Objects.requireNonNull(color, "color");
        return this;
    }

    public Ruler withMinorWidth(float width) {
        minorWidth = requireWidth(width);
        return this;
    }

    public Ruler withMajorWidth(float width) {
        majorWidth = requireWidth(width);
        return this;
    }

    @Override
    public void onNewPage(Document document) {
        Bounds page = document.getPageBounds();
        float step = minorStep.points();
        try {
            PDPageContentStream stream = document.getContentStream();
            int verticalLines = (int) Math.floor(page.width() / step);
            int horizontalLines = (int) Math.floor(page.height() / step);
            // Paint the fine grid first, then major lines over it at every intersection.
            for (boolean major : new boolean[] {false, true}) {
                for (int index = 0; index <= verticalLines; index++) {
                    if (isMajor(index) == major) {
                        drawVertical(stream, page, index * step, major);
                    }
                }
                for (int index = 0; index <= horizontalLines; index++) {
                    if (isMajor(index) == major) {
                        drawHorizontal(stream, page, index * step, major);
                    }
                }
            }
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    private boolean isMajor(int index) {
        return index % majorEvery == 0;
    }

    private void drawVertical(PDPageContentStream stream, Bounds page, float x, boolean major) throws IOException {
        stream.setStrokingColor(major ? majorColor : minorColor);
        stream.setLineWidth(major ? majorWidth : minorWidth);
        stream.moveTo(x, page.bottom());
        stream.lineTo(x, page.top());
        stream.stroke();
    }

    private void drawHorizontal(PDPageContentStream stream, Bounds page, float y, boolean major) throws IOException {
        stream.setStrokingColor(major ? majorColor : minorColor);
        stream.setLineWidth(major ? majorWidth : minorWidth);
        stream.moveTo(page.left(), y);
        stream.lineTo(page.right(), y);
        stream.stroke();
    }

    private static float requireWidth(float width) {
        if (!(width > 0) || !Float.isFinite(width)) {
            throw new IllegalArgumentException("Ruler line width must be positive and finite, but was " + width);
        }
        return width;
    }
}
