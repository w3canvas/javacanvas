package com.w3canvas.javacanvas.backend.rhino.impl.node;

import java.awt.Component;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JRootPane;

import com.w3canvas.javacanvas.js.EventTarget;
import com.w3canvas.javacanvas.js.INodeUtils;
import com.w3canvas.javacanvas.js.NodeList;
import com.w3canvas.javacanvas.backend.rhino.impl.event.CSSAttribute;
import com.w3canvas.javacanvas.backend.rhino.impl.event.JSEvent;
import com.w3canvas.javacanvas.backend.rhino.impl.event.JSMouseEvent;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.w3c.dom.events.EventException;

@SuppressWarnings("serial")
public class Node extends ProjectScriptableObject implements EventTarget, INodeUtils {
	public static final String STYLE_ATTRIBUTE = "style";

	private String id;
	private List<Node> childNodes = new ArrayList<Node>();
	private StyleHolder styles = new StyleHolder();
	private AttributeHolder attributes = new AttributeHolder();
	private Node parentNode = null;
	protected Document document;
	private static final TreeMap<String, Node> zOrderNodes = new TreeMap<String, Node>();
	private String lastZOrder = null;

	private Function fnOnClick, fnOnDblClick, fnOnMouseUp, fnOnMouseDown;

	// private Function mousemoveFunction = null;

	private Map<String, List<EventActionWrapper>> listeners = new HashMap<String, List<EventActionWrapper>>();

	/*
	 *
	 * Architectural design choice ....
	 *
	 * I was going to make everything super turbo and have a Function declared
	 * for each type of callback. But the code would then be a little too big
	 * just to save one hashtable lookup for each callback. So instead I decided
	 * to simplify the code for all events except for "mousemove" which happens
	 * a lot. So "mousemove" has no hashtable lookup, but the rest of the events
	 * do.
	 *
	 * -- paul wheaton
	 */

	/*
	 * onLoad("load"), onError("error"), onClose("close"),
	 * onBeforeUnload("beforeunload"), onResize("resize"), onContextMenu(
	 * "contextmenu"), onMousewheel("mousewheel"), onMousedown("mousedown"),
	 * onMouseup("mouseup"), onMousemove( "mousemove"), onDblclick("dblclick"),
	 * onKeydown("keydown"), onKeyup("keyup"), onKeypress("keypress"), onBlur(
	 * "blur");
	 */

	protected void init() {
		jsFunction_setAttribute(STYLE_ATTRIBUTE, "z-index: 0");
		jsFunction_setAttribute(STYLE_ATTRIBUTE, "position: static");
	}

    public void setDocument(Document document) {
        this.document = document;
    }

    public Document getDocument() {
        return this.document;
    }

	private static class EventActionWrapper {

		private Function action;

		private Boolean phase;

		public EventActionWrapper(Function action, Boolean phase) {
			this.action = action;
			this.phase = phase;
		}

		public Function getAction() {
			return action;
		}

		@SuppressWarnings("unused")
		public void setAction(Function action) {
			this.action = action;
		}

		public Boolean getPhase() {
			return phase;
		}

		@SuppressWarnings("unused")
		public void setPhase(Boolean phase) {
			this.phase = phase;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((action == null) ? 0 : action.hashCode());
			result = prime * result + ((phase == null) ? 0 : phase.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EventActionWrapper other = (EventActionWrapper) obj;
			if (action == null) {
				if (other.action != null)
					return false;
			} else if (!action.equals(other.action))
				return false;
			if (phase == null) {
				if (other.phase != null)
					return false;
			} else if (!phase.equals(other.phase))
				return false;
			return true;
		}

		private EventActionWrapper getOuterType() {
			return EventActionWrapper.this;
		}

	}

	public void jsSet_onclick(final Function function) {
		fnOnClick = function;

		List<EventActionWrapper> functions = listeners.get(EventTarget.ON_CLICK);

		if (functions != null) {
			functions.clear();
		}

		jsFunction_addEventListener(EventTarget.ON_CLICK, function, false);
	}

