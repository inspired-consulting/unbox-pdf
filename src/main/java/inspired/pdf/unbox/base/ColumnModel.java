package inspired.pdf.unbox.base;

public interface ColumnModel<C extends Column> extends Iterable<C> {

    int size();

    C get(int index);

    float getOverallWidth();

    default float width(int index) {
        return get(index).width();
    }

    <T extends ColumnModel<?>> T scale(float scale);

    <T extends ColumnModel<?>> T scaleToSize(float width);

}
