package org.jbehave.core.configuration;

import java.io.PrintStream;

public class PrintStreamAnnotationMonitor implements AnnotationMonitor {

    private final PrintStream output;

    public PrintStreamAnnotationMonitor() {
        this(System.out);
    }

    public PrintStreamAnnotationMonitor(PrintStream output) {
        this.output = output;
    }

    public void annotatedElementInvalid(Class<?> elementClass, Exception cause) {
        output.println("Annotated element invalid: " + elementClass);
        cause.printStackTrace(output);
    }
}