package it.util.SemanticAnalysis;

import it.util.ReturnType;

import java.util.Arrays;
import java.util.LinkedList;

public class SemanticItem {
    private LinkedList<ReturnType> args = null;
    private ReturnType returnType;
    private boolean puntatore = false; //AKA Bestia di Satana

    public SemanticItem(Type type, ReturnType returnType) {
        if (type == Type.FUN)
            args = new LinkedList<>();
        this.returnType = returnType;
    }

    public SemanticItem(Type type, ReturnType returnType, boolean puntatore) {
        if (type == Type.FUN)
            args = new LinkedList<>();
        this.returnType = returnType;
        this.puntatore = puntatore;
    }

    public LinkedList<ReturnType> getArgs() {
        return args;
    }

    public void addArg(ReturnType r){
        args.add(r);
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public void changeReturnType(ReturnType rt){
        if (returnType == ReturnType.VAR)
            returnType = rt;
    }

    public boolean isPuntatore() {
        return puntatore;
    }

    @Override
    public String toString() {
        return (args != null ? "FUN " + Arrays.toString(args.toArray()) : "VAR") + " Ritorno: " + returnType;
    }
}
