package inspired.pdf.unbox.decorators;

/**
 * Anything that can be wrapped by a Decorator.
 */
public interface Decoratable {

    /**
     * Add a decorator.
     * @param decorator The decorator.
     * @return The element to continue with, either the wrapped element or the decorator.
     */
    Decoratable with(Decorator decorator);

}
