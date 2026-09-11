package inspired.pdf.unbox.base;

public interface ColumnModel<C extends Column> extends Iterable<C> {

    int size();

    C get(int index);

    float getOverallWidth();

    default float width(int index) {
        return get(index).width();
    }

    /**
     * Create a copy with all column widths multiplied by the given factor.
     * Implementations narrow the return type to their own class.
     */
    ColumnModel<C> scale(float scale);

    /**
     * Create a copy whose column widths keep their proportions and sum to the given width.
     * Implementations narrow the return type to their own class.
     */
    ColumnModel<C> scaleToSize(float width);

    /**
     * Calculate the factor that scales the overall width to the target width.
     * An empty model has no width to scale and yields factor one.
     *
     * @throws IllegalArgumentException if the column widths do not sum to a positive value.
     */
    default float scaleFactor(float targetWidth) {
        if (size() == 0) {
            return 1f;
        }
        float overallWidth = getOverallWidth();
        if (!(overallWidth > 0)) {
            throw new IllegalArgumentException("Column widths must sum to a positive value, but " + this + " sums to " + overallWidth);
        }
        return targetWidth / overallWidth;
    }

}
