package it.util.SintaticAnalysis;

import it.util.ReturnType;
import it.util.SemanticAnalysis.SemanticNode;

public class SintaticItem {
    private SemanticNode currentSymbolTable;
    private final SintaticEnum name;
    private String value;
    private ReturnType rt;

    //    Contructor
    public SintaticItem(SintaticEnum name, String value) {
        this.name = name;
        this.value = value;
        this.rt = null;
        this.currentSymbolTable = null;
    }

    public SintaticItem(SintaticEnum name) {
        this.name = name;
        this.rt = null;
        this.currentSymbolTable = null;
    }


    //    Getter & Setter
    public SintaticEnum getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public ReturnType getAndSetReturnType() {
        if (rt == null)
            switch (name) {
                case VAR -> rt = ReturnType.VAR;
                case INTEGER, INTEGER_CONST -> rt = ReturnType.INTEGER;
                case BOOL, BOOL_CONST -> rt = ReturnType.BOOL;
                case REAL, REAL_CONST -> rt = ReturnType.REAL;
                case STRING, STRING_CONST -> rt = ReturnType.STRING;
                default -> rt = ReturnType.VOID;
            }
        return rt;
    }

    public ReturnType getReturnType() {
        ReturnType x = rt;
        if (rt == null)
            switch (name) {
                case VAR -> x = ReturnType.VAR;
                case INTEGER, INTEGER_CONST -> x = ReturnType.INTEGER;
                case BOOL, BOOL_CONST -> x = ReturnType.BOOL;
                case REAL, REAL_CONST -> x = ReturnType.REAL;
                case STRING, STRING_CONST -> x = ReturnType.STRING;
                default -> x = ReturnType.VOID;
            }
        return x;
    }

    public ReturnType setReturnType(ReturnType rt) {
        this.rt = rt;
        return rt;
    }

    public SemanticNode getCurrentSymbolTable() {
        return currentSymbolTable;
    }

    public void setCurrentSymbolTable(SemanticNode currentSymbolTable) {
        this.currentSymbolTable = currentSymbolTable;
    }

    //    ToString
    public String toString() {
        StringBuilder sb = new StringBuilder(String.valueOf(name));
        if (value != null)
            sb.append("[").append(value).append("]");
        sb.append("\n[RT: ").append(rt).append("]");
        return sb.toString();
    }
}
