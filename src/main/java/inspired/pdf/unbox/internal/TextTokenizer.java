package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Font;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Util to help split text about several lines if a maximum width is exceeded.
 */
public class TextTokenizer {

    private final Font font;

    public TextTokenizer(Font font) {
        this.font = font;
    }

    public String[] chunkMultiLine(String text, float maxWidth) {
        var chunkedLines = new ArrayList<>();
        var lines = text.split("\\n");
        for (var line : lines) {
            chunkedLines.addAll(Arrays.asList(chunk(line, maxWidth)));
        }
        return chunkedLines.toArray(new String[chunkedLines.size()]);
    }

    public String[] chunk(String text, float maxWidth) {
        if (isBlank(text)) {
            return new String[0];
        } else if (font.width(text) <= maxWidth) {
            return new String[] {text};
        } else {
            return rechunk(text.split("\\s"), maxWidth);
        }
    }

    private String[] rechunk(String[] words, float maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (String word : words) {
            if (currentLine.isEmpty() || match(currentLine, word, maxWidth)) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word);
            }
        }
        lines.add(currentLine.toString().trim());
        return lines.toArray(new String[0]);
    }

    private boolean match(StringBuilder sb, String next, float maxWidth) {
        return font.width(sb.toString() + " " + next) <= maxWidth;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

}
