package inspired.pdf.unbox;

import java.awt.*;

import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_700;

/**
 * Represents a stroke with width and color.
 * @param color The color.
 * @param width The width.
 */
public record Stroke(Color color, float width) {

    public static Stroke none() {
        return new Stroke(Color.white, 0);
    }

    public Stroke(Color color, float width) {
        this.width = width;
        this.color = color;
    }

    public Stroke(float width) {
        this(GRAY_700, width);
    }

    public Stroke(Color color) {
        this(color, 0.5f);
    }

    public boolean isEmpty() {
        return width == 0;
    }

}
