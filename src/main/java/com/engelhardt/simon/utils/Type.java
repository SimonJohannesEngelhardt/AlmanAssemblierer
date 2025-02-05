package com.engelhardt.simon.utils;

public sealed interface Type permits Type.PrimitiveType, Type.ReferenceType {
    Type STRING_TYPE = new ReferenceType("String", "java.lang");
    Type LONG_TYPE = new PrimitiveType("J", "long");
    Type BOOLEAN_TYPE = new PrimitiveType("Z", "boolean");
    Type DOUBLE_TYPE = new PrimitiveType("D", "double");
    Type VOID_TYPE = new PrimitiveType("V", "void");

    static Type of(String name, String packageN) {
        return switch (name) {
            case "String" -> STRING_TYPE;
            case "long" -> LONG_TYPE;
            case "boolean" -> BOOLEAN_TYPE;
            case "double" -> DOUBLE_TYPE;
            case "void" -> VOID_TYPE;
            default -> new ReferenceType(name, packageN);
        };
    }

    String jvmName();

    String name();

    boolean isPrimitive();

    default boolean isReference() {
        return !isPrimitive();
    }

    static boolean isBuildIn(String name) {
        return switch (name) {
            case "gleitkommazahl", "zeichenkette", "ganzzahl", "wahrheitswert" -> true;
            default -> false;
        };
    }

    record PrimitiveType(String jvmName, String name) implements Type {
        @Override
        public boolean isPrimitive() {
            return true;
        }
    }

    record ReferenceType(String className, String packageName) implements Type {
        @Override
        public String jvmName() {
            return STR."L\{packageName().replace('.', '/')}/\{className};";
        }

        @Override
        public String name() {
            return STR."\{packageName()}.\{className()}";
        }

        @Override
        public boolean isPrimitive() {
            return false;
        }

    }
}
