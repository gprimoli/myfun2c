package it.util.SemanticAnalysis;

import java.util.Hashtable;
import java.util.Map;

public class SymbolTable {
    private static int autoincremental = 0;
    private final int symbolTableNumber;
    private final Hashtable<String, SemanticItem> symbolTable;

    public SymbolTable() {
        symbolTableNumber = autoincremental++;
        this.symbolTable = new Hashtable<>();
    }

    public boolean insert(String s, SemanticItem si) {
        return symbolTable.putIfAbsent(s, si) == null;
    }

    public SemanticItem get(String s) {
        return symbolTable.get(s);
    }

    public int getSymbolTableNumber(){
        return symbolTableNumber;
    }

    public void resetAutoIncremental(){
        autoincremental = 0;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder("Symbol Table #").append(symbolTableNumber).append("\n");
        for (Map.Entry<String, SemanticItem> entry : symbolTable.entrySet())
            out.append(entry.getKey()).append("->").append(entry.getValue()).append("\n");
        if(symbolTable.size() == 0)
            out.append("VUOTO: statement senza dichiariazioni");
        return out.toString();
    }
}
