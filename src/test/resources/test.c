#include <stdio.h>
long y = -20;
void f(long i) {
    printf("%ld\n", i);
    printf("%ld\n", i);
}
int main() {
   f(-20);
}
