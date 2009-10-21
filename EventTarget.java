package com.w3canvas.javacanvas.js;

import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.events.EventException;
import com.w3canvas.javacanvas.js.impl.event.JSEvent;

// the JavaScript spec has functions that require EventTarget objects that implement these methods.
// At the same time, these objects are treated a lot like a Node.  And the spec specifically mentions
// an EventTarget interface - so really there is no need for an interface, there is a need for a Node that
// has these methods.  But an interface is used here to just help with keeping our implementation lined up
// with the spec.

public interface EventTarget extends Scriptable {

	public String ON_CLICK = "click";

	public String ON_MOUSEDOWN = "mousedown";

	public String ON_MOUSEUP = "mouseup";

	public String ON_DBLCLICK = "dblclick";

	void jsFunction_addEventListener(String type, Function listener, Boolean useCapture);

	void jsFunction_removeEventListener(String type, Function listener, Boolean useCapture);

	boolean jsFunction_dispatchEvent(JSEvent evt) throws EventException;

}
