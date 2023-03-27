package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Font;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Util to help split text about several lines if a maximum width is exceeded.
 */
public class TextTokenizer {

    private final Font font;

    public TextTokenizer(Font font) {
        this.font = font;
    }

    public List<String> chunkMultiLine(String text, float maxWidth) {
        var chunkedLines = new ArrayList<String>();
        var lines = text.split("\\n");
        for (var line : lines) {
            chunkedLines.addAll(chunk(line, maxWidth));
        }
        return chunkedLines;
    }

    public List<String> chunk(String text, float maxWidth) {
        if (isBlank(text)) {
            return Collections.emptyList();
        } else if (font.width(text) <= maxWidth) {
            return List.of(text);
        } else {
            List<String> tokens = tokenize(text.split("\\s"), maxWidth);
            return rechunk(tokens, maxWidth);
        }
    }

    private List<String> rechunk(List<String> tokens, float maxWidth) {
        List<String> lines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        for (String word : tokens) {
            if (currentLine.isEmpty() || match(currentLine, word, maxWidth)) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word).append(" ");
            }
        }
        lines.add(currentLine.toString().trim());
        return lines;
    }

    private List<String> tokenize(String[] words, float maxWidth) {
        List<String> tokens = new ArrayList<>();
        for (String word : words) {
            if (match(word, maxWidth)) {
                tokens.add(word);
            } else {
                tokens.addAll(breakUp(word, maxWidth));
            }
        }
        return tokens;
    }

    private boolean match(StringBuilder sb, String next, float maxWidth) {
        return match(sb.toString() + " " + next, maxWidth);
    }

    private boolean match(String token, float maxWidth) {
        return font.width(token) <= maxWidth;
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().length() == 0;
    }

    private List<String> breakUp(String word, float maxWidth) {
        List<String> tokens = new ArrayList<>();
        float fullWidth = font.width(word);
        int lineCharSize = Math.max(1, (int) Math.floor(maxWidth / fullWidth * word.length()));
        for (int i = 0; i < word.length(); i += lineCharSize) {
            int endIndex = Math.min(word.length(), i + lineCharSize);
            tokens.add(word.substring(i, endIndex));
        }
        return tokens;
    }

}
