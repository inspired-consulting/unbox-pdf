package inspired.pdf.unbox;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifies physical-unit conversion and unit-aware box-model factories. */
class LengthTest {

    @Test
    void convertsAbsoluteUnitsToPoints() {
        assertEquals(72f, Length.of(1, Unit.INCH).points(), 0.0001f);
        assertEquals(12f, Length.of(1, Unit.PICA).points(), 0.0001f);
        assertEquals(10f, Length.of(1, Unit.CM).in(Unit.MM).value(), 0.0001f);
    }

    @Test
    void boxModelFactoriesConvertAtConstruction() {
        Length twoCm = Length.of(2, Unit.CM);
        assertEquals(new Margin(twoCm.points(), twoCm.points(), twoCm.points(), twoCm.points()), Margin.of(twoCm));
        assertEquals(new Padding(twoCm.points(), twoCm.points(), twoCm.points(), twoCm.points()), Padding.of(twoCm));
        assertEquals(new Border(twoCm.points(), twoCm.points(), twoCm.points(), twoCm.points(), 0), Border.of(twoCm));
        assertEquals(Length.of(1, Unit.CM).points(),
                Border.of(Length.of(1, Unit.CM)).withRadius(Length.of(1, Unit.CM)).radius(), 0.0001f);
    }

    @Test
    void rejectsInvalidLengthValuesAndUnits() {
        assertThrows(IllegalArgumentException.class, () -> Length.of(Float.NaN, Unit.MM));
        assertThrows(IllegalArgumentException.class, () -> Length.of(Float.POSITIVE_INFINITY, Unit.MM));
        assertThrows(NullPointerException.class, () -> Length.of(1, null));
        assertThrows(NullPointerException.class, () -> Length.of(1, Unit.MM).in(null));
    }
}
