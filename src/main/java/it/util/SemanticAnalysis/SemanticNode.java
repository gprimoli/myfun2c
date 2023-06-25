package it.util.SemanticAnalysis;

import it.util.tree.Node;

import java.util.LinkedList;
import java.util.List;

public class SemanticNode extends Node<SymbolTable> {
    public SemanticNode() {
        super(new SymbolTable());
    }

    public boolean addToSymbolTable(String s, SemanticItem si) {
        return getInfo().insert(s, si);
    }

    public SemanticItem lookup(String s) {
        return lookupR(this, s, Type.VAR);
    }

    public SemanticItem lookup(String s, Type t) {
        return lookupR(this, s, t);
    }

    public SemanticItem localLookup(String s) {
        return getInfo().get(s);
    }

    public SemanticNode createScope() {
        addChild(new SemanticNode());
        List<SemanticNode> l = getChildCasted();
        return l.get(l.size() - 1);
    }

    public SemanticNode leaveScope() {
        return (SemanticNode) getFather();
    }

    public int getSymbolTableNumber() {
        return getInfo().getSymbolTableNumber();
    }

    //  Auto-Cast
    public List<SemanticNode> getChildCasted() {
        LinkedList<SemanticNode> r = new LinkedList<>();
        for (Node<SymbolTable> n : super.getChild())
            r.add((SemanticNode) n);
        return r;
    }

    public SemanticNode getFatherCasted() {
        return (SemanticNode) super.getFather();
    }


    private SemanticItem lookupR(SemanticNode n, String s, Type t) {
        if (n == null)
            return null;
        SemanticItem x = n.getInfo().get(s);
        if (x == null || (t == Type.FUN && x.getArgs() == null))
            return lookupR(n.getFatherCasted(), s, t);
        return x;
    }
}
