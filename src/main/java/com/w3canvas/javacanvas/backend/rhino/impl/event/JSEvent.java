package com.w3canvas.javacanvas.backend.rhino.impl.event;

import java.util.Calendar;
import java.util.Date;

import com.w3canvas.javacanvas.js.EventTarget;
import com.w3canvas.javacanvas.backend.rhino.impl.node.ProjectScriptableObject;

public class JSEvent extends ProjectScriptableObject
{
    // Event phase constants as per DOM Events spec
    public static final int NONE = 0;
    public static final int CAPTURING_PHASE = 1;
    public static final int AT_TARGET = 2;
    public static final int BUBBLING_PHASE = 3;

    private String eventTypeArg;
    private boolean canBubbleArg;
    private boolean cancelableArg;
    private boolean defaultPrevented = false;
    private boolean propagationStopped = false;
    private EventTarget target;
    private EventTarget currentTarget;
    private int eventPhase = NONE;

    public void jsFunction_initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg)
    {
        this.eventTypeArg = eventTypeArg;
        this.canBubbleArg = canBubbleArg;
        this.cancelableArg = cancelableArg;
    }

    public void jsFunction_preventDefault()
    {
        if (cancelableArg) {
            defaultPrevented = true;
        }
    }

    public void jsFunction_stopPropagation()
    {
        propagationStopped = true;
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
        return currentTarget;
    }

    public int jsGet_eventPhase()
    {
        return eventPhase;
    }

    public EventTarget jsGet_target()
    {
        return target;
    }

    public boolean jsGet_defaultPrevented()
    {
        return defaultPrevented;
    }

    // Internal methods for event dispatch
    public void setTarget(EventTarget target)
    {
        this.target = target;
    }

    public void setCurrentTarget(EventTarget currentTarget)
    {
        this.currentTarget = currentTarget;
    }

    public void setEventPhase(int eventPhase)
    {
        this.eventPhase = eventPhase;
    }

    public boolean isPropagationStopped()
    {
        return propagationStopped;
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
