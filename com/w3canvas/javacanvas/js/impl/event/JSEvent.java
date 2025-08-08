package com.w3canvas.javacanvas.js.impl.event;

import java.util.Calendar;
import java.util.Date;

import com.w3canvas.javacanvas.js.EventTarget;
import com.w3canvas.javacanvas.js.impl.node.ProjectScriptableObject;

public class JSEvent extends ProjectScriptableObject
{


    private String eventTypeArg;
    private boolean canBubbleArg;
    private boolean cancelableArg;

    public void jsFunction_initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg)
    {
        this.eventTypeArg = eventTypeArg;
        this.canBubbleArg = canBubbleArg;
        this.cancelableArg = cancelableArg;
    }

    public void jsFunction_preventDefault()
    {
        // TODO Auto-generated method stub
    }

    public void jsFunction_stopPropagation()
    {
        // TODO Auto-generated method stub
    }

    public boolean jsGet_bubbles()
    {
        return canBubbleArg;
    }

    public boolean jsGet_cancelable()
    {
        return cancelableArg;
    }

    public EventTarget jsGet_currentTarget()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public int jsGet_eventPhase()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public EventTarget jsGet_target()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Date jsGet_timeStamp()
    {
        return Calendar.getInstance().getTime();
    }

    public String jsGet_type()
    {
        return eventTypeArg;
    }

}
