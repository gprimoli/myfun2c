package it;

import java.io.File;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import it.util.*;
import it.util.tree.*;
import it.util.SintaticAnalysis.*;
import it.util.SemanticAnalysis.*;
import it.util.SemanticAnalysis.Exception.*;

public class Semantiker {
    private final SintaticNode root;
    private SemanticNode symbolTable;
    private String dirSource;
    private boolean debug;

    public Semantiker(SintaticNode root, File dirSource, boolean debug) {
        this.root = root;
        this.dirSource = Util.getPath(dirSource);
        this.debug = debug;
        symbolTable = new SemanticNode();
    }

    public void analizza() throws SemanticException {
        try {
            analizzaScopeAndInferenza(root);
            analizzaTypeSystem(root);
        } catch (Exception e) {
            debug();
            symbolTable.getInfo().resetAutoIncremental();
            throw e;
        } finally {
            debug();
            symbolTable.getInfo().resetAutoIncremental();
        }
    }


    //---------------------------------------------------------------------------------------------------//


    //  TypeSystem
    private ReturnType analizzaTypeSystem(SintaticNode n) throws SemanticException {
        ReturnType rt = n.getAndSetReturnType();
        if (rt != ReturnType.VOID) return rt;

        SemanticNode currentSymbolTable = n.getCurrentSymbolTable();
        List<SintaticNode> childs = n.getChildCasted();

        switch (n.getName()) {
            case ID -> n.setReturnType(currentSymbolTable.lookup(n.getValue()).getReturnType());
            case RETURN -> {
                SintaticNode funNode = getFathersInfo(n, SintaticEnum.FUN);
                SemanticItem funItem = currentSymbolTable.lookup(funNode.getValue(), Type.FUN);
                SintaticNode child = childs.get(0);
                ReturnType nodeReturn;
                if (child.getName() == SintaticEnum.ID)
                    nodeReturn = n.setReturnType(currentSymbolTable.lookup(child.getValue()).getReturnType());
                else nodeReturn = n.setReturnType(analizzaTypeSystem(child));
                if (funItem.getReturnType() != nodeReturn)
                    throw new SemanticException("Il return non rispetta la dichiarazione della funzione: " + funNode.getValue());
            }
            case ASSIGNSTAT -> {
                SemanticItem id = currentSymbolTable.lookup(childs.get(0).getValue());
                ReturnType idRT = id.getReturnType();
                ReturnType expr = analizzaTypeSystem(childs.get(1));
                if (idRT == ReturnType.VAR)
                    id.changeReturnType(expr);
                else if (idRT != expr && !couldBeCasted(idRT, expr))//intero -> reale
                    throw new SemanticException("TypeMismatch assegnazione: " + childs.get(0).getValue());

                SintaticNode varDeclNode = getFathersInfo(n, SintaticEnum.VARDECL);
                if (varDeclNode != null) {
                    SintaticNode varNode = varDeclNode
                            .getChildCasted()
                            .get(0);
                    if (varNode.getName() == SintaticEnum.VAR) {
                        if (varNode.getAndSetReturnType() == ReturnType.VAR)
                            varNode.setReturnType(expr);
                        else varNode.setReturnType(ReturnType.VAR);
                    }
                }
            }
            case WHILESTAT, IFSTAT -> {
                if (analizzaTypeSystem(childs.get(0)) != ReturnType.BOOL)
                    throw new SemanticException("Boolean richiesto sul nodo: ", n);
            }
            case CALLFUN -> {
                SemanticItem funItem = currentSymbolTable.lookup(n.getValue(), Type.FUN);
                if (funItem == null) throw new SemanticException("Funzione utilizzata senza precedente dichiarazione");
                LinkedList<ReturnType> args = funItem.getArgs();
                LinkedList<ReturnType> params = new LinkedList<>();
                getParams(n, params);
                if (args.size() != params.size())
                    throw new SemanticException("I parametri della funzione non combaciano per numero con quelli dichiarati nella funzione: " + n.getValue());
                else {
                    for (int i = 0; i < childs.size(); i++) {
                        if (params.get(i) != args.get(i))
                            throw new SemanticException("I parametri della funzione non combaciano per tipo con quelli dichiarati nella funzione:" + n.getValue());
                    }
                }
                n.setReturnType(funItem.getReturnType());
            }
            case READSTAT -> {
                if (childs.size() == 2 && analizzaTypeSystem(childs.get(1)) == ReturnType.VOID)
                    throw new SemanticException("TypeMismatch, stai provado a leggere un valore e il secondo parametro restituisce VOID");
            }
            case WRITESTAT -> {
                if (analizzaTypeSystem(childs.get(0)) == ReturnType.VOID)
                    throw new SemanticException("TypeMismatch, stai provado a scrivere un parametro che restituisce VOID");
            }
            case EXPR -> {
                if (childs.size() == 2)
                    n.setReturnType(checkCompatibility(childs.get(0), childs.get(1), n.getValue()));
                else if (childs.size() == 1)
                    n.setReturnType(checkCompatibility(childs.get(0), n.getValue()));
            }
        }
        for (SintaticNode c : childs)
            analizzaTypeSystem(c);
        return n.getAndSetReturnType();
    }

