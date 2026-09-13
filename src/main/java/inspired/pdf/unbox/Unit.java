package inspired.pdf.unbox;

/** Absolute units supported by the PDF geometry API. */
public enum Unit {
    /** PDF points; 72 points equal one inch. */
    POINT(1f),
    /** Traditional pica; 12 points equal one pica. */
    PICA(12f),
    /** Physical inch. */
    INCH(72f),
    /** Centimetre. */
    CM(72f / 2.54f),
    /** Millimetre. */
    MM(72f / 25.4f);

    private final float pointsPerUnit;

    Unit(float pointsPerUnit) {
        this.pointsPerUnit = pointsPerUnit;
    }

    float pointsPerUnit() {
        return pointsPerUnit;
    }
}
