package com.engelhardt.simon.utils;

public class Parameter {
    public String name;
    public String type;
    public Type theType;

    public Parameter(String name, String type, Type theType) {
        this.name = name;
        this.type = type;
        this.theType = theType;
    }

    public Parameter(String name, String type) {
        this(name, type, null);
    }
}
