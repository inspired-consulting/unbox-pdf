package samples;

import inspired.pdf.unbox.Border;
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

public class VerticalText {

    public static void main(String[] args) throws IOException {
        Document document = new Document();

        BorderDecorator border = new BorderDecorator(Border.of(1));
        BorderDecorator borderColumn = new BorderDecorator(Border.of(1), Color.BLUE);

        Container container = rowStretch(1, 10).with(border);
        container.add(new VerticalParagraph("Vertical Text").with(borderColumn));
        container.add(new Paragraph("Test Text").with(borderColumn));
        document.render(container);

        document.forward(40);

        container = row(1, 10).with(border);
        container.add(new VerticalParagraph("Vertical Text").with(borderColumn));
        container.add(new Paragraph("Ohne Stretch").with(borderColumn));
        document.render(container);

        document.forward(40);

        container = rowStretch(1, 10).with(border);
        container.add(new VerticalParagraph("Inner Height").withInnerHeight(90).with(background(Color.CYAN)).with(borderColumn));
        container.add(new Paragraph("Test Text").with(borderColumn));
        document.render(container);

        document.forward(40);

        container = rowStretch(1, 10).with(border);
        container.add(new VerticalParagraph("Vertical Text").with(Padding.of(20)).with(borderColumn));
        container.add(new Paragraph("Padding").with(Padding.of(20)).with(borderColumn));
        document.render(container);

        document.forward(40);

        container = rowStretch(1, 1, 20).with(border);
        container.add(new VerticalParagraph("Vertical Text2").with(borderColumn));
        container.add(new Paragraph("Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text " +
                "Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text ").with(borderColumn));
        document.render(container);

        document.forward(40);

        container = row(1, 1, 20).with(border);
        container.add(new VerticalParagraph("Vertical Text2").with(borderColumn));
        container.add(new Paragraph("Ohne Stretch Test Text Test Text Test Text Test Text Test Text Test Text " +
                "Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text ").with(borderColumn));
        document.render(container);

        document.forward(40);

        container = rowStretch(1, 1, 20).with(border);
        container.add(new VerticalParagraph("Inner height").withInnerHeight(90).with(background(Color.CYAN)).with(borderColumn));
        container.add(new Paragraph("Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text " +
                "Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text ").with(borderColumn));
        document.render(container);

        document.forward(40);

        container = row(1, 1, 20).with(border);
        container.add(new VerticalParagraph("Inner height").withInnerHeight(90).with(background(Color.CYAN)).with(borderColumn));
        container.add(new Paragraph("Ohne Stretch Text Test Text Test Text Test Text Test Text Test Text Test Text " +
                "Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text Test Text ").with(borderColumn));
        document.render(container);

        PDDocument pdf = document.finish();
        pdf.save("./samples/out/VerticalText.pdf");
        pdf.close();
    }

}
