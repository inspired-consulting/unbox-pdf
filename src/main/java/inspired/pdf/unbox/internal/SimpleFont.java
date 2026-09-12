package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Font;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.awt.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple immutable font info. Characters the underlying PDFBox font cannot encode are
 * replaced by a replacement character, a question mark by default, so that caller data
 * outside the font's encoding does not abort document generation. Configure
 * {@link #withReplacement(Character)} with {@code null} to fail with a clear exception instead.
 */
public class SimpleFont implements Font {

    public static final char DEFAULT_REPLACEMENT = '?';

    private final PDFont font;
    private final float size;
    private final Color color;
    private final Character replacement;
    // Encodability cache: a plain array for the Latin range, which covers most text and is
    // looked up on every measurement, and a map for everything else.
    private static final byte UNKNOWN = 0, YES = 1, NO = 2;
    private final byte[] latin = new byte[256];
    private final Map<Integer, Boolean> others = new ConcurrentHashMap<>();

    public static SimpleFont helvetica(float size) {
        return new SimpleFont(PDType1Font.HELVETICA, size);
    }

    public static Font helvetica_bold(float size) {
        return new SimpleFont(PDType1Font.HELVETICA_BOLD, size);
    }

    public static Font helvetica_bold(float size, Color color) {
        return new SimpleFont(PDType1Font.HELVETICA_BOLD, size, color);
    }

    public SimpleFont(PDFont font, float size) {
        this(font, size, Color.BLACK);
    }

    public SimpleFont(PDFont font, float size, Color color) {
        this(font, size, color, DEFAULT_REPLACEMENT);
    }

    public SimpleFont(PDFont font, float size, Color color, Character replacement) {
        this.font = font;
        this.size = size;
        this.color = color;
        this.replacement = replacement;
    }

    /**
     * Create a copy that replaces characters the font cannot encode with the given
     * character, or fails with a {@link PdfUnboxException} if {@code replacement} is null.
     */
    public SimpleFont withReplacement(Character replacement) {
        return new SimpleFont(font, size, color, replacement);
    }

    @Override
    public PDFont getFont() {
        return font;
    }

    @Override
    public float getSize() {
        return size;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public String encodable(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder result = null;
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            int length = Character.charCount(codePoint);
            if (!canEncode(codePoint)) {
                if (replacement == null) {
                    throw new PdfUnboxException(String.format(
                            "Character U+%04X is not available in font %s", codePoint, font.getName()));
                }
                if (result == null) {
                    result = new StringBuilder(text.length()).append(text, 0, i);
                }
                result.append(replacement.charValue());
            } else if (result != null) {
                result.appendCodePoint(codePoint);
            }
            i += length;
        }
        return result == null ? text : result.toString();
    }

    @Override
    public String toString() {
        return "SimpleFont[" + font.getName() + ", " + size + "]";
    }

    private boolean canEncode(int codePoint) {
        if (codePoint < latin.length) {
            byte state = latin[codePoint];
            if (state == UNKNOWN) {
                state = probe(codePoint) ? YES : NO;
                latin[codePoint] = state;
            }
            return state == YES;
        }
        return others.computeIfAbsent(codePoint, this::probe);
    }

    private boolean probe(int codePoint) {
        try {
            font.encode(new String(Character.toChars(codePoint)));
            return true;
        } catch (IllegalArgumentException | IOException e) {
            return false;
        }
    }

}
