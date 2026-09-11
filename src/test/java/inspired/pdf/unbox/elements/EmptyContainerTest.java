package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.Unbox;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that empty containers render safely and preserve their allocated spacing.
 */
class EmptyContainerTest {

    @Test
    void emptyContainersRenderWithoutAdvancingTheCursor() {
        for (Container container : List.of(Unbox.row(), Unbox.rowStretch(), Unbox.column(), Unbox.columnStretch())) {
            try (Document document = new Document()) {
                document.addPage();
                float before = document.getPosition();
                document.render(container);
                assertEquals(before, document.getPosition(), 0.001f);
                document.finish();
            }
        }
    }

    @Test
    void emptyVerticalStretchPreservesMarginAndPadding() {
        try (Document document = new Document()) {
            document.addPage();
            float before = document.getPosition();
            document.render(Unbox.columnStretch().with(Margin.of(3)).with(Padding.of(5)));
            assertEquals(16, before - document.getPosition(), 0.001f);
            document.finish();
        }
    }
}
