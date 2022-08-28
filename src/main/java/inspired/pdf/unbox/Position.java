package inspired.pdf.unbox;

/**
 * Represents the position on a PDF page.
 * @param x The offset from the left.
 * @param y The offset from the bottom.
 */
public record Position(float x, float y) {

    public Position moveDown(float amount) {
        return new Position(x, y - amount);
    }

    public Position moveUp(float amount) {
        return new Position(x, y + amount);
    }

}
