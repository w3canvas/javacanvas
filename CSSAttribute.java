package com.w3canvas.javacanvas.js.impl.event;


public enum CSSAttribute {

    Z_ORDER ("z-index"),
    DISPLAY ("display"),
    VISIBILITY("visibility"),
    POSITION("position");

    private final String attrName;

    CSSAttribute(String attrName) {
    	this.attrName = attrName;
    }

    public String getItemValue() {
    	return attrName;
    }

    public static CSSAttribute getValue(String val)
    {
        for (CSSAttribute item : CSSAttribute.values())
        {
            if (item.getItemValue().equalsIgnoreCase(val))
            {
                return item;
            }
        }

        throw new IllegalArgumentException("No enum const class " + val);
    }

}
