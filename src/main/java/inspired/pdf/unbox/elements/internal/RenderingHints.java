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

    /**
     * Replace the extra padding. Layouts set hints per render instead of accumulating them.
     */
    public RenderingHints setExtraPadding(Padding padding) {
        extraPadding = padding;
        return this;
    }

    /**
     * Clear all hints. Containers reset their children before measuring and rendering.
     */
    public RenderingHints reset() {
        extraPadding = Padding.of(0);
        return this;
    }

    public Padding getExtraPadding() {
        return extraPadding;
    }

}
