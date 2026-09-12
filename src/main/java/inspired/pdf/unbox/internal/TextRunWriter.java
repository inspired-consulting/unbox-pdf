package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Overflow;
import inspired.pdf.unbox.TextRun;
import inspired.pdf.unbox.VAlign;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Measures and draws text consisting of several runs with different fonts. Words flow
 * across run boundaries and wrap as one text. Each line is as tall as its tallest run,
 * and all runs on a line share one baseline, so mixed sizes and colors align.
 */
public class TextRunWriter {

    private static final String ELLIPSIS = "…";

    private final Font baseFont;

    /**
     * @param baseFont The font that defines the height of a paragraph without any text
     */
    public TextRunWriter(Font baseFont) {
        this.baseFont = baseFont;
    }

    /**
     * Height of the first {@code lineLimit} lines the runs wrap into at the given width.
     */
    public float calculateHeight(List<TextRun> runs, float maxWidth, Integer lineLimit) {
        List<Line> lines = layout(runs, maxWidth);
        if (lines.isEmpty()) {
            return baseFont.lineHeight();
        }
        int count = lines.size();
        if (lineLimit != null && count > lineLimit) {
            count = lineLimit;
        }
        return heightOf(lines, count);
    }

    /**
     * Draw the runs into the bounds and return the height of the written lines.
     * Line limit, overflow handling, and alignment follow {@link TextWriter}.
     */
    public float write(PDPageContentStream stream, Bounds bounds, List<TextRun> runs,
                       Align align, VAlign vAlign, Integer lineLimit, Overflow overflow) {
        Objects.requireNonNull(overflow, "overflow");
        List<Line> all = layout(runs, bounds.width());
        List<Line> lines = all;
        int alignmentLines = all.size();
        if (overflow != Overflow.OVERFLOW) {
            int capacity = all.size();
            if (lineLimit != null) {
                capacity = Math.min(capacity, Math.max(0, lineLimit));
            }
            alignmentLines = capacity;
            // Same tolerance for floating-point height rounding as TextWriter.
            while (capacity > 0 && heightOf(all, capacity) * 0.99f > bounds.height()) {
                capacity--;
            }
            if (capacity < all.size()) {
                lines = new ArrayList<>(all.subList(0, capacity));
                if (overflow == Overflow.ELLIPSIS && capacity > 0) {
                    lines.set(capacity - 1, withEllipsis(lines.get(capacity - 1), bounds.width()));
                }
            }
        }
        float blockHeight = heightOf(all, overflow == Overflow.CLIP ? alignmentLines : lines.size());
        float lineTop = switch (vAlign) {
            case TOP -> bounds.top();
            case MIDDLE -> bounds.top() - (bounds.height() - blockHeight) / 2f;
            case BOTTOM -> bounds.top() - bounds.height() + blockHeight;
        };
        float written = 0f;
        for (Line line : lines) {
            float baseline = lineTop - line.height + line.height * TextWriter.CORRECTION_FACTOR;
            float x = offsetX(bounds, line.width(), align);
            for (Piece piece : line.pieces) {
                writePiece(stream, piece, x, baseline);
                x += piece.width();
            }
            lineTop -= line.height;
            written += line.height;
        }
        return written;
    }