	public Function jsGet_onclick() {
		return fnOnClick;
	}

	public void jsSet_ondblclick(final Function function) {
		fnOnDblClick = function;

		List<EventActionWrapper> functions = listeners.get(EventTarget.ON_DBLCLICK);

		if (functions != null) {
			functions.clear();
		}

		jsFunction_addEventListener(EventTarget.ON_DBLCLICK, function, false);
	}

	public Function jsGet_ondblclick() {
		return fnOnDblClick;
	}

	public void jsSet_onmouseup(final Function function) {
		fnOnMouseUp = function;

		List<EventActionWrapper> functions = listeners.get(EventTarget.ON_MOUSEUP);

		if (functions != null) {
			functions.clear();
		}

		jsFunction_addEventListener(EventTarget.ON_MOUSEUP, function, false);
	}

	public Function jsGet_onmouseup() {
		return fnOnDblClick;
	}

	public void jsSet_onmousedown(final Function function) {
		fnOnMouseDown = function;

		List<EventActionWrapper> functions = listeners.get(EventTarget.ON_MOUSEDOWN);

		if (functions != null) {
			functions.clear();
		}

		jsFunction_addEventListener(EventTarget.ON_MOUSEDOWN, function, false);
	}

	public Function jsGet_onmousedown() {
		return fnOnDblClick;
	}

	@Override
	public void jsFunction_addEventListener(String eventName, final Function action, Boolean phase) {
		// todo put mousemove shortcut in
		List<EventActionWrapper> functions = listeners.get(eventName);
		if (functions == null) {
			functions = new ArrayList<EventActionWrapper>();
		}
		functions.add(new EventActionWrapper(action, phase));
		listeners.put(eventName, functions);
	}

	@Override
	public void jsFunction_removeEventListener(String eventName, final Function action, Boolean phase) {
		List<EventActionWrapper> functions = listeners.get(eventName);
		if (functions != null) {
			functions.remove(new EventActionWrapper(action, phase));
		}
	}

	@Override
	public boolean jsFunction_dispatchEvent(JSEvent evt) throws EventException {
		throw new EventException(EventException.UNSPECIFIED_EVENT_TYPE_ERR, "unimplemented method");
	}

	public void jsFunction_setAttribute(String key, String value) {
		if (STYLE_ATTRIBUTE.equalsIgnoreCase(key)) {
			StyleHolder.applyStyles(value, styles);
		} else {
			attributes.put(key, value);
		}
	}

	public boolean jsFunction_hasAttribute(String key) {
		return (attributes.get(key) != null);
	}

	public String jsFunction_getAttribute(String key) {
		return (String) attributes.get(key, attributes);
	}

	public void jsFunction_removeAttribute(String key) {
		attributes.delete(key);
	}

	public void jsFunction_appendChild(Node child) {
		childNodes.add(child);
		child.appendToParent(this);
	}

	public void jsFunction_removeChild(Node child) {
		this.document.removeElementById(child.jsGet_id());
		childNodes.remove(child);
	}

	/**
	 * separate method - each node can have specific issues on append.
	 * recalculation left & top - depends on: - parent node width - already
	 * available nodes at the same node level
	 *
	 * @param parent
	 *            - parent node
	 */
	protected void appendToParent(Node parent) {
		setParentNode(parent);

		String nodePosition = (String) styles.get(CSSAttribute.POSITION.getItemValue());

		if (!"absolute".equalsIgnoreCase(nodePosition) && parent.jsGet_childNodes().jsGet_length() > 1) {
			Integer parentWidth = parent.getWidth();
			Integer itemNodeWidth = this.getWidth();

			Node prevChildNode = parent.jsGet_childNodes()
					.jsFunction_item(parent.jsGet_childNodes().jsGet_length() - 2);

			// TODO - should be recalculated by spec. rules
			// calculated like "float : left"
			if (parentWidth < itemNodeWidth || prevChildNode.getWidth() + itemNodeWidth > parentWidth) {
				setLeft(prevChildNode.getLeft());
				setTop(prevChildNode.getTop() + prevChildNode.getHeight());
			} else {
				setLeft(prevChildNode.getLeft() + prevChildNode.getWidth());
				setTop(prevChildNode.getTop());
			}
		}
	}

