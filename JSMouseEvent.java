package com.w3canvas.javacanvas.js.impl.event;

import java.awt.event.MouseEvent;

import com.w3canvas.javacanvas.js.AbstractView;
import com.w3canvas.javacanvas.js.EventTarget;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

public class JSMouseEvent extends JSUIEvent
{

//    private String typeArg;
//    private boolean canBubbleArg;
//    private boolean cancelableArg;
//    private IAbstractView viewArg;
//    private Integer detailArg;
    private Integer screenXArg;
    private Integer screenYArg;
    private Integer clientXArg;
    private Integer clientYArg;
    private boolean ctrlKeyArg;
    private boolean altKeyArg;
    private boolean shiftKeyArg;
    private boolean metaKeyArg;
    private Integer buttonArg;
    private EventTarget relatedTargetArg;

    public static JSMouseEvent convert(MouseEvent e)
    {
        JSMouseEvent mouseEvent = RhinoCanvasUtils.getScriptableInstance(JSMouseEvent.class, null);

        mouseEvent.jsFunction_initMouseEvent(e.paramString(), true, true, null, e.getClickCount(), e
            .getXOnScreen(), e.getYOnScreen(), e.getX(), e.getY(), e.isControlDown(), e.isAltDown(), e
            .isShiftDown(), e.isMetaDown(), e.getButton(), null);

        return mouseEvent;
    }

    public void jsFunction_initMouseEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg,
                                          AbstractView viewArg, Integer detailArg, Integer screenXArg, Integer screenYArg, Integer clientXArg,
                                          Integer clientYArg, boolean ctrlKeyArg, boolean altKeyArg, boolean shiftKeyArg, boolean metaKeyArg,
                                          Integer buttonArg, EventTarget relatedTargetArg)
    {

//        this.typeArg = typeArg;
//        this.canBubbleArg = canBubbleArg;
//        this.cancelableArg = cancelableArg;
//        this.viewArg = viewArg;
//        this.detailArg = detailArg;
        this.screenXArg = screenXArg;
        this.screenYArg = screenYArg;
        this.clientXArg = clientXArg;
        this.clientYArg = clientYArg;
        this.ctrlKeyArg = ctrlKeyArg;
        this.altKeyArg = altKeyArg;
        this.shiftKeyArg = shiftKeyArg;
        this.metaKeyArg = metaKeyArg;
        this.buttonArg = buttonArg;
        this.relatedTargetArg = relatedTargetArg;
    }

    public boolean jsGet_altKey()
    {
        return altKeyArg;
    }

    public Integer jsGet_button()
    {
        return buttonArg;
    }

    public Integer jsGet_clientX()
    {
        return clientXArg;
    }

    public Integer jsGet_clientY()
    {
        return clientYArg;
    }

    public boolean jsGet_ctrlKey()
    {
        return ctrlKeyArg;
    }

    public boolean jsGet_metaKey()
    {
        return metaKeyArg;
    }

    public EventTarget jsGet_relatedTarget()
    {
        return relatedTargetArg;
    }

    public Integer jsGet_screenX()
    {
        return screenXArg;
    }

    public Integer jsGet_screenY()
    {
        return screenYArg;
    }

    public boolean jsGet_shiftKey()
    {
        return shiftKeyArg;
    }

    // used by FF
    public Integer jsGet_pageX()
    {
        return jsGet_clientX();
    }

    // used by FF
    public Integer jsGet_pageY()
    {
        return jsGet_clientY();
    }

}
