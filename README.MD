# Alman Assembliere

## Notitzen

Auf Linux: gcc test1.s -o a.out
Auf MacOS:
arch -x86_64 gcc test1.s -o a.out
arch -x86_64 gcc -S -fno-asynchronous-unwind-tables C-testfiles/test.c -o C-testfiles/test.s

### Konventionen
Eine Expression wird immer so evaluiert, dass in %rax das Ergebnis steht.
Dabei steht 0 für false und alles andere für true.
