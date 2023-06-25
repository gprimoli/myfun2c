#include "main.h"

static tgc_t gc;
tgc_t *getGc(){ return &gc; }

int c = 1;


double sommac(int a, double b, String  *size){
double result;
result = a + b + c;
if(result > 100){
String valore = creaString("grande");
*size = valore;
} else {
String valore = creaString("piccola");
*size = valore;
}
return result;
}


void stampa(String messaggio){
int i = 1;
while(i <= 4){
int incremento = 1;
print("", "\n");
i = i + incremento;
}
print(messaggio, "\n");
}

int main(int argc, char *argv[]){
tgc_start(&gc, &argc);

int a = 1; double b = 2.2;
String taglia;
String ans = creaString("no");
double risultato = sommac(a, b,  &taglia);
stampa(concat(concat(concat(concat(concat(concat(concat("la somma di ", a), " e "), b), " incrementata di "), c), " è "), taglia));
stampa(concat("ed è pari a ", risultato));
print("vuoi continuare? (si/no)", " ");
scan(&ans);
while(confrontaString(ans, "si")){
print("inserisci un intero:", "\n");
scan(&a);
print("inserisci un reale:", "\n");
scan(&b);
risultato = sommac(a, b,  &taglia);
stampa(concat(concat(concat(concat(concat(concat(concat("la somma di ", a), " e "), b), " incrementata di "), c), " è "), taglia));
stampa(concat(" ed è pari a ", risultato));
print("vuoi continuare? (si/no):	", "\n");
scan(&ans);
}
print("", "\n");
print("ciao", "");

tgc_stop(&gc);
return 0;
}
