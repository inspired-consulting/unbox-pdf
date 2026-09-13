package inspired.pdf.unbox;

import java.util.Objects;

/** A finite physical length that can be converted to PDF points or another unit. */
public record Length(float value, Unit unit) {

    /** Validate a length at the API boundary while preserving the existing float geometry internally. */
    public Length {
        Objects.requireNonNull(unit, "unit");
        if (!Float.isFinite(value)) {
            throw new IllegalArgumentException("Length value must be finite, but was " + value);
        }
    }

    /** Create a length in the given unit. */
    public static Length of(float value, Unit unit) {
        return new Length(value, unit);
    }

    /** Return this length expressed in PDF points. */
    public float points() {
        return value * unit.pointsPerUnit();
    }

    /** Return this length expressed in another unit. */
    public Length in(Unit target) {
        Objects.requireNonNull(target, "target");
        return new Length(points() / target.pointsPerUnit(), target);
    }
}
