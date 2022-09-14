package inspired.pdf.unbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LengthTest {

    @Test
    void fromMM() {
        var length = Length.mm(10f);
        assertEquals(28.346458f, length.getValue());
    }

    @Test
    void fromInch() {
        var length = Length.inch(10f);
        assertEquals(720f, length.getValue());
    }

    @Test
    void mmIsCalculatedToAndFromPdfValue() {
        var length = Length.mm(10f);
        assertNotEquals(10f, length.getValue());
        assertEquals(10f, length.asMM());
    }

    @Test
    void inchIsCalculatedToAndFromPdfValue() {
        var length = Length.inch(10f);
        assertNotEquals(10f, length.getValue());
        assertEquals(10f, length.asInch());
    }

    @Test
    void getValueReturnsPdfValue() {
        var length = Length.fromPdf(10f);
        assertEquals(10f, length.getValue());
    }

    @Test
    void lengthAreEqualIfValueIsEqual() {
        var a = Length.mm(10f);
        var b = Length.mm(10f);
        assertEquals(a, b);
    }

    @Test
    void addCanAddTwoPositiveLengths() {
        var a = Length.mm(10f);
        var b = Length.mm(5f);
        assertEquals(Length.mm(15f), a.add(b));
    }

    @Test
    void addCanAddPositiveAndNegativeLengths() {
        var a = Length.mm(10f);
        var b = Length.mm(-5f);
        assertEquals(Length.mm(5f), a.add(b));
    }

    @Test
    void subtractCanSubtractTwoPositiveLengths() {
        var a = Length.mm(10f);
        var b = Length.mm(5f);
        assertEquals(Length.mm(5f), a.subtract(b));
    }

    @Test
    void subtractCanSubtractPositiveAndNegativeLengths() {
        var a = Length.mm(10f);
        var b = Length.mm(-5f);
        assertEquals(Length.mm(15f), a.subtract(b));
    }

    @Test
    void testToString() {
        var length = Length.mm(10f);
        var s = length.toString();
        assertEquals("[10.0 mm]", s);
    }
}