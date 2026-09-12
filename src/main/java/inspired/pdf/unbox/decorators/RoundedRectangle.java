package inspired.pdf.unbox.decorators;

import inspired.pdf.unbox.Bounds;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

/**
 * Traces a rectangle with rounded corners as a path, shared by background and border
 * decorators so that fill and outline have exactly the same shape.
 */
final class RoundedRectangle {

    /** Control point distance that approximates a quarter circle with a cubic bezier curve. */
    private static final float BEZIER = 0.552284749831f;

    private RoundedRectangle() {
    }

    /**
     * The effective radius: the requested one, clamped to half of the smaller side.
     */
    static float clamp(float radius, Bounds bounds) {
        return Math.min(radius, Math.min(bounds.width(), bounds.height()) / 2f);
    }

    static void trace(PDPageContentStream stream, Bounds bounds, float radius) throws IOException {
        float left = bounds.left();
        float right = bounds.right();
        float top = bounds.top();
        float bottom = bounds.bottom();
        float r = clamp(radius, bounds);
        float k = r * BEZIER;

        stream.moveTo(left + r, top);
        stream.lineTo(right - r, top);
        stream.curveTo(right - r + k, top, right, top - r + k, right, top - r);
        stream.lineTo(right, bottom + r);
        stream.curveTo(right, bottom + r - k, right - r + k, bottom, right - r, bottom);
        stream.lineTo(left + r, bottom);
        stream.curveTo(left + r - k, bottom, left, bottom + r - k, left, bottom + r);
        stream.lineTo(left, top - r);
        stream.curveTo(left, top - r + k, left + r - k, top, left + r, top);
        stream.closePath();
    }

}
