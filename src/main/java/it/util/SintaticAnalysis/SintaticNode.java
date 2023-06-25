package it.util.SintaticAnalysis;

import it.util.ReturnType;
import it.util.SemanticAnalysis.SemanticNode;
import it.util.tree.Node;

import java.util.LinkedList;
import java.util.List;

public class SintaticNode extends Node<SintaticItem> {
    //    Contructor
    public SintaticNode(SintaticItem info, Object... nodes) {
        super(info, nodes);
    }

    public SintaticNode(SintaticEnum name, Object... nodes) {
        super(new SintaticItem(name), nodes);
    }

    public SintaticNode(SintaticEnum name, String value, Object... nodes) {
        super(new SintaticItem(name, value), nodes);
    }


    //    Getter & Setter
    public SintaticEnum getName() {
        return getInfo().getName();
    }

    public String getValue() {
        return getInfo().getValue();
    }

    public ReturnType getReturnType() {
        return getInfo().getReturnType();
    }

    public ReturnType getAndSetReturnType() {
        return getInfo().getAndSetReturnType();
    }

    public ReturnType setReturnType(ReturnType rt) {
        return getInfo().setReturnType(rt);
    }

    public SemanticNode getCurrentSymbolTable() {
        return getInfo().getCurrentSymbolTable();
    }

    public void setCurrentSymbolTable(SemanticNode currentSymbolTable) {
        getInfo().setCurrentSymbolTable(currentSymbolTable);
    }


    //   Auto-Cast
    public List<SintaticNode> getChildCasted() {
        LinkedList<SintaticNode> r = new LinkedList<>();
        for (Node<SintaticItem> n : super.getChild())
            r.add((SintaticNode) n);
        return r;
    }

    public SintaticNode getFatherCasted() {
        return (SintaticNode) super.getFather();
    }


    public String content() {
        SemanticNode st = getCurrentSymbolTable();
        return getInfo().toString() + "\nSymbolTable #N." + (st != null ? st.getSymbolTableNumber() : "ERROR");
    }
}
