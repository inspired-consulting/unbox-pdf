package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Padding;

/**
 * Can be added to PdfElements while rendering to add temporary information.
 * Elements are free to ignore these!
 */
public class RenderingHints {

    private Padding extraPadding = Padding.of(0);

    public RenderingHints() { }

    public RenderingHints(RenderingHints copy) {
        extraPadding = copy.extraPadding;
    }

    public RenderingHints addExtraPadding(Padding padding) {
       extraPadding = extraPadding.add(padding);
       return this;
    }

    public Padding getExtraPadding() {
        return extraPadding;
    }

}
