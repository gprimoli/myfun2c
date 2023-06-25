package it.util.SintaticAnalysis;
import it.util.ReturnType;

public enum SintaticEnum {
    PROGRAM,
    MAIN,
    VARDECLLIST,
    FUNLIST,
    VARDECL,
    ASSIGNSTAT,
    IDLISTINIT,
    STATLIST,
    IDLISTINITOBBL,
    IDLIST,
    PARAMDECLLIST,
    PARDECL,
    IFSTAT,
    ELSESTAT,
    WHILESTAT,
    FUN,
    READSTAT,
    WRITESTAT,
    CALLFUN,
    EXPRLIST,
    EXPR,
    NULL,
    VAR,INTEGER,BOOL,REAL,STRING,ID,
    INTEGER_CONST,REAL_CONST,BOOL_CONST,STRING_CONST,
    OUTPAR, OUT, RETURN, COMMA;

    public static SintaticEnum evaluate(ReturnType type){
        return switch (type) {
            case INTEGER, BOOL -> SintaticEnum.INTEGER;
            case STRING -> SintaticEnum.STRING;
            case REAL -> SintaticEnum.REAL;
            default -> SintaticEnum.NULL;
        };
    }

    public static boolean isConst(SintaticNode n){
        return switch (n.getName()){
            case INTEGER_CONST,REAL_CONST,BOOL_CONST,STRING_CONST -> true;
            default -> false;
        };
    }

    public static boolean evaluate(SintaticEnum se){
        return switch (se) {
            case INTEGER, BOOL, STRING, REAL -> true;
            default -> false;
        };
    }
}