    /**
     * Wrap the runs into lines at the given width. Words never break across runs, but a
     * line may contain pieces of several runs. Blank lines are dropped, as in {@link TextTokenizer}.
     */
    List<Line> layout(List<TextRun> runs, float maxWidth) {
        if (runs.stream().allMatch(run -> run.text().isBlank())) {
            return List.of();
        }
        TextTokenizer.requirePositiveWidth(maxWidth);
        Layout layout = new Layout(maxWidth);
        for (TextRun run : runs) {
            String text = run.text();
            StringBuilder word = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '\n') {
                    layout.word(word, run.font());
                    layout.lineBreak();
                } else if (Character.isWhitespace(c)) {
                    layout.word(word, run.font());
                    layout.space();
                } else {
                    word.append(c);
                }
            }
            layout.word(word, run.font());
        }
        return layout.finish();
    }

    // -- internal

    private static float heightOf(List<Line> lines, int count) {
        float height = 0f;
        for (int i = 0; i < Math.min(count, lines.size()); i++) {
            height += lines.get(i).height;
        }
        return height;
    }

    private static float offsetX(Bounds bounds, float lineWidth, Align align) {
        return switch (align) {
            case LEFT -> bounds.left();
            case CENTER -> (int) (bounds.center() - lineWidth / 2);
            case RIGHT -> (int) (bounds.left() + bounds.width() - lineWidth);
        };
    }

    private static void writePiece(PDPageContentStream stream, Piece piece, float x, float y) {
        Font font = piece.font;
        try {
            stream.beginText();
            stream.setNonStrokingColor(font.getColor());
            stream.setFont(font.getFont(), font.getSize());
            stream.newLineAtOffset(x, y);
            stream.showText(font.encodable(piece.text));
            stream.endText();
        } catch (IOException e) {
            throw new PdfUnboxException(e);
        }
    }

    /**
     * Trim the line from its end until an ellipsis fits, dropping emptied pieces.
     */
    private static Line withEllipsis(Line line, float maxWidth) {
        Line result = new Line();
        result.pieces.addAll(line.pieces);
        result.height = line.height;
        while (!result.pieces.isEmpty()) {
            Piece last = result.pieces.get(result.pieces.size() - 1);
            String text = last.text;
            while (true) {
                Piece candidate = new Piece(text.stripTrailing() + ELLIPSIS, last.font);
                result.pieces.set(result.pieces.size() - 1, candidate);
                if (result.width() <= maxWidth) {
                    return result;
                }
                if (text.isEmpty()) {
                    break;
                }
                text = text.substring(0, text.length() - 1);
            }
            result.pieces.remove(result.pieces.size() - 1);
        }
        result.pieces.add(new Piece(ELLIPSIS, line.pieces.get(line.pieces.size() - 1).font));
        return result;
    }

    /** A piece of one line drawn with one font. */
    record Piece(String text, Font font) {
        float width() {
            return font.width(text);
        }
    }

    /** One wrapped line: its pieces in drawing order and its height, the tallest font on it. */
    static final class Line {
        final List<Piece> pieces = new ArrayList<>();
        float height;

        float width() {
            float width = 0f;
            for (Piece piece : pieces) {
                width += piece.width();
            }
            return width;
        }

        String text() {
            StringBuilder text = new StringBuilder();
            pieces.forEach(piece -> text.append(piece.text));
            return text.toString();
        }
    }

    /** Greedy line filling shared by all runs of one paragraph. */
    private static final class Layout {
        private final float maxWidth;
        private final List<Line> lines = new ArrayList<>();
        private Line current = new Line();
        private boolean pendingSpace;

        Layout(float maxWidth) {
            this.maxWidth = maxWidth;
        }

        void space() {
            pendingSpace = true;
        }

        void lineBreak() {
            flushLine();
            pendingSpace = false;
        }

        void word(StringBuilder word, Font font) {
            if (word.isEmpty()) {
                return;
            }
            String text = word.toString();
            word.setLength(0);
            String candidate = pendingSpace && !current.pieces.isEmpty() ? " " + text : text;
            pendingSpace = false;
            if (current.width() + font.width(candidate) <= maxWidth) {
                append(candidate, font);
                return;
            }
            flushLine();
            if (font.width(text) <= maxWidth) {
                append(text, font);
                return;
            }
            // A single word wider than the line is broken into pieces that fit.
            String rest = text;
            while (!rest.isEmpty()) {
                int end = rest.length();
                while (end > 1 && font.width(rest.substring(0, end)) > maxWidth) {
                    end--;
                }
                append(rest.substring(0, end), font);
                rest = rest.substring(end);
                if (!rest.isEmpty()) {
                    flushLine();
                }
            }
        }

        List<Line> finish() {
            flushLine();
            return lines;
        }

        private void append(String text, Font font) {
            int last = current.pieces.size() - 1;
            if (last >= 0 && current.pieces.get(last).font == font) {
                current.pieces.set(last, new Piece(current.pieces.get(last).text + text, font));
            } else {
                current.pieces.add(new Piece(text, font));
            }
            current.height = Math.max(current.height, font.lineHeight());
        }

        private void flushLine() {
            if (!current.pieces.isEmpty()) {
                lines.add(current);
                current = new Line();
            }
        }
    }

}
