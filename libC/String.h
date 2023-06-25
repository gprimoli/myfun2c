typedef struct c_String *String;

#define creaString(a) _Generic(a, String: creaStringS, char*: creaStringC)(a)
String creaStringC(char *val);
String creaStringS(String val);


#define confrontaString(a, ...) _Generic(a, String: confrontaStringWith(__VA_ARGS__), char*: confrontaCharWith(__VA_ARGS__))(a, __VA_ARGS__)
#define confrontaStringWith(b) _Generic(b, String: confrontaSS, char*: confrontaSC)
#define confrontaCharWith(b) _Generic(b, String: confrontaCS, char*: confrontaCC)
int confrontaSS(String x, String y);
int confrontaSC(String x, char *y);
int confrontaCS(char *x, String y);
int confrontaCC(char *x, char *y);


//Overload
#define concat(a, ...) _Generic(a, String: concatStringWith(__VA_ARGS__), char*: concatCharWith(__VA_ARGS__))(a, __VA_ARGS__)
#define concatStringWith(b) _Generic(b, int: concatSI, double: concatSR, String: concatSS, char*: concatSC)
#define concatCharWith(b) _Generic(b, int: concatCI, double: concatCD, String: concatCS, char*: concatCC)
String concatSS(String s1, String s2);
String concatSI(String s1, int i);
String concatSR(String s1, double i);
String concatSC(String s1, char *s2);
String concatCS(char *s1, String s2);
String concatCI(char *s1, int i);
String concatCD(char *s1, double i);
String concatCC(char *s1, char *s2);


#define scan(a) _Generic(a, String*: scanS, char*: scanC, int*: scanI, double*: scanR)(a)
void scanI(int *x);
void scanS(String *x);
void scanC(char *x);
void scanR(double *x);


#define print(a, b) _Generic(a, int: printI, String: printS, char*: printC, double: printR)(a, b)
void printI(int x, char *after);
void printS(String x, char *after);
void printC(char *x, char *after);
void printR(double x, char *after);