    private ReturnType checkCompatibility(SintaticNode n1, SintaticNode n2, String op) throws SemanticException {
        ReturnType n1Type = analizzaTypeSystem(n1);
        ReturnType n2Type = analizzaTypeSystem(n2);
        final boolean noStringNoBool = n1Type != ReturnType.BOOL && n2Type != ReturnType.BOOL && n1Type != ReturnType.STRING && n2Type != ReturnType.STRING;
        switch (op) {
            case "DIV" -> {
                if (noStringNoBool)
                    return ReturnType.REAL;
            }
            case "DIVINT" -> {
                if (noStringNoBool)
                    return ReturnType.INTEGER;
            }
            case "PLUS", "MINUS", "TIMES", "POW" -> {
                if(noStringNoBool){
                    if (n1Type == ReturnType.REAL || n2Type == ReturnType.REAL)
                        return ReturnType.REAL;
                    if (n1Type == ReturnType.INTEGER && n2Type == ReturnType.INTEGER)
                        return ReturnType.INTEGER;
                }
            }
            case "STR_CONCAT" -> {
                if (n1Type == ReturnType.STRING)
                    return ReturnType.STRING;
            }
            case "GT", "GE", "LT", "LE" -> {
                if (n1Type != ReturnType.STRING && n2Type != ReturnType.STRING)
                    return ReturnType.BOOL;
            }
            case "EQ", "NE" -> {
                if (n1Type == n2Type)
                    return ReturnType.BOOL;
            }
            case "AND", "OR" -> {
                if (n1Type == ReturnType.BOOL && n2Type == ReturnType.BOOL)
                    return ReturnType.BOOL;
            }
        }
        throw new SemanticException("Compatibiltà dell'operazione non valida: " + n1.getValue() + " " + op + " " + n2.getValue());
    }

    private ReturnType checkCompatibility(SintaticNode n1, String op) throws SemanticException {
        ReturnType n1Type = analizzaTypeSystem(n1);
        switch (op) {
            case "MINUS" -> {
                if (n1Type == ReturnType.INTEGER || n1Type == ReturnType.REAL)
                    return n1Type;
            }
            case "NOT" -> {
                if (n1Type == ReturnType.BOOL)
                    return ReturnType.BOOL;
            }
            case "PAR" -> {
                return n1Type;
            }
        }
        throw new SemanticException("Compatibiltà dell'operazione non valida: " + n1.getValue() + " " + op);
    }

    private boolean couldBeCasted(ReturnType n1, ReturnType n2) {
        return n1 == ReturnType.REAL && n2 == ReturnType.INTEGER;
    }

    private void getParams(SintaticNode n, LinkedList<ReturnType> params) throws SemanticException {
        for (SintaticNode child : n.getChildCasted())
            if (child.getName() == SintaticEnum.EXPR || SintaticEnum.isConst(child))
                params.add(analizzaTypeSystem(child));
            else if (child.getName() == SintaticEnum.ID)
                params.add(n.getCurrentSymbolTable().lookup(child.getValue()).getReturnType());
            else
                getParams(child, params);
    }


    //---------------------------------------------------------------------------------------------------//


