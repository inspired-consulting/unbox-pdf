package inspired.pdf.unbox.elements.internal;

import inspired.pdf.unbox.Bounds;
import inspired.pdf.unbox.Document;
import inspired.pdf.unbox.decorators.Decoratable;
import inspired.pdf.unbox.decorators.Decorator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDecoratable implements Decoratable {

    private final List<Decorator> decorators = new ArrayList<>();

    public Decoratable with(Decorator decorator) {
        this.decorators.add(decorator);
        return this;
    }

    protected void applyDecorators(Document document, Bounds viewPort) {
        for (Decorator decorator : decorators()) {
            decorator.render(document, viewPort);
        }
    }

    protected List<Decorator> decorators() {
        return decorators;
    }

}