	public Node jsGet_parentNode() {
		return parentNode;
	}

	protected void setParentNode(Node parent) {
		parentNode = parent;
	}

	public String jsGet_id() {
		return id;
	}

	public void jsSet_id(String id) {
		this.id = id;

		this.document.addElement(id, this);
	}

	public void jsSet_style(StyleHolder style) {
		this.styles = style;
	}

	public StyleHolder jsGet_style() {
		return styles;
	}

	public NodeList jsGet_childNodes() {
		return new NodeList() {

			@Override
			public Node jsFunction_item(Integer index) {
				return childNodes.isEmpty() ? null : childNodes.get(index);
			}

			@Override
			public Integer jsGet_length() {
				return childNodes.size();
			}
		};
	}

	public Node jsGet_firstChild() {
		return childNodes.isEmpty() ? null : childNodes.get(0);
	}

	public Node jsGet_lastChild() {
		return childNodes.isEmpty() ? null : childNodes.get(childNodes.size() - 1);
	}

	public String jsGet_nodeName() {
		return getNodeName();
	}

	public String getNodeName() {
		// Default implementation for base Node class
		// Subclasses should override this to return their specific node name
		return "#node";
	}

	public Integer jsGet_nodeType() {
		// Default to ELEMENT_NODE (1) as most nodes are elements
		// Subclasses can override for specific types:
		// ELEMENT_NODE = 1, TEXT_NODE = 3, DOCUMENT_NODE = 9
		return 1;
	}

	private String getStyleValue(String attr, Integer defaultVal) {
		Object result = jsGet_style().get(attr);
		if (result == null) {
			result = defaultVal;
			jsGet_style().put(attr, result);
		}

		return result.toString();
	}

	protected void onResize() {
	}

	public HTMLCanvasElement getOwner() {
		return null;
	}

	public String jsGet_left() {
		return getStyleValue("left", 0);
	}

	public void jsSet_left(Object left) {
		jsGet_style().put("left", left);
		onResize();
	}

	@Override
	public void setLeft(Integer left) {
		jsSet_left(left);
	}

	@Override
	public Integer getLeft() {
		Integer left = 0;
		Object result = jsGet_left();

		if (result instanceof String) {
			left = Float.valueOf((String) result).intValue();
		} else if (result instanceof Integer) {
			left = (Integer) result;
		}

		return left;
	}

	@Override
	public Integer getRealLeft() {
		Integer realLeft = getLeft();
		String nodePosition = (String) styles.get(CSSAttribute.POSITION.getItemValue());

		if (("static".equals(nodePosition) || "relative".equals(nodePosition)) && (jsGet_parentNode() != null)) {
			realLeft += jsGet_parentNode().getRealLeft();
		}

		return realLeft;
	}

	public Object jsGet_top() {
		return getStyleValue("top", 0);
	}

	public void jsSet_top(Object top) {
		jsGet_style().put("top", top);
		onResize();
	}

	@Override
	public void setTop(Integer top) {
		jsSet_top(top);
	}

	@Override
	public Integer getTop() {
		Integer top = 0;
		Object result = jsGet_top();

		if (result instanceof String) {
			top = Float.valueOf((String) result).intValue();
		} else if (result instanceof Integer) {
			top = (Integer) result;
		}

		return top;
	}

	@Override
	public Integer getRealTop() {
		Integer realTop = getTop();
		String nodePosition = (String) styles.get(CSSAttribute.POSITION.getItemValue());

		if (("static".equals(nodePosition) || "relative".equals(nodePosition)) && (jsGet_parentNode() != null)) {
			realTop += jsGet_parentNode().getRealTop();
		}

		return realTop;
	}

	public Object jsGet_width() {
		return getStyleValue("width", 0);
	}

	public void jsSet_width(Object width) {
		jsGet_style().put("width", width);
		if ((getOwner() != null) && (getOwner().getCanvas() != null)) {
			getOwner().getCanvas().reset();
		}
		onResize();
	}

