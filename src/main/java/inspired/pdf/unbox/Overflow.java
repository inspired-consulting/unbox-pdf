package inspired.pdf.unbox;

/** How a paragraph handles text beyond its line limit or available height. */
public enum Overflow {
    /** Draw all lines without enlarging the space allocated to the paragraph. */
    OVERFLOW,
    /** Omit excess lines without a marker. This is the default. */
    CLIP,
    /** Mark omitted text with an ellipsis on the last visible line, if one fits. */
    ELLIPSIS
}
