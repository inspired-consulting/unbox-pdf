package inspired.pdf.unbox.elements;

import inspired.pdf.unbox.decorators.Decorator;

public interface Table extends PdfElement {

    TableRow addRow();

    TableRow addRow(TableRow row);

    TableRow addHeader(TableRow row);

    @Override
    Table with(Decorator decorator);

}