	@Override
	public void setWidth(Integer width) {
		jsSet_width(width);
	}

	@Override
	public Integer getWidth() {
		Integer width = 0;
		Object result = jsGet_width();

		if (result instanceof String) {
			width = Float.valueOf((String) result).intValue();
		} else if (result instanceof Integer) {
			width = (Integer) result;
		}

		return width;
	}

	public Object jsGet_height() {
		return getStyleValue("height", 0);
	}

	public void jsSet_height(Object height) {
		jsGet_style().put("height", height);
		if ((getOwner() != null) && (getOwner().getCanvas() != null)) {
			getOwner().getCanvas().reset();
		}
		onResize();
	}

	public void setHeight(Integer height) {
		jsSet_height(height);
	}

	public Integer getHeight() {
		Integer height = 0;
		Object result = jsGet_height();

		if (result instanceof String) {
			height = Float.valueOf((String) result).intValue();
		} else if (result instanceof Integer) {
			height = (Integer) result;
		}

		return height;
	}

	private void callFunction(String functionName, Object[] objects) {
		LinkedList<Node> nodesChain = new LinkedList<Node>();
		fillNodeChain(nodesChain, this);
		captureChainEvents(nodesChain, functionName, objects);
		bubbleChainEvents(nodesChain, functionName, objects);
	}

	private void captureChainEvents(LinkedList<Node> nodesChain, String functionName, Object[] objects) {

		ListIterator<Node> iterator = nodesChain.listIterator(nodesChain.size());

		while (iterator.hasPrevious()) {
			Node itemNode = iterator.previous();
			itemNode.callFunction(functionName, objects, true);
		}
	}

	private void bubbleChainEvents(LinkedList<Node> nodesChain, String functionName, Object[] objects) {

		for (Node itemNode : nodesChain) {
			itemNode.callFunction(functionName, objects, false);
		}
	}

	private void callFunction(String functionName, Object[] objects, Boolean phase) {
		List<EventActionWrapper> functions = listeners.get(functionName);

		if (functions != null) {
			for (EventActionWrapper functionWrapper : functions) {
				if (phase == functionWrapper.getPhase()) {
					Function function = functionWrapper.getAction();
					function.call(Context.enter(), function, function, objects);
				}
			}
		}
	}

	protected void fillNodeChain(LinkedList<Node> nodeChain, Node sourceNode) {
		nodeChain.add(sourceNode);
		Node parentNode = sourceNode.jsGet_parentNode();

		while (parentNode != null) {
			nodeChain.add(parentNode);
			parentNode = parentNode.jsGet_parentNode();
		}
	}

	public void callErrorFunction() {

	}

	public void callCloseFunction() {
		callFunction("close", new Object[] {});
	}

	public void callBeforeunloadFunction() {

	}

	public void callLoadFunction() {
		callFunction("load", new Object[] {});
	}

	public void callResizeFunction() {
		callFunction("resize", new Object[] {});
	}

	public void callContextmenuFunction() {

	}

	public void callMousewheelFunction() {

	}

	public void callMousedownFunction(JSMouseEvent event) {
		callFunction(EventTarget.ON_MOUSEDOWN, new Object[] { event });
	}

	public void callMouseupFunction(JSMouseEvent event) {
		callFunction(EventTarget.ON_MOUSEUP, new Object[] { event });
	}

	public void callMousemoveFunction(JSMouseEvent event) {
		callFunction("mousemove", new Object[] { event });
	}

	public void callDoubleclickFunction(JSMouseEvent event) {
		callFunction(EventTarget.ON_DBLCLICK, new Object[] { event });
	}

	public void callClickFunction(JSMouseEvent event) {
		callFunction(EventTarget.ON_CLICK, new Object[] { event });
	}

	public void callKeydownFunction() {

	}

	public void callKeyupFunction() {

	}

	public void callKeypressFunction() {

	}

	public void callBlurFunction() {

	}

	public Point getNodePosition() {
		return new Point(getRealLeft(), getRealTop());
	}

