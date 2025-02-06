# Alman Assembliere

## Notitzen

Auf Linux: gcc test1.s -o a.out
Auf MacOS:
arch -x86_64 gcc test1.s -o a.out
arch -x86_64 gcc -S -fno-asynchronous-unwind-tables C-testfiles/test.c -o C-testfiles/test.s

### Konventionen

Eine Expression wird immer so evaluiert, dass in %rax das Ergebnis steht.
Dabei steht 0 für false und alles andere für true.

`movq    _x(%rip), %rax`
Nehme die Adresse am Label _x + das %rip register

`.p2align 3, 0x0`
Quad Align
Die 3 steht für 2^3 = 8, also ist immer eine 2er Potenz gemeint. For example ‘.p2align 3’ advances the location counter
until it is a multiple of 8. If the location counter is already a multiple of 8, no change is needed. If the expression
is omitted then a default value of 0 is used, effectively disabling alignment requirements.
https://sourceware.org/binutils/docs/as/P2align.html#P2align

### Einstiegspunkt?

- haupt
- haupteinstiegspunkt
- einstiegspunkt

## ACHTUNG WICHTIG

Beim kompilieren auf Linux gibt es folgendes Problem:

- in meinem Compiler werden alle Funktionen mit einem **_** vorangestellt geschrieben
- das liegt daran das C bei der Kompilierung von z.B. f1() nicht etwas f1 aufruft, sondern _f1
- das ist aber auf Linux nicht so
- auf Linux wird das ganze einfach direkt ohne **_** übersetzt
- und dann können die Funktionen natürlich nicht mehr gefunden werden
