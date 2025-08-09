package com.w3canvas.javacanvas.js.impl.node;

import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

public enum NodeType {

	Canvas("canvas") {
		@Override
		public Node getNode() {
			return RhinoCanvasUtils.getScriptableInstance(
					HTMLCanvasElement.class, null);
		}
	},

	Image("img") {
		@Override
		public Node getNode() {
			return RhinoCanvasUtils.getScriptableInstance(Image.class,
					new Object[] { 1, 1 });
		}
	};

	private String nodeType;

	NodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getNodeType() {
		return nodeType;
	}

	public static NodeType getNodeTypeByName(String node) {
		for (NodeType nodeTypeItem : values()) {
			if (nodeTypeItem.getNodeType().equalsIgnoreCase(node)) {
				return nodeTypeItem;
			}
		}

		return null;
	}

	public Node getNode() {
		return null;
	}

}