	@Override
	public boolean isMineXYArea(Point xy) {
		int width = getWidth();
		int height = getHeight();

		Point nodePosition = getNodePosition();

		return (xy.x >= nodePosition.x && xy.x <= width + nodePosition.x)
				&& (xy.y >= nodePosition.y && xy.y <= nodePosition.y + height);
	}

	@Override
	public boolean isDisplayed() {
		boolean isDisplayed = true;

		if (styles.get(CSSAttribute.DISPLAY.getItemValue()) != null
				&& "none".equalsIgnoreCase((String) styles.get(CSSAttribute.DISPLAY.getItemValue()))) {
			isDisplayed = false;
		} else if (styles.get(CSSAttribute.DISPLAY.getItemValue()) == null
				|| "inherit".equalsIgnoreCase((String) styles.get(CSSAttribute.DISPLAY.getItemValue()))) {
			// get value from parent node
			if (jsGet_parentNode() != null) {
				isDisplayed = jsGet_parentNode().isVisible();
			}
		}

		return isDisplayed;
	}

	@Override
	public boolean isVisible() {
		boolean isVisible = true;

		if (styles.get(CSSAttribute.VISIBILITY.getItemValue()) != null
				&& "hidden".equalsIgnoreCase((String) styles.get(CSSAttribute.VISIBILITY.getItemValue()))) {
			isVisible &= false;
		} else if (styles.get(CSSAttribute.VISIBILITY.getItemValue()) == null
				|| "inherit".equalsIgnoreCase((String) styles.get(CSSAttribute.VISIBILITY.getItemValue()))) {
			// get value from parent node
			if (jsGet_parentNode() != null) {
				isVisible = jsGet_parentNode().isVisible();
			}
		}

		return isVisible;
	}

	@Override
	public boolean isNodeVisible() {
		return isDisplayed() & isVisible();
	}

	@Override
	public boolean isMineArea(Point xy) {
		return isMineXYArea(xy) && isNodeVisible();
	}

	/**
	 * The javascript Z-Order stuff is the reverse from Java. Further, java
	 * requires the first item to start with a 1.
	 */
	protected void setZOrderingPref(Integer zOrder) {
		// stored by the requested zOrder as a string. If "1" is passed in, the
		// key is "00001".
		// if "1" is passed in again, the object with the "00001" key is changed
		// to "00001_999" and the new object key is saved under "00001"
		// if "1" is passed in yet again, the object with the "00001" key is
		// changed to "00001_998" and the new object key is saved under "00001"

		if (lastZOrder != null) {
			zOrderNodes.remove(lastZOrder);
		}

		String s = Integer.toString(zOrder);
		s = "00000".substring(5 - s.length()) + s;
		Node c = zOrderNodes.get(s);

		if (c != null) {
			int suborder = 0;
			while (zOrderNodes.get(s + "_" + suborder) != null) {
				suborder++;
			}
			s += ("_" + suborder);
		}
		zOrderNodes.put(s, this);
		lastZOrder = s;

		redrawNodes();
	}

	protected void redrawNodes() {
		Collection<Node> nodes = zOrderNodes.values();

		JRootPane root = null;
		// Get root pane if using Swing backend
		if (this.document.getWindowHost() instanceof com.w3canvas.javacanvas.backend.awt.SwingWindowHost) {
			root = ((com.w3canvas.javacanvas.backend.awt.SwingWindowHost) this.document.getWindowHost()).getContainer().getRootPane();
		}
		int i = nodes.size();

		if (root != null) {
			for (Node node : nodes) {
				Object panel = node.getNodePanel();
				if (panel instanceof java.awt.Component) {
					root.setComponentZOrder((java.awt.Component) panel, i);
				}
				i--;
			}

			root.validate();
			root.repaint();
		}
	}

	public Object getNodePanel() {
		return null;
	}

	protected void setLastZOrder(String lastZOrder) {
		this.lastZOrder = lastZOrder;
	}

	protected static TreeMap<String, Node> getZNodes() {
		return zOrderNodes;
	}

}
