package inspired.pdf.unbox;

import java.util.Objects;

/**
 * A piece of paragraph text drawn with one font. A paragraph consists of one or more
 * runs that flow together and wrap as one text; runs on the same line share the baseline.
 * @param text The text of the run; may contain line breaks
 * @param font The font of the run
 */
public record TextRun(String text, Font font) {

    public TextRun {
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(font, "font");
    }

}
