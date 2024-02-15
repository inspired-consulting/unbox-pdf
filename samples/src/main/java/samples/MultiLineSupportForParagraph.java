package samples;

import inspired.pdf.unbox.Border;
import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.decorators.BorderDecorator;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.Paragraph;
import inspired.pdf.unbox.elements.VerticalParagraph;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.*;
import java.io.IOException;

import static inspired.pdf.unbox.Unbox.*;

/**
 * Demonstrates the support for multi-line text in paragraphs.
 */
public class MultiLineSupportForParagraph {

    public static void main(String[] args) throws IOException {
        Document document = new Document();

        BorderDecorator border = new BorderDecorator(Border.of(1));
        BorderDecorator borderColumn = new BorderDecorator(Border.of(1), Color.BLUE);

        Container container = row(1, 5, 5, 5, 5, 5).with(border);
        container.add(new VerticalParagraph("Vertical Text\nNextLine").with(borderColumn));
        container.add(new Paragraph("Test Text\nNextLine\n SpaceNextLine \nNext Line").with(borderColumn));
        container.add(new Paragraph("Limited3\nNextLine\n SpaceNextLine \nNext Line").limit(3).with(borderColumn));
        container.add(new Paragraph("Limited3\nNextLine\n SpaceNextLine \nNext Line").limit(3).with(Padding.of(10)).with(borderColumn));
        container.add(new Paragraph("InnerHeight40\nNextLine\n SpaceNextLine \nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line").withInnerHeight(40).with(borderColumn));
        container.add(new Paragraph("InnerHeight40\nPadding10\n SpaceNextLine \nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line").withInnerHeight(40).with(Padding.of(10)).with(borderColumn));
        document.render(container);

        document.forward(40);

        container = rowStretch(1, 5, 5, 5, 5, 5).with(border);
        container.add(new VerticalParagraph("Stretch\nNextLine").with(borderColumn));
        container.add(new Paragraph("Test Text\nNextLine\n SpaceNextLine \nNext Line").with(borderColumn));
        container.add(new Paragraph("Limited3\nNextLine\n SpaceNextLine \nNext Line").limit(3).with(borderColumn));
        container.add(new Paragraph("Line\nwith\noverflow\n should \n be \n written \n even \n if \n too \n \n long").withOverflow(true).withInnerHeight(40).with(Padding.of(5)).with(borderColumn));
        container.add(new Paragraph("InnerHeight40\nNextLine\n SpaceNextLine \nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line").withInnerHeight(40).with(borderColumn));
        container.add(new Paragraph("InnerHeight40\nPadding10\n SpaceNextLine \nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line\nNext Line").withInnerHeight(40).with(Padding.of(10)).with(borderColumn));
        document.render(container);

        document.forward(40);

        PDDocument pdf = document.finish();
        pdf.save("./samples/out/MultiLineParagraph.pdf");
        pdf.close();
    }

}
