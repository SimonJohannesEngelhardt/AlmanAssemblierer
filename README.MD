# Alman Assembliere

## Voraussetzungen

Java muss installiert sein. Am besten eine neuere Version wie Java 23. Es wird Linux und MacOS unterstützt, auf Windows
könnte es eventuell mit einer WSL funktionieren.

## Installation

`./gradlew` - falls Gradle noch nicht installiert wird, so wird es installiert. Außerdem werden Abhängigkeiten
heruntergeladen.

## Kompilieren

Um ein Programm in der "Almn"-Sprache zu kompilieren muss zuerst eine Datei mit der Endung .almn erstellt werden. In
diese kann nun der Code geschrieben werden. Anschließend kann das Programm mit folgendem Befehl kompiliert werden:
`./gradlew run --args="path/to/file.almn"`
Oder falls die jar direkt benutzt wird:
`java -jar Alman-Assembliere-v-1.0-.jar path/to/file.almn`

Außerdem können noch folgende weiter Parameter mit übergeben werden:

- `--tree-view` - zeigt den AST in einem Baum an
- `--pretty-printer` - Gibt den Code in einer schönen Form auf der Konsole aus
- `--linux` - Kompiliert den Code für Linux (Standard ist MacOS)

Im selben Ordner wie die `.almn` Datei wird eine `.s` Datei, sowie ein `.c` und eine `.h` Datei erstellt. Die `.s` Datei
ist die
Assemblerdatei, die `.c` Datei ist die C Datei für direkten Zugriff auf die Methoden und die `.h` Datei ist die Header
Datei
die die Schnittstelle für C zur Verfügung stellt.

Die `.s` Datei kann nun mit einem Assembler kompiliert werden. Dafür kann ein Befehl wie `gcc test1.s -o test1`
verwendet werden.
Achtung: Auf M* Chips(MacOS) muss der Befehl `arch -x86_64 gcc test1.s -o test1` verwendet werden.

## Ausführen

`./test1` - führt das Programm aus

# Ressourcen

[Detailiertes Wiki](https://github.com/SimonJohannesEngelhardt/AlmanAssemblierer/wiki/)

## Beispieldateien

Um sich einen ersten Überblick zu verschaffen, empfehlt sich der Ordner `src/test/resources/`. Hier sind mehrere Ordner
die jeweils ein Beispielprogramm beinhalten, zusammen mit bereits kompilierten Dateien.
