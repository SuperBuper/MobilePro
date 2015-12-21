package com.processmap.mobilepro.util;

public class KeyValueObject
{
    private final String name;
    private final String value;

    public KeyValueObject(String aName, String aValue)
    {
        name = aName;
        value = aValue;
    }

    public String name()   { return name; }
    public String value() { return value; }

    public String toString() { return value.toString(); }
}
