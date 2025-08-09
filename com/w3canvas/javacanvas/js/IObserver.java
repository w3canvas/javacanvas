package com.w3canvas.javacanvas.js;

import com.w3canvas.javacanvas.backend.rhino.impl.node.CustomEvent;

public interface IObserver {

	public void notifyMe(CustomEvent event);

}
