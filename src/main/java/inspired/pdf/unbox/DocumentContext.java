package inspired.pdf.unbox;

public interface DocumentContext {

    Margin getMargin();

    Padding getPadding();

    Bounds getPageBounds();

    Bounds getViewPort();

    Bounds getHeaderBounds();

    Bounds getFooterBounds();

    default Bounds getContentBounds() {
        return getPageBounds().apply(getMargin());
    }

}
