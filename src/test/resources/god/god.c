#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <strings.h>
#include "god.h"

int i = 0;
void test1(long expected, long received) {
    printf("%d:\tExpected: %ld\tReceived: %ld\t%s\n", i++, expected, received, expected == received ? "✅" : "❌");
}

void test(long expected, long received) {
    printf("%ld\n", received);
}

int main() {
    test(42, f42());
    test(6, f6(1,2,3,4,5,6,7));
    test(1, f7(1,2,3,4,5,6,7));
    test(1, g1(1,2,3,4,5,6,7,8,9));
    test(9, g9(1,2,3,4,5,6,7,8,9));
    test(12, f1(3,3,3));
    test(212, f1(200,4,3));
    test(200, whileLoop(200));
    test(720, fakultaet(6));
    test(2, fakultaet(2));
    test(1, fakultaet(1));
    test(1, fakultaet(0));
    test(720, fakultaetRecursive(6));
    test(1, fakultaetRecursive(0));
    test(720, fakultaetIt(6));
    test(720, fakultaetAux(1, 6));
    test(5040, fakultaetAux(1, 7));
    test1(0, fib(0));
    test1(2, fib(3));
    test1(3, fib(4));
    test1(5, fib(5));
    test1(8, fib(6));
}

