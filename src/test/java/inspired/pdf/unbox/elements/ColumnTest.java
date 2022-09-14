package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.Unbox;
import inspired.pdf.unbox.internal.SimpleFont;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ColumnTest {

    public static final SimpleFont HELVETICA_10 = SimpleFont.helvetica(10);

    @Test
    public void verify_column_height_is_sum_of_elements() {
        Container column = Unbox.column();
        column.add(new Paragraph("A").withInnerHeight(20));
        column.add(new Paragraph("B").withInnerHeight(20));
        column.add(new Paragraph("C").withInnerHeight(20));
        float height = column.innerHeight(new Bounds(0, 100, 100, 100));
        assertEquals(60, height);
    }

    @Test
    public void verify_column_height_is_sum_of_elements_regarding_margins_and_paddings() {
        Container column = Unbox.column();
        column.add(new Paragraph("A", HELVETICA_10).with(Padding.of(10)).with(Margin.of(10)));
        column.add(new Paragraph("B").withInnerHeight(20).with(Margin.of(10)));
        float height = column.innerHeight(new Bounds(0, 100, 100, 100));
        assertEquals(80 + HELVETICA_10.lineHeight(), height);
    }

}
