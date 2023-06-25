#include "main.h"

#define MAX 1024

struct c_String {
    char val[MAX];
};

//=================================================//

static String newstruct() {
    String x = tgc_alloc(getGc(), sizeof(struct c_String));
    if (x == NULL) {
        printf("[ERROR] Spazio insufficiente per creare un String\n");
        exit(0);
    } else {
        return x;
    }
}

//=================================================//

String creaStringC(char *val) {
    String x = newstruct();
    strcpy(x->val, val);
    return x;
}
String creaStringS(String val) {
    String x = newstruct();
    strcpy(x->val, val->val);
    return x;
}

int confrontaSS(String x, String y) {
    return !strcmp(x->val, y->val);
}
int confrontaSC(String x, char *y) {
    return !strcmp(x->val, y);
}
int confrontaCS(char *x, String y) {
    return !strcmp(x, y->val);
}
int confrontaCC(char *x, char *y) {
    return !strcmp(x, y);
}

String concatSS(String s1, String s2) {
    String s = creaString(s1);
    sprintf(s->val, "%s%s", s1->val, s2->val);
    return s;
}
String concatSI(String s1, int i) {
    String s = creaString(s1);
    sprintf(s->val, "%s%d", s1->val, i);
    return s;
}
String concatSR(String s1, double i) {
    String s = creaString("");
    sprintf(s->val, "%s%.2f", s1->val, i);
    return s;
}
String concatSC(String s1, char *s2) {
    String s = creaString("");
    sprintf(s->val, "%s%s", s1->val, s2);
    return s;
}
String concatCS(char *s1, String s2) {
    String s = creaString("");
    sprintf(s->val, "%s%s", s1, s2->val);
    return s;
}
String concatCC(char *s1, char *s2) {
    String s = creaString("");
    sprintf(s->val, "%s%s", s1, s2);
    return s;
}
String concatCI(char *s1, int i) {
    String s = creaString("");
    sprintf(s->val, "%s%d", s1, i);
    return s;
}
String concatCD(char *s1, double i) {
    String s = creaString("");
    sprintf(s->val, "%s%.2f", s1, i);
    return s;
}

void scanS(String *x){
    if(*x == NULL)
        *x = creaString("");
    scanf("%s", (*x)->val);
}
void scanI(int *x){
    scanf("%d", x);
}
void scanC(char *x){
    scanf("%s", x);
}
void scanR(double *x){
    scanf("%lf", x);
}

void printI(int x, char *after){
    printf("%d%s", x, after);
}
void printS(String x, char *after){
    printf("%s%s", x->val, after);
}
void printC(char *x, char *after){
    printf("%s%s", x, after);
}
void printR(double x, char *after){
    printf("%.2f%s", x, after);
}
