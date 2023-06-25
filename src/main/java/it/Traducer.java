package it;

import it.util.ReturnType;
import it.util.SintaticAnalysis.SintaticEnum;
import it.util.SintaticAnalysis.SintaticNode;
import it.util.Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;


import java.awt.datatransfer.StringSelection;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

public class Traducer {
    private final SintaticNode root;
    private final File source, dirLibs;

    public Traducer(SintaticNode root, File dirLibs, File source) {
        this.root = root;
        this.dirLibs = dirLibs;
        this.source = source;
    }

    public void traduce() {
        String code = traduce(root);
        creaMain(code);
        creaMakeFile();
        copyToClipboard(code);
    }

    private String traduce(SintaticNode n) {
        StringBuilder sb = new StringBuilder();
        List<SintaticNode> childs = n.getChildCasted();

        switch (n.getName()) {
            case BOOL_CONST, INTEGER_CONST, REAL_CONST, ID -> sb.append(n.getValue());
            case NULL -> sb.append("NULL");
            case STRING_CONST -> sb.append("\"").append(n.getValue()).append("\"");

            case STRING -> sb.append("String ");
            case INTEGER, BOOL -> sb.append("int ");
            case REAL -> sb.append("double ");
            case COMMA -> sb.append(", ");
            case OUT -> sb.append(" *");
            case OUTPAR -> sb.append(" &");
            case RETURN -> sb.append("return ").append(traduce(childs.get(0))).append(";\n");

            case PROGRAM -> {
                sb.append("#include \"main.h\"\n");
                sb.append("\nstatic tgc_t gc;\n");
                sb.append("tgc_t *getGc(){ return &gc; }\n\n");

                for (SintaticNode s : childs)
                    sb.append(traduce(s));
            }
            case MAIN -> {
                sb.append("\nint main(int argc, char *argv[]){\ntgc_start(&gc, &argc);\n\n");
                for (SintaticNode s : childs)
                    sb.append(traduce(s));
                sb.append("\ntgc_stop(&gc);\nreturn 0;\n}\n");
            }
            case VARDECL -> {
                ReturnType typeReturn = childs.get(0).getReturnType();
                if (typeReturn != ReturnType.VAR)
                    sb.append(traduce(new SintaticNode(SintaticEnum.evaluate(typeReturn))));
                sb.append(traduce(childs.get(1)));
                sb.append(";\n");
            }
            case IDLISTINITOBBL -> {
                if (childs.size() == 1)
                    sb.append(traduce(childs.get(0)));
                else {
                    int i = 0;
                    List<ReturnType> idsRetType = new LinkedList<>();
                    getAllreturnType(n, idsRetType);
                    for (SintaticNode child : childs) {
                        if (child.getName() == SintaticEnum.ASSIGNSTAT) {
                            SintaticEnum sintaticEnum = SintaticEnum.valueOf(idsRetType.get(i++).toString());
                            sb.append(traduce(new SintaticNode(sintaticEnum))).append(traduce(child));
                        } else if (child.getName() == SintaticEnum.COMMA)
                            sb.append("; ");
                    }
                }
            }
            case STATLIST -> {
                for (SintaticNode s : childs) {
                    sb.append(traduce(s));
                    switch (s.getName()) {
                        case ASSIGNSTAT, CALLFUN -> sb.append(";\n");
                    }
                }
            }

            case FUN -> {
                int i = 0;
                sb.append("\n\n");
                ReturnType funType = n.getCurrentSymbolTable().lookup(n.getValue()).getReturnType();
                if (funType != ReturnType.VOID){
                    i++;
                    sb.append(traduce(new SintaticNode(SintaticEnum.evaluate(funType))));
                } else
                    sb.append("void ");

                sb.append(n.getValue()).append("(");
                for (SintaticNode child : childs) {
                    i++;
                    if (child.getName() == SintaticEnum.PARAMDECLLIST || child.getName() == SintaticEnum.PARDECL){
                        sb.append(traduce(child));
                        break;
                    }
                }
                sb.append("){\n");

                while (i < childs.size())
                    sb.append(traduce(childs.get(i++)));

                sb.append("}\n");
            }
            case IFSTAT -> {
                sb.append("if(")
                        .append(traduce(childs.get(0)))
                        .append("){\n");
                for (int i = 1; i < childs.size(); i++)
                    sb.append(traduce(childs.get(i)));
                sb.append("}\n");
            }
            case ELSESTAT -> {
                sb.append("} else {\n");
                for (SintaticNode child : childs) sb.append(traduce(child));
            }
            case WHILESTAT -> {
                sb.append("while(")
                        .append(traduce(childs.get(0)))
                        .append("){\n");
                for (int i = 1; i < childs.size(); i++)
                    sb.append(traduce(childs.get(i)));
                sb.append("}\n");
            }
            case READSTAT -> {
                List<SintaticNode> ids = new LinkedList<>();
                getAllIds(childs.get(0), ids);
                if (childs.size() == 2)
                    sb.append("print(").append(traduce(childs.get(1))).append(", \"\\n\");\n");
                for (SintaticNode id : ids)
                    sb.append("scan(&").append(traduce(id)).append(");\n");
            }
            case IDLIST -> {
                //Non viene mai utilizzato perché ho modificato readstat ...
                //alternativa è creare una funzione scan che prende in input più variabili.
                if (childs.size() == 1)
                    sb.append("&").append(traduce(childs.get(0)));
                else
                    sb.append(traduce(childs.get(0)))
                            .append(traduce(childs.get(1)))
                            .append("&").append(traduce(childs.get(2)));
            }
            case WRITESTAT -> {
                sb.append("print(").append(traduce(childs.get(0))).append(", \"");
                if (n.getValue() != null)
                    switch (n.getValue()) {
                        case "LN" -> sb.append("\\n");
                        case "ET" -> sb.append(" ");
                        case "EB" -> sb.append("\\t");
                    }
                sb.append("\");\n");
            }
            case ASSIGNSTAT -> {
                SintaticNode idNode = childs.get(0);
                //da gestire il puntatore se è un parametro di output
                if (idNode.getCurrentSymbolTable().lookup(idNode.getValue()).isPuntatore())
                    sb.append("*");
                sb.append(traduce(childs.get(0))).append(" = ");
                SintaticNode secondoNodo = childs.get(1);
                if (secondoNodo.getReturnType() == ReturnType.STRING && secondoNodo.getName() != SintaticEnum.ID) {
                    sb.append("creaString(").append(traduce(secondoNodo)).append(")");
                } else {
                    sb.append(traduce(secondoNodo));
                }
            }
            case CALLFUN -> {
                sb.append(n.getValue()).append("(");
                for (SintaticNode child : childs)
                    sb.append(traduce(child));
                sb.append(")");
            }
            case EXPR -> {
//                sb.append("(");
                if (childs.size() > 1) {
                    switch (n.getValue()) {
                        case "PLUS" -> sb.append(traduce(childs.get(0))).append(" + ").append(traduce(childs.get(1)));
                        case "MINUS" -> sb.append(traduce(childs.get(0))).append(" - ").append(traduce(childs.get(1)));
                        case "TIMES" -> sb.append(traduce(childs.get(0))).append(" * ").append(traduce(childs.get(1)));
                        case "DIV" -> sb.append(traduce(childs.get(0))).append(" / ").append(traduce(childs.get(1)));
                        case "DIVINT" -> sb.append("ceil(").append(traduce(childs.get(0))).append(" / ").append(traduce(childs.get(1))).append(")");
                        case "AND" -> sb.append(traduce(childs.get(0))).append(" && ").append(traduce(childs.get(1)));
                        case "POW" -> sb.append("pow(").append(traduce(childs.get(0))).append(", ").append(traduce(childs.get(1))).append(")");
                        case "STR_CONCAT" -> sb.append("concat(").append(traduce(childs.get(0))).append(", ").append(traduce(childs.get(1))).append(")");
                        case "OR" -> sb.append(traduce(childs.get(0))).append(" || ").append(traduce(childs.get(1)));
                        case "GT" -> sb.append(traduce(childs.get(0))).append(" > ").append(traduce(childs.get(1)));
                        case "GE" -> sb.append(traduce(childs.get(0))).append(" >= ").append(traduce(childs.get(1)));
                        case "LT" -> sb.append(traduce(childs.get(0))).append(" < ").append(traduce(childs.get(1)));
                        case "LE" -> sb.append(traduce(childs.get(0))).append(" <= ").append(traduce(childs.get(1)));
                        case "EQ" -> {
                            if (childs.get(1).getReturnType() == ReturnType.STRING) {
                                sb.append("confrontaString(").append(traduce(childs.get(0))).append(", ").append(traduce(childs.get(1))).append(")");
                            } else {
                                sb.append(traduce(childs.get(0))).append(" == ").append(traduce(childs.get(1)));
                            }
                        }
                        case "NE" -> {
                            if (childs.get(1).getReturnType() == ReturnType.STRING) {
                                sb.append("!confrontaString(").append(traduce(childs.get(0))).append(", ").append(traduce(childs.get(1))).append(")");
                            } else {
                                sb.append(traduce(childs.get(0))).append(" != ").append(traduce(childs.get(1)));
                            }
                        }
                    }
                } else {
                    switch (n.getValue()) {
                        case "PAR" -> sb.append("(").append(traduce(childs.get(0))).append(")");
                        case "MINUS" -> sb.append("-").append(traduce(childs.get(0)));
                        case "NOT" -> sb.append("!").append(traduce(childs.get(0)));
                    }
                }
//                sb.append(")");
            }
            default -> {
                for (SintaticNode s : childs)
                    sb.append(traduce(s));
            }
        }

        return sb.toString();
    }

