package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.base.TableModel;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdfparser.PDFStreamParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies that flexible table dividers follow row widths and horizontal margins. */
class FlexTableTest {

    @Test
    void dividersUseEachRowsWidthWithinMargins() throws IOException {
        try (Document document = new Document()) {
            FlexTable table = new FlexTable();
            table.with(Margin.of(0, 70, 0, 30));
            table.addRow(new TableRow(TableModel.of(1, 2)).withCells("One third", "Two thirds"));
            table.addRow(new TableRow(TableModel.of(3, 1)).withCells("Three quarters", "One quarter"));
            float left = document.getViewPort().left() + 30;
            float width = document.getViewPort().width() - 100;
            document.render(table);
            var pdf = document.finish();
            Path output = Path.of("target", "flex-table-margin.pdf");
            Files.createDirectories(output.getParent());
            pdf.save(output.toFile());


            PDFStreamParser parser = new PDFStreamParser(pdf.getPage(0));
            parser.parse();
            List<Object> tokens = parser.getTokens();
            List<Float> dividers = new ArrayList<>();
            float moveX = 0;
            float moveY = 0;
            for (int i = 2; i < tokens.size(); i++) {
                if (tokens.get(i) instanceof Operator operator) {
                    if (operator.getName().equals("m")) {
                        moveX = ((COSNumber) tokens.get(i - 2)).floatValue();
                        moveY = ((COSNumber) tokens.get(i - 1)).floatValue();
                    } else if (operator.getName().equals("l")) {
                        float x = ((COSNumber) tokens.get(i - 2)).floatValue();
                        float y = ((COSNumber) tokens.get(i - 1)).floatValue();
                        if (x == moveX && y != moveY) {
                            dividers.add(x);
                        }
                    }
                }
            }
            assertEquals(2, dividers.size());
            assertEquals(left + width / 3, dividers.get(0), 0.01f);
            assertEquals(left + width * 3 / 4, dividers.get(1), 0.01f);
        }
    }
}
