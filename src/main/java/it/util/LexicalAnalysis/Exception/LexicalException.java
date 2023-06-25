package it.util.LexicalAnalysis.Exception;
import java.io.IOException;

public class LexicalException extends IOException {
    public LexicalException(String message, int l, int c) {
        super(message + " (L:" + (l + 1) + " C:" + (c + 1) + ")");
    }

    public LexicalException(String message) {
        super(message);
    }

    public Throwable fillInStackTrace() {
        return this;
    }
}
