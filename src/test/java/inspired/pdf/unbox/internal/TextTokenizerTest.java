package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Font;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextTokenizerTest {

    Font font = SimpleFont.helvetica(9);
    TextTokenizer tokenizer = new TextTokenizer(font);

    @Test
    void emptyTextReturnsNoLines() {
        List<String> lines = tokenizer.chunk("", 100);
        assertEquals(0, lines.size());
    }

    @Test
    void nullTextReturnsNoLines() {
        List<String> lines = tokenizer.chunk(null, 100);
        assertEquals(0, lines.size());
    }

    @Test
    void singleWordReturnsSingleLine() {
        List<String> lines = tokenizer.chunk("word", 100);
        assertEquals(1, lines.size());
        assertEquals("word", lines.get(0));
    }

    @Test
    void wordsWillBeBrokenIfTooLong() {
        List<String> lines = tokenizer.chunk("The term Autokatalogaufkleberentfernungsettikettendruckerhersteller is too long for one line.", 100);
        assertEquals(5, lines.size());
        assertEquals("The term", lines.get(0));
        assertEquals("Autokatalogaufkleberentf", lines.get(1));
        assertEquals("ernungsettikettendrucker", lines.get(2));
        assertEquals("hersteller is too long for", lines.get(3));
        assertEquals("one line.", lines.get(4));
    }

    @Test
    void canHandleVerySamllColumn() {
        List<String> lines = tokenizer.chunk("12345678901234567890123456789012345678901234567890", 1);
        assertEquals(50, lines.size());
        assertEquals("1", lines.get(0));
        assertEquals("0", lines.get(49));
    }

    @Test
    void lineBreaksStartNewLines() {
        List<String> lines = tokenizer.chunkMultiLine("here comes the\nline break.", 100);
        assertEquals(2, lines.size());
        assertEquals("here comes the", lines.get(0));
        assertEquals("line break.", lines.get(1));
    }

}