package com.w3canvas.javacanvas.js.impl.node;

import com.w3canvas.javacanvas.js.impl.event.CSSAttribute;

public class CustomEvent
{

    private CSSAttribute eventType;
    private Object value;

    public CustomEvent(CSSAttribute event, Object value)
    {
        this.eventType = event;
        this.value = value;
    }

    public CSSAttribute getEventType()
    {
        return eventType;
    }

    public Object getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "t:" + eventType + " v:" + value;
    }
}
