package com.engelhardt.simon.utils;

public sealed interface Type permits Type.PrimitiveType, Type.ReferenceType {
    String string_type = "Zeichenkette";
    String long_type = "ganzzahl";
    String boolean_type = "wahrheitswert";
    String void_type = "nichts";


    Type STRING_TYPE = new ReferenceType(string_type, "char *");
    Type LONG_TYPE = new PrimitiveType(long_type, "long");
    Type BOOLEAN_TYPE = new PrimitiveType(boolean_type, "long");
    Type VOID_TYPE = new PrimitiveType(void_type, "void");


    static Type of(String name) {
        return switch (name) {
            case string_type -> STRING_TYPE;
            case long_type -> LONG_TYPE;
            case boolean_type -> BOOLEAN_TYPE;
            case void_type -> VOID_TYPE;
            default -> new ReferenceType(name, null);
        };
    }

    String name();

    String ctype();

    boolean isPrimitive();

    default boolean isReference() {
        return !isPrimitive();
    }

    static boolean isBuildIn(String name) {
        return switch (name) {
            case "zeichenkette", "ganzzahl", "wahrheitswert" -> true;
            default -> false;
        };
    }

    record PrimitiveType(String name, String ctype) implements Type {
        @Override
        public boolean isPrimitive() {
            return true;
        }
    }

    record ReferenceType(String name, String ctype) implements Type {
        @Override
        public boolean isPrimitive() {
            return false;
        }

    }
}
