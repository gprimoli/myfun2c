package it.util.SemanticAnalysis.Exception;

import it.util.SintaticAnalysis.SintaticNode;

public class SemanticException extends Exception{
    public SemanticException() {
    }

    public SemanticException(String message) {
        super(message);
    }

    public SemanticException(String message, SintaticNode n) {
        super(message + n.getName());
    }

    public Throwable fillInStackTrace() {
        return this;
    }
}