    private void getAllreturnType(SintaticNode n, List<ReturnType> l) {
        if (n.getName() == SintaticEnum.ID)
            l.add(n.getCurrentSymbolTable().lookup(n.getValue()).getReturnType());
        else
            for (SintaticNode subChild : n.getChildCasted())
                getAllreturnType(subChild, l);
    }

    private void getAllIds(SintaticNode n, List<SintaticNode> l) {
        if (n.getName() == SintaticEnum.ID)
            l.add(n);
        else
            for (SintaticNode subChild : n.getChildCasted())
                getAllIds(subChild, l);
    }


    //Creazione File
    private void creaMakeFile() {
        try {
            String fileName = source.getName();
            String sourcePath = Util.getPath(source);

            PrintWriter fw = new PrintWriter(dirLibs.getCanonicalPath() + File.separator + "Makefile");
            fw.write(fileName + ".exe:\n\tgcc tgc.o String.o main.c -o \"" + fileName + ".exe\" -lm");
            fw.close();

            fw = new PrintWriter(dirLibs.getCanonicalPath() + File.separator + "make.bat");
            fw.write("wsl.exe make\nmove \"" + fileName + ".exe\" \"" + sourcePath + "\"\ncd \"" + sourcePath + "\"\nwsl.exe ./" + fileName + ".exe");
            fw.close();
        } catch (IOException e) {
            System.err.println("Impossibile scrivere il makefile controlla i permessi.");
            System.exit(500);
        }
    }

    private void creaMain(String code) {
        try {
            PrintWriter fw = new PrintWriter(dirLibs.getCanonicalPath() + File.separator + "main.c");
            fw.write(code);
            fw.close();
        } catch (IOException e) {
            System.err.println("Impossibile scrivere il main.c controlla i permessi.");
            System.exit(500);
        }
    }

    //Debug
    private void copyToClipboard(String code) {
        StringSelection stringSelection = new StringSelection(code);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
        System.out.println("Il codice tradotto lo trovi nella clipboard incollalo per utilizzarlo");
    }
}


