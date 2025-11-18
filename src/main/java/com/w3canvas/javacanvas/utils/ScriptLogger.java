package com.w3canvas.javacanvas.utils;


public class ScriptLogger {

	public void log(String msg) {
		System.out.println("LOG ->> " + msg);
	}

	public void log(Object... args) {
		StringBuilder sb = new StringBuilder("LOG ->> ");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(args[i]);
		}
		System.out.println(sb.toString());
	}

	public void point(Object obj) {
		System.out.println("LOG - point " + obj);
	}

}
