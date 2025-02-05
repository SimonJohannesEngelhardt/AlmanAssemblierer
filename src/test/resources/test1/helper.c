#include <stdio.h>

int f1(long x1, long x2, long x3, long x4, long x5, long x6, long x7, long x8, long x9) {
    int a = 200;
    return x1+x2+x3+x4+x5+x6+x7+x8+x9;
}
int main() {
    printf("%d\n", f1(1,2,3,4,5,6,7,8,9));
}