    //  ScopeAndInferenza
    private void analizzaScopeAndInferenza(SintaticNode n) throws SemanticException {
        n.setCurrentSymbolTable(symbolTable);

        SintaticItem nodeInfo = n.getInfo();
        List<SintaticNode> childs = n.getChildCasted();

        switch (n.getName()) {
            case VARDECL -> {
                SintaticNode firstChild = childs.get(0);
                List<SintaticNode> ids = new LinkedList<>();
                getAllIds(n, ids);
                for (SintaticNode id : ids)
                    addTosymbolTable(id, firstChild);
            }
            case ASSIGNSTAT -> {
                SintaticNode vardeclNodo = getFathersInfo(n, SintaticEnum.VARDECL);
                if (vardeclNodo != null) {
                    //nel caso non viene chiamato in vardecl ma in un assegnamento normale
                    ReturnType constReturn;
                    List<SintaticNode> vardeclChilds = vardeclNodo.getChildCasted();
                    if (vardeclChilds.get(0).getName() != SintaticEnum.VAR)
                        addTosymbolTable(childs.get(0), vardeclChilds.get(0));
                    else if ((constReturn = childs.get(1).getReturnType()) != null && constReturn != ReturnType.VOID)
                        addTosymbolTable(childs.get(0), Type.VAR, constReturn);
                    else
                        addTosymbolTable(childs.get(0), Type.VAR, ReturnType.VAR);//Temporaneo
                }
            }
            case PARDECL -> {
                SintaticNode funNode = getFathersInfo(n, SintaticEnum.FUN);
                SemanticItem funItem = n.getCurrentSymbolTable().lookup(funNode.getValue());
                if (childs.size() > 2) {
                    SemanticItem si = new SemanticItem(Type.VAR, childs.get(0).getReturnType(), true);
                    addTosymbolTable(childs.get(2), si);
                    funItem.addArg(childs.get(0).getReturnType());
                } else {
                    addTosymbolTable(childs.get(1), childs.get(0));
                    funItem.addArg(childs.get(0).getReturnType());
                }
            }
            case FUN -> {
                SemanticItem s;
                SintaticNode funType = null;
                SintaticEnum[] possible = {SintaticEnum.INTEGER, SintaticEnum.BOOL, SintaticEnum.REAL, SintaticEnum.STRING};
                for (SintaticNode child : childs) {
                    if (funType != null) break;
                    for (SintaticEnum se : possible)
                        if (se == child.getName()) {
                            funType = child;
                            break;
                        }
                }
                if (funType != null) {
                    s = new SemanticItem(Type.FUN, funType.getReturnType());
                    if (getChildsInfo(n, SintaticEnum.RETURN) == null)
                        throw new SemanticException("Return mancante alla funzione: " + nodeInfo.getValue());
                } else {
                    s = new SemanticItem(Type.FUN, ReturnType.VOID);
                    if (getChildsInfo(n, SintaticEnum.RETURN) != null)
                        throw new SemanticException("Return non consentito alla funzione: " + nodeInfo.getValue() + ", è staa dichiarata void");
                }
                addTosymbolTable(nodeInfo.getValue(), s);
            }
            case ID -> {
//              Siccome lo visito dopo VARDECL in questo caso controllo
//              se una variabile è stata dichiarata prima del suo utilizzo
                if (symbolTable.lookup(nodeInfo.getValue()) == null) {
                    throw new SemanticException("Mancata dichiarazione della variabile " + nodeInfo.getValue());
                }
            }
        }

        switch (n.getName()) {
            case FUN, MAIN, IFSTAT, ELSESTAT, WHILESTAT -> symbolTable = symbolTable.createScope();
        }

        for (SintaticNode c : childs)
            analizzaScopeAndInferenza(c);

        switch (n.getName()) {
            case FUN, MAIN, IFSTAT, ELSESTAT, WHILESTAT -> symbolTable = symbolTable.leaveScope();
        }
    }

    private SintaticNode getFathersInfo(SintaticNode n, SintaticEnum aboutWho) {
        SintaticNode father = n.getFatherCasted();
        if (father == null)
            return null;
        return father.getName() == aboutWho ? father : getFathersInfo(father, aboutWho);
    }

    private SintaticNode getChildsInfo(SintaticNode n, SintaticEnum aboutWho) {
        //È più probabile che il return si trova alla fine dello statment (utilizzo questa fun solo per quello)
        SintaticNode tmp;
        List<SintaticNode> childs = n.getChildCasted();
        for (int i = childs.size() - 1; i > -1; i--) {
            SintaticNode child = childs.get(i);
            if (child.getName() == aboutWho)
                return child;
            else if ((tmp = getChildsInfo(child, aboutWho)) != null)
                return tmp;
        }
        return null;
    }

    private void getAllIds(SintaticNode n, List<SintaticNode> l) {
        for (SintaticNode child : n.getChildCasted())
            if (child.getName() == SintaticEnum.ID)
                l.add(child);
            else if (child.getName() != SintaticEnum.ASSIGNSTAT)
                getAllIds(child, l);
    }


    //---------------------------------------------------------------------------------------------------//


    //iD Tipo FUN/VAR
    private void addTosymbolTable(String id, SemanticItem si) throws SemanticException {
        if (!symbolTable.addToSymbolTable(id, si))
            throw new SemanticException("Variabile " + id + "già dichiarata nel seguente scope");
    }

    private void addTosymbolTable(SintaticNode n1, Type t, ReturnType rt) throws SemanticException {
        SemanticItem si = new SemanticItem(t, rt);
        String id = n1.getValue();
        addTosymbolTable(id, si);
    }

    private void addTosymbolTable(SintaticNode n1, SintaticNode n2) throws SemanticException {
        SemanticItem si = new SemanticItem(Type.VAR, n2.getReturnType());
        String id = n1.getValue();
        addTosymbolTable(id, si);
    }

    private void addTosymbolTable(SintaticNode n1, SemanticItem si) throws SemanticException {
        String id = n1.getValue();
        addTosymbolTable(id, si);
    }


    //---------------------------------------------------------------------------------------------------//


    private void debug() {
        if (!debug) return;
        while (!symbolTable.isRoot())
            symbolTable = symbolTable.getFatherCasted();
        Node.debug(symbolTable, dirSource, "SymbleTableTree");
        Node.debug(root, dirSource, "SintaxTree");
    }
}
