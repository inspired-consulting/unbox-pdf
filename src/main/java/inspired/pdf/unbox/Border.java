package inspired.pdf.unbox;

public record Border(float top, float right, float bottom, float left) {

    public static Border of(float m) {
        return new Border(m, m, m, m);
    }

    public static Border of(float vertical, float horizontal) {
        return new Border(vertical, horizontal, vertical, horizontal);
    }

    public static Border of(float top, float right, float bottom, float left) {
        return new Border(top, right, bottom, left);
    }

    public static Border of(float top, float vertical, float bottom) {
        return new Border(top, vertical, bottom, vertical);
    }

    public boolean isUniform() {
        return top == right && top == bottom && top == left;
    }

}
