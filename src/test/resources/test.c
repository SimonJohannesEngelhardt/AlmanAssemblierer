#include <stdio.h>
char * s = "Hallo";
void f(long i) {
    printf("%ld\n", i);
    printf("%ld\n", i);
}
int main() {
    int y = 1;
    if(1==y && 2==y) {
        f(20);
    }
}
