package samples;

import inspired.pdf.unbox.*;
import inspired.pdf.unbox.decorators.BorderDecorator;
import inspired.pdf.unbox.elements.*;
import inspired.pdf.unbox.elements.Container;
import inspired.pdf.unbox.elements.internal.HorizontalLayout;
import inspired.pdf.unbox.elements.internal.HorizontalStretchLayout;
import inspired.pdf.unbox.elements.internal.VerticalStretchLayout;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.*;
import java.io.IOException;

import static inspired.pdf.unbox.Unbox.*;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;

public class StretchingColumns {

    public static void main(String[] args) throws IOException {
        Document document = new Document();

        BorderDecorator border = new BorderDecorator(Border.of(1));
        BorderDecorator borderColumn = new BorderDecorator(Border.of(1), Color.BLUE);

        // add a paragraph
        document.render(paragraph("Kein Stretch", helvetica_bold(12)));
        Container container = new Container(new HorizontalLayout());

        document.render(container.with(border).with(Padding.of(5))
            .add(paragraph("Ohne Col").with(border))
            .add(column().with(borderColumn)
                    .add(paragraph("Col m Border")))
            .add(column().with(borderColumn)
                    .add(paragraph("Col m Border"))
                    .add(paragraph("Col m Border")))
            .add(column().with(borderColumn)
                    .add(paragraph("P mit Border").with(border))
                    .add(paragraph("P mit Border").with(border)))
            .add(new Container(new VerticalStretchLayout()).with(borderColumn)
                    .add(paragraph("VerticalStretch").with(border))
                    .add(paragraph("VerticalStretch + Center").with(border)))
            .add(column().with(borderColumn)
                    .add(paragraph("P mit Border").with(border))
                    .add(paragraph("P mB vBottom Länge").align(VAlign.BOTTOM).with(border)))
            .add(column().with(borderColumn)
                    .add(paragraph("P + Col mit Border").with(border))
                    .add(paragraph("P + Col mit Border Test3 Test3 Test3").with(border)))
            .add(column().with(borderColumn)
                    .add(paragraph("Col mit Border"))
                    .add(paragraph("Col mit Border"))
                    .add(paragraph("Col mit Border erzeugt Länge Border erzeugt Länge Border erzeugt Länge")))
        );

        document.forward(40);
        document.render(paragraph("Mit Horizontal Stretch", helvetica_bold(12)));
        container = new Container(new HorizontalStretchLayout());

        document.render(container.with(border).with(Padding.of(5))
            .add(paragraph("Ohne Col").with(border))
            .add(column().with(borderColumn)
                    .add(paragraph("Col m Border")))
            .add(column().with(borderColumn)
                    .add(paragraph("Col m Border"))
                    .add(paragraph("Col m Border")))
            .add(column().with(borderColumn)
                    .add(paragraph("P mit Border").with(border))
                    .add(paragraph("P mit Border").with(border)))
            .add(new Container(new VerticalStretchLayout()).with(borderColumn)
                    .add(paragraph("VerticalStretch").with(border))
                    .add(paragraph("VerticalStretch + Center").align(VAlign.MIDDLE).with(border)))
            .add(new Container(new VerticalStretchLayout()).with(borderColumn)
                    .add(paragraph("P mit Border").with(border))
                    .add(paragraph("P mB vBottom Länge").align(VAlign.BOTTOM).with(border)))
            .add(column().with(borderColumn)
                    .add(paragraph("P + Col mit Border").with(border))
                    .add(paragraph("P + Col mit Border Test3 Test3 Test3").with(border)))
            .add(column().with(borderColumn)
                    .add(paragraph("Col mit Border"))
                    .add(paragraph("Col mit Border"))
                    .add(paragraph("Col mit Border erzeugt Länge Border erzeugt Länge Border erzeugt Länge")))
        );

        PDDocument pdf = document.finish();
        pdf.save("./samples/out/StretchingPdf.pdf");
        pdf.close();
    }

}
