package inspired.pdf.unbox.internal;

import inspired.pdf.unbox.Font;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TextTokenizerTest {

    Font font = SimpleFont.helvetica(9);
    TextTokenizer tokenizer = new TextTokenizer(font);

    @Test
    void emptyTextReturnsNoLines() {
        String[] lines = tokenizer.chunk("", 100);
        assertEquals(0, lines.length);
    }

    @Test
    void nullTextReturnsNoLines() {
        String[] lines = tokenizer.chunk(null, 100);
        assertEquals(0, lines.length);
    }

    @Test
    void singleWordReturnsSingleLine() {
        String[] lines = tokenizer.chunk("word", 100);
        assertEquals(1, lines.length);
        assertEquals("word", lines[0]);
    }

    @Test
    void tooLongWordIsInOneOwnLine() {
        String[] lines = tokenizer.chunk("Das ist ein Wortdasvielzulangundohneunterbrechung ist.", 100);
        assertEquals(3, lines.length);
        assertEquals("Das ist ein", lines[0]);
        assertEquals("Wortdasvielzulangundohneunterbrechung", lines[1]);
        assert(font.width("Wortdasvielzulangundohneunterbrechung") > 100);
        assertEquals("ist.", lines[2]);
    }

    @Test
    void lineBreaksStartNewLines() {
        String[] lines = tokenizer.chunkMultiLine("here comes the\nline break.", 100);
        assertEquals(2, lines.length);
        assertEquals("here comes the", lines[0]);
        assertEquals("line break.", lines[1]);
    }
}