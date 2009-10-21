package com.w3canvas.javacanvas.js.impl.node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.w3canvas.javacanvas.js.IObserver;
import com.w3canvas.javacanvas.js.impl.event.CSSAttribute;

import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

@SuppressWarnings("serial")
public class StyleHolder extends ScriptableObject {

	private Map<Object, Object> params;
	private Map<CSSAttribute, List<IObserver>> observers;

	public StyleHolder() {
		params = new HashMap<Object, Object>();
		observers = new HashMap<CSSAttribute, List<IObserver>>();
	}

	public void put(String name, Scriptable start, Object value) {
		put(name, value);
	}

	public Object put(Object key, Object value) {
		if (key instanceof String) {
			params.put((String) key, value);
			checkObservers((String) key, value);
		}

		return value;
	}

	public Object get(Object key) {
		return params.get(((String) key).toLowerCase());
	}

	public String getClassName() {
		return toString();
	}

	public String toString() {
		return this.getClass().getSimpleName();
	}

	public void registerObserver(IObserver observer, CSSAttribute eventType) {
		List<IObserver> observersList = observers.get(eventType);

		if (observersList == null) {
			observersList = new ArrayList<IObserver>();
			observers.put(eventType, observersList);
		}

		observersList.add(observer);
	}

	private void checkObservers(String key, Object value) {
		CSSAttribute eventType = null;

		try {
			eventType = CSSAttribute.getValue(key);
		} catch (IllegalArgumentException e) {
		}

		if (eventType != null && observers.get(eventType) != null) {
			for (IObserver observer : observers.get(eventType)) {
				observer.notifyMe(new CustomEvent(eventType, value));
			}
		}
	}

	public static void applyStyles(String styles, StyleHolder style) {
		String[] stylesArr = styles.split(";");
		for (String styleItem : stylesArr) {
			String[] styleItemArr = styleItem.split(":");

			if (styleItemArr.length == 2) {
				style.put(styleItemArr[0].trim().toLowerCase(), styleItemArr[1].trim());
			}
		}
	}
}
