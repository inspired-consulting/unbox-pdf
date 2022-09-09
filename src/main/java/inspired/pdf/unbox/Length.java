package inspired.pdf.unbox;

import java.util.Objects;

public final class Length {


    /**
     * The value of the length. PDFBox uses values that are
     * 1/72 inch when no UserUnit is set.
     */
    private final float value;

    private static final float UNITS_PER_INCH = 72;
    private static final float UNITS_PER_MM = UNITS_PER_INCH / 25.4f;


    private Length(float value) {
        this.value = value;
    }

    public static Length mm(float mmValue) {
        return new Length(mmValue * UNITS_PER_MM);
    }

    public static Length inch(float inchValue) {
        return new Length(inchValue * UNITS_PER_INCH);
    }

    static Length fromPdf(float pdfValue) {
        return new Length(pdfValue);
    }

    public float asMM() {
        return value / UNITS_PER_MM;
    }

    public float asInch() {
        return value / UNITS_PER_INCH;
    }

    float getValue() {
        return value;
    }

    public Length add(Length length) {
        return new Length(this.value + length.value);
    }

    public Length subtract(Length length) {
        return new Length(this.value - length.value);
    }

    public String toString() {
        return String.format("[%s mm]", asMM());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Length length = (Length) o;
        return Float.compare(length.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
