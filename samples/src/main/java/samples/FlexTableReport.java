package samples;

import inspired.pdf.unbox.Align;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.Font;
import inspired.pdf.unbox.Margin;
import inspired.pdf.unbox.Padding;
import inspired.pdf.unbox.Stroke;
import inspired.pdf.unbox.base.TableModel;
import inspired.pdf.unbox.elements.FlexTable;
import inspired.pdf.unbox.elements.TableRow;
import inspired.pdf.unbox.internal.SimpleFont;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;

import static inspired.pdf.unbox.Unbox.background;
import static inspired.pdf.unbox.Unbox.paragraph;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica;
import static inspired.pdf.unbox.internal.SimpleFont.helvetica_bold;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_100;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_300;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_600;
import static inspired.pdf.unbox.themes.UnboxTheme.GRAY_800;
import static inspired.pdf.unbox.themes.UnboxTheme.GREEN;
import static inspired.pdf.unbox.themes.UnboxTheme.RED_ORANGE;

/**
 * Demonstrates a {@link FlexTable}, where every row brings its own column model.
 * The sample renders a fictional quality report of ACME Inc. for a rocket stage.
 * Section rows span the full width, each section uses its own column layout,
 * and a summary row closes the table. The table has a horizontal margin, so
 * the column dividers must follow the reduced width of each row.
 */
public class FlexTableReport {

    private static final Font TEXT = helvetica(8);
    private static final Font BOLD = helvetica_bold(8);
    private static final Font PASS = helvetica_bold(8, GREEN);
    private static final Font FAIL = helvetica_bold(8, RED_ORANGE);
    private static final Font NOTE = helvetica_bold(8, GRAY_600);

    public static void main(String[] args) throws IOException {
        try (Document document = new Document()) {
            document.render(paragraph("ACME Inc. – Rocket Technology Division", helvetica_bold(16))
                .with(Margin.bottom(2)));
            document.render(paragraph("Quality Report: Orbital Booster OB-7, Serial 0042", helvetica_bold(11, GRAY_800))
                .with(Margin.bottom(2)));
            document.render(paragraph("Acceptance testing campaign, September 2026", new SimpleFont(PDType1Font.HELVETICA, 9, GRAY_600))
                .with(Margin.bottom(12)));

            FlexTable table = new FlexTable();
            table.with(Margin.of(0, 40, 10));
            table.withRowStroke(new Stroke(GRAY_300, 0.5f));
            table.withColumnStroke(Stroke.none());
            table.withCellPadding(Padding.of(4, 5));

            // Section 1: engine tests, four columns with a numeric result column
            TableModel engineColumns = new TableModel()
                .add("Component", 2f)
                .add("Test", 2.5f)
                .add("Measured", 1.2f, Align.RIGHT)
                .add("Status", 0.8f, Align.CENTER);
            table.addRow(sectionRow("1. Propulsion"));
            table.addRow(TableRow.header(engineColumns, BOLD, background(GRAY_100)));
            table.addRow(new TableRow(engineColumns, TEXT)
                .withCells("Main engine ME-3", "Hot fire, 120 s at 100 % thrust", "2,215 kN")
                .addCell("PASS", PASS));
            table.addRow(new TableRow(engineColumns, TEXT)
                .withCells("Main engine ME-3", "Throttle sweep 40 % to 110 %", "±0.6 %")
                .addCell("PASS", PASS));
            table.addRow(new TableRow(engineColumns, TEXT)
                .withCells("Turbopump TP-9", "Vibration at 31,000 rpm", "4.1 mm/s")
                .addCell("FAIL", FAIL));
            table.addRow(new TableRow(engineColumns, TEXT)
                .withCells("Gimbal actuator", "Slew rate, both axes", "12.4 °/s")
                .addCell("PASS", PASS));

            // Section 2: structural inspections, three columns with different widths
            TableModel structureColumns = new TableModel()
                .add("Assembly", 1.5f)
                .add("Inspection method", 2f)
                .add("Findings", 3f);
            table.addRow(sectionRow("2. Structures"));
            table.addRow(TableRow.header(structureColumns, BOLD, background(GRAY_100)));
            table.addRow(new TableRow(structureColumns, TEXT)
                .withCells("LOX tank dome", "Ultrasonic weld scan",
                    "No indications above 0.5 mm. Weld seams W1 to W8 released."));
            table.addRow(new TableRow(structureColumns, TEXT)
                .withCells("Interstage", "Proof load, 1.25 × limit load",
                    "Permanent deformation 0.02 mm, within tolerance."));
            table.addRow(new TableRow(structureColumns, TEXT)
                .withCells("Landing leg hinge", "Dye penetrant",
                    "Hairline crack at bolt hole 3. Part quarantined, see NCR-2026-118."));

            // Section 3: avionics, five columns with a short remark column
            TableModel avionicsColumns = new TableModel()
                .add("Unit", 1.5f)
                .add("Firmware", 1f)
                .add("Test cases", 0.9f, Align.RIGHT)
                .add("Passed", 0.9f, Align.RIGHT)
                .add("Remark", 2.2f);
            table.addRow(sectionRow("3. Avionics"));
            table.addRow(TableRow.header(avionicsColumns, BOLD, background(GRAY_100)));
            table.addRow(new TableRow(avionicsColumns, TEXT)
                .withCells("Flight computer A", "v4.2.1", "1,284", "1,284", "Redundant lane switch verified"));
            table.addRow(new TableRow(avionicsColumns, TEXT)
                .withCells("Flight computer B", "v4.2.1", "1,284", "1,283", "Timeout in self test, retest planned"));
            table.addRow(new TableRow(avionicsColumns, TEXT)
                .withCells("Telemetry radio", "v2.0.7", "312", "312", "Link margin 9 dB at 1,200 km"));

            // Summary: two columns, independent of the section layouts
            TableModel summaryColumns = TableModel.of(3f, 1f);
            table.addRow(new TableRow(summaryColumns, BOLD).with(background(GRAY_100))
                .addCell("Open non-conformances")
                .addCell("2", Align.RIGHT));
            table.addRow(new TableRow(summaryColumns, BOLD).with(background(GRAY_100))
                .addCell("Release recommendation")
                .addCell("HOLD", Align.RIGHT, FAIL));

            document.render(table);

            document.render(paragraph("Reviewed by the quality board on 2026-09-10. "
                + "Retest of turbopump TP-9 and replacement of the landing leg hinge are required before release.", NOTE)
                .with(Padding.of(2, 40)));

            PDDocument pdf = document.finish();
            pdf.save("./samples/out/FlexTableReport.pdf");
        }
    }

    /** A row with a single column spanning the full table width. */
    private static TableRow sectionRow(String title) {
        return new TableRow(TableModel.of(1f))
            .with(background(GRAY_300))
            .addCell(title, helvetica_bold(9));
    }

}
