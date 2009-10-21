package com.w3canvas.javacanvas.js.impl.node;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.util.Stack;

import net.sf.css4j.Value;
import com.w3canvas.javacanvas.js.CanvasText;
import com.w3canvas.javacanvas.js.impl.gradient.CanvasGradient;
import com.w3canvas.javacanvas.js.impl.gradient.LinearCanvasGradient;
import com.w3canvas.javacanvas.js.impl.gradient.RadialCanvasGradient;
import com.w3canvas.javacanvas.utils.RhinoCanvasUtils;

@SuppressWarnings("serial")
public class CanvasRenderingContext2D extends ProjectScriptableObject implements CanvasText {

	private Graphics2D graphics;
	private GeneralPath path;
	private Stack<ContextState> stack;
	private HTMLCanvasElement canvas;
	private Paint fillPaint;
	private Paint strokePaint;
	private Object fillStyle;
	private Object strokeStyle;
	private Float globalAlpha;
	private String globalCompositeOperation;
	private Float lineWidth;
	private String lineJoin;
	private String lineCap;
	private Float miterLimit; // convert to rad?
	private CanvasTextStyle textStyle;

	public CanvasRenderingContext2D() {
		resetLevel1();
	}

	private void resetLevel1() {
		path = new GeneralPath();
		stack = new Stack<ContextState>();
		fillPaint = Color.BLACK;
		strokePaint = Color.BLACK;
		fillStyle = "#000";
		strokeStyle = "#000";
		globalAlpha = 1.0f;
		globalCompositeOperation = "source-over";
		lineWidth = 1.0f;
		lineJoin = "miter";
		lineCap = "butt";
		miterLimit = 10.0f; // convert to rad?
	}

	private void resetLevel2() {
		graphics = (Graphics2D) canvas.getImage().getGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setPaint(Color.BLACK);
		textStyle = new CanvasTextStyle(this);
	}

	public void initCanvas(HTMLCanvasElement canvas) {
		this.canvas = canvas;
		resetLevel2();
	}

	void reset() {
		resetLevel1();
		resetLevel2();
	}

	public void jsFunction_save() {
		stack.push(new ContextState(this));
	}

	public void jsFunction_restore() {
		ContextState st = stack.pop();
		st.apply(this);
	}

	public void jsFunction_scale(Double x, Double y) {
		graphics.scale(x, y);
	}

	public void jsFunction_rotate(Double angle) {
		graphics.rotate(angle);
	}

	public void jsFunction_translate(Double x, Double y) {
		graphics.translate(x, y);
	}

	public void jsFunction_transform(Double m11, Double m12, Double m21, Double m22, Double dx, Double dy) {
		graphics.transform(new AffineTransform(m11, m12, m21, m22, dx, dy));
	}

	// TODO - check
	public void jsFunction_setTransform(Double m11, Double m12, Double m21, Double m22, Double dx, Double dy) {
		graphics.transform(new AffineTransform(m11, m12, m21, m22, dx, dy));
	}

	public Double jsGet_globalAlpha() {
		return globalAlpha.doubleValue();
	}

	public void jsSet_globalAlpha(Double globalAlpha) {
		this.globalAlpha = globalAlpha.floatValue();
		jsSet_globalCompositeOperation(globalCompositeOperation);
	}

	public String jsGet_globalCompositeOperation() {
		return globalCompositeOperation;
	}

	public void jsSet_globalCompositeOperation(String op) {
		globalCompositeOperation = op;

		// TODO - this just reeks of needing a hash table

		int c;
		if ("source-atop".equals(op)) {
			c = AlphaComposite.SRC_ATOP;
		} else if ("source-in".equals(op)) {
			c = AlphaComposite.SRC_IN;
		} else if ("source-out".equals(op)) {
			c = AlphaComposite.SRC_OUT;
		} else if ("destination-atop".equals(op)) {
			c = AlphaComposite.DST_ATOP;
		} else if ("destination-in".equals(op)) {
			c = AlphaComposite.DST_IN;
		} else if ("destination-out".equals(op)) {
			c = AlphaComposite.DST_OUT;
		} else if ("destination-over".equals(op)) {
			c = AlphaComposite.DST_OVER;
		} else if ("xor".equals(op)) {
			c = AlphaComposite.XOR;
		} else if ("over".equals(op)) {
			c = AlphaComposite.CLEAR;
		} else {
			c = AlphaComposite.SRC_OVER;
		}

		graphics.setComposite(AlphaComposite.getInstance(c, globalAlpha));
	}

	public Object jsGet_fillStyle() {
		return fillStyle;
	}

	public void jsSet_fillStyle(Object fillStyle) {
		this.fillStyle = fillStyle;

		if (fillStyle instanceof String) {
			Value fs = new Value(((String) fillStyle).toLowerCase());
			fillPaint = new Color(fs.getColor(), true);
		} else if (fillStyle instanceof CanvasGradient) {
			fillPaint = ((CanvasGradient) fillStyle).getPaint();
		} else if (fillStyle instanceof CanvasPattern) {
			fillPaint = ((CanvasPattern) fillStyle).getPaint();
		}
	}

	public Object jsGet_strokeStyle() {
		return strokeStyle;
	}

	public void jsSet_strokeStyle(Object strokeStyle) {
		this.strokeStyle = strokeStyle;

		if (strokeStyle instanceof String) {
			Value fs = new Value(((String) strokeStyle).toLowerCase());
			strokePaint = new Color(fs.getColor(), true);
		} else if (strokeStyle instanceof CanvasGradient) {
			strokePaint = ((CanvasGradient) strokeStyle).getPaint();
		} else {
			strokePaint = ((CanvasPattern) strokeStyle).getPaint();
		}
	}

	public CanvasGradient jsFunction_createLinearGradient(Double x0, Double y0, Double x1, Double y1) {
		return RhinoCanvasUtils.getScriptableInstance(LinearCanvasGradient.class, new Object[] { x0, y0, x1, y1 });
	}

	public CanvasGradient jsFunction_createRadialGradient(Double x0, Double y0, Double r0, Double x1, Double y1,
			Double r1) {
		return RhinoCanvasUtils.getScriptableInstance(RadialCanvasGradient.class,
				new Object[] { x0, y0, r0, x1, y1, r1 });
	}

	public CanvasPattern jsFunction_createPattern(Image image, String repetition) {
		return new CanvasPattern(image, repetition);
	}

	public CanvasTextStyle jsFunction_createTextStyle() {
		return new CanvasTextStyle(this);
	}

	private void updateStroke() {
		int cap;

		if (lineCap.equals("round")) {
			cap = BasicStroke.CAP_ROUND;
		} else if (lineCap.equals("square")) {
			cap = BasicStroke.CAP_SQUARE;
		} else {
			cap = BasicStroke.CAP_BUTT;
		}

		// round, bevel and miter. By default this property is set to miter.
		int join;
		if (lineJoin.equals("round")) {
			join = BasicStroke.JOIN_ROUND;
		} else if (lineJoin.equals("bevel")) {
			join = BasicStroke.JOIN_BEVEL;
		} else {
			join = BasicStroke.JOIN_MITER;
		}

		graphics.setStroke(new BasicStroke(lineWidth, cap, join, miterLimit));
	}

	public void jsSet_lineWidth(Double lw) {
		if (lineWidth != lw.floatValue()) {
			lineWidth = lw.floatValue();
			updateStroke();
		}
	}

	public Object jsGet_lineWidth() {
		return lineWidth;
	}

	public void jsSet_lineCap(String cap) {
		this.lineCap = cap;
		updateStroke();
	}

	public String jsGet_lineCap() {
		return lineCap;
	}

	public void jsSet_lineJoin(String join) {
		this.lineJoin = join;
		updateStroke();
	}

	public String jsGet_lineJoin() {
		return lineJoin;
	}

	public void jsSet_miterLimit(Double miterLimit) {
		this.miterLimit = miterLimit.floatValue();
		updateStroke();
	}

	public float jsGet_miterLimit() {
		return miterLimit;
	}

	public void jsFunction_clearRect(Double x, Double y, Double w, Double h) {
		graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, globalAlpha));
		graphics.setPaint(Color.WHITE);
		graphics.fill(new Rectangle2D.Double(x, y, w, h));
		jsSet_globalAlpha(globalAlpha.doubleValue());
		canvas.dirty();
	}

	public void jsFunction_fillRect(Double x, Double y, Double w, Double h) {
		graphics.setPaint(fillPaint);
		graphics.fill(new Rectangle2D.Double(x, y, w, h));
		canvas.dirty();
	}

	public void jsFunction_strokeRect(Double x, Double y, Double w, Double h) {
		graphics.setPaint(strokePaint);
		graphics.draw(new Rectangle2D.Float(x.floatValue(), y.floatValue(), w.floatValue(), h.floatValue()));
		canvas.dirty();
	}

	public void jsFunction_beginPath() {
		path = new GeneralPath();
	}

	public void jsFunction_closePath() {
		path.closePath();
	}

	public void jsFunction_moveTo(Double x, Double y) {
		Point2D p = new Point2D.Double(x, y);
		graphics.getTransform().transform(p, p);
		path.moveTo(p.getX(), p.getY());
	}

	public void jsFunction_lineTo(Double x, Double y) {
		Point2D p = new Point2D.Double(x, y);
		graphics.getTransform().transform(p, p);
		path.lineTo(p.getX(), p.getY());
	}

	public void jsFunction_quadraticCurveTo(Double cpx, Double cpy, Double x, Double y) {
		double[] xy = { cpx, cpy, x, y };
		graphics.getTransform().transform(xy, 0, xy, 0, 2);
		path.quadTo(xy[0], xy[1], xy[2], xy[3]);
	}

	public void jsFunction_bezierCurveTo(Double cp1x, Double cp1y, Double cp2x, Double cp2y, Double x, Double y) {
		float[] xy = { cp1x.floatValue(), cp1y.floatValue(), cp2x.floatValue(), cp2y.floatValue(), x.floatValue(),
				y.floatValue() };
		graphics.getTransform().transform(xy, 0, xy, 0, 3);
		path.curveTo(xy[0], xy[1], xy[2], xy[3], xy[4], xy[5]);
	}

	public void jsFunction_arcTo(Double x1, Double y1, Double x2, Double y2, Double radius) {
		System.out.println("arcTo Not Yet implemented...");
	}

	public void jsFunction_rect(Double x, Double y, Double w, Double h) {
		path.append(new Rectangle2D.Double(x, y, w, h), true);
	}

	public void jsFunction_arc(Double x, Double y, Double radius, Double startAngle, Double endAngle,
			boolean counterclockwise) {

		boolean clockwise = !counterclockwise;
		double twopi = 2 * Math.PI;

		while (startAngle < 0) {
			startAngle += twopi;
		}
		while (startAngle > twopi) {
			startAngle -= twopi;
		}

		while (endAngle < 0) {
			endAngle += twopi;
		}
		while (endAngle > twopi) {
			endAngle -= twopi;
		}

		if (clockwise) {
			if (startAngle > endAngle) {
				endAngle += twopi;
			}
			// ang must be negative!

		} else {
			if (startAngle < endAngle) {
				endAngle -= twopi;
			}

			// ang must be positve!
		}
		double ang = startAngle - endAngle;

		// TODO: is this correct? cf:
		// http://developer.mozilla.org/en/docs/Canvas_tutorial:Drawing_shapes
		if (ang == 0.0) {
			ang = Math.PI * 2;
		}

		startAngle = -startAngle;

		path.append(graphics.getTransform().createTransformedShape(
				new Arc2D.Double(x - radius, y - radius, 2 * radius, 2 * radius, Math.toDegrees(startAngle), Math
						.toDegrees(ang), Arc2D.OPEN)), true);
	}

	public void jsFunction_fill() {
		AffineTransform t = graphics.getTransform();
		graphics.setTransform(new AffineTransform());
		graphics.setPaint(fillPaint);
		graphics.fill(path);
		graphics.setTransform(t);
		canvas.dirty();
	}

	public void jsFunction_stroke() {

		graphics.setPaint(strokePaint);

		try {
			graphics.draw(graphics.getTransform().createInverse().createTransformedShape(path));
		} catch (NoninvertibleTransformException e) {
			e.printStackTrace();
		}

		canvas.dirty();
	}

	public void jsFunction_clip() {
		AffineTransform t = graphics.getTransform();
		graphics.setTransform(new AffineTransform());
		graphics.setClip(path);
		graphics.setTransform(t);
	}

	public boolean jsFunction_isPointInPath(Double x, Double y) {
		Point2D p = new Point2D.Double(x, y);
		graphics.getTransform().transform(p, p);
		return path.contains(p);
	}

	public void jsFunction_drawImage(Image image, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
		AffineTransform at;

		if (sw == 0 && sh == 0 && dx == 0 && dy == 0 && dw == 0 && dh == 0) {
			int local_dx = sx;
			int local_dy = sy;

			at = new AffineTransform();
			at.setToTranslation(local_dx, local_dy);

		} else if (dx == 0 && dy == 0 && dw == 0 && dh == 0) {
			float local_dx = sx;
			float local_dy = sy;
			float local_dw = sw;
			float local_dh = sh;
			at = new AffineTransform(local_dw / image.getRealWidth(), 0, 0, local_dh / image.getRealHeight(), local_dx,
					local_dy);
		} else {
			Graphics2D g = (Graphics2D) graphics.create();
			g.clip(new Rectangle2D.Float(dx, dy, dw, dh));

			float scaleX = (float) dw / sw;
			float scaleY = (float) dh / sh;

			float x0 = dx - sx * scaleX;
			float y0 = dy - sy * scaleY;

			at = new AffineTransform(scaleX, 0, 0, scaleY, x0, y0);
		}

		graphics.drawImage(image.getImage(), at, null);
		// image.dirty();
		// canvas.getHelper().repaint();
	}

	@Override
	public TextMetrics jsFunction_measureText(String text) {
		FontMetrics metrics = textStyle.getMetrics();
		Integer width = metrics.stringWidth(text);

		return RhinoCanvasUtils.getScriptableInstance(TextMetrics.class, new Double[] { width.doubleValue() });
	}

	@Override
	public String jsGet_font() {
		return CanvasTextStyle.getFontStyle(textStyle);
	}

	@Override
	public void jsSet_font(String font) {
		CanvasTextStyle.fontParser(font, textStyle);
	}

	@Override
	public void jsFunction_fillText(String text, Double x, Double y, int maxWidth) {
		if (textStyle != null) {
			Point textPoint = textStyle.calculateTextPosition(text, x, y, maxWidth);

			updateStroke();
			graphics.setPaint(fillPaint);

			graphics.setFont(textStyle.getFont());
			graphics.drawString(text, textPoint.x, textPoint.y);
			canvas.dirty();
		}
	}

	@Override
	public void jsFunction_strokeText(String text, Double x, Double y, int maxWidth) {
		if (textStyle != null) {
			Point textPoint = textStyle.calculateTextPosition(text, x, y, maxWidth);

			updateStroke();
			graphics.setPaint(fillPaint);

			GlyphVector gv = textStyle.getFont().createGlyphVector(graphics.getFontRenderContext(), text);

			graphics.translate(textPoint.x, textPoint.y);
			graphics.draw(gv.getOutline());

			canvas.dirty();
		}
	}

	@Override
	public String jsGet_textAlign() {
		return textStyle.getTextAlign();
	}

	@Override
	public String jsGet_textBaseline() {
		return textStyle.getVerticalAlign();
	}

	@Override
	public void jsSet_textAlign(String textAlign) {
		textStyle.setTextAlign(textAlign);
	}

	@Override
	public void jsSet_textBaseline(String textBaseline) {
		textStyle.setVerticalAlign(textBaseline);
	}

	public ImageData jsFunction_createImageData(int width, int height) {
		return RhinoCanvasUtils.getScriptableInstance(ImageData.class, new Integer[] { width, height, null });
	}

	public ImageData jsFunction_getImageData(int x, int y, int width, int height) {
		CanvasPixelArray pxArray = null;
		int[] pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(canvas.getImage(), x, y, width, height, pixels, 0, width);
		try {
			pg.grabPixels();

			if ((pg.getStatus() & ImageObserver.ABORT) != 0) {
				System.err.println("image fetch aborted or errored");
			} else {
				pxArray = CanvasPixelArray.getInstance(pixels, width, height);
			}

		} catch (InterruptedException e) {
			System.err.println("interrupted waiting for pixels!");
		}

		return RhinoCanvasUtils.getScriptableInstance(ImageData.class, new Object[] { width, height, pxArray });
	}

	public void jsFunction_putImageData(ImageData imagedata, int dx, int dy, int dirtyX, int dirtyY, int dirtyWidth,
			int dirtyHeight) {
		AffineTransform at = new AffineTransform();
		at.setToTranslation(dx, dy);
		graphics.drawImage(imagedata.getImage(dirtyX, dirtyY, dirtyWidth, dirtyHeight), at, null);
		canvas.getNodePanel().repaint();
	}

	private static class ContextState {

		private AffineTransform transform;
		private Paint fillPaint;
		private Paint strokePaint;
		private Object fillStyle;
		private Object strokeStyle;
		private float globalAlpha;
		private float lineWidth;
		private String lineJoin;
		private String lineCap;
		private float miterLimit; // convert to rad?
		private String globalCompositeOperation;

		ContextState(CanvasRenderingContext2D ctx) {

			transform = ctx.graphics.getTransform();
			fillPaint = ctx.fillPaint;
			fillStyle = ctx.fillStyle;
			strokeStyle = ctx.strokeStyle;
			strokePaint = ctx.strokePaint;
			globalAlpha = ctx.globalAlpha;
			globalCompositeOperation = ctx.globalCompositeOperation;
			lineWidth = ctx.lineWidth;
			lineJoin = ctx.lineJoin;
			lineCap = ctx.lineCap;
			miterLimit = ctx.miterLimit;
		}

		public void apply(CanvasRenderingContext2D ctx) {
			ctx.graphics.setTransform(transform);
			ctx.fillPaint = fillPaint;
			ctx.jsSet_fillStyle(fillStyle);
			ctx.strokeStyle = strokeStyle;
			ctx.strokePaint = strokePaint;
			ctx.globalAlpha = globalAlpha;
			ctx.jsSet_globalCompositeOperation(globalCompositeOperation);
			ctx.lineWidth = lineWidth;
			ctx.lineJoin = lineJoin;
			ctx.lineCap = lineCap;
			ctx.miterLimit = miterLimit;
			ctx.updateStroke();
		}

	}

	private static class CanvasTextStyle {

		private String fontFamily = "SansSerif";
		private String fontStyle = "plain";
		private String fontVariant = "none";
		private String fontWeight = "regular";
		private String fontSize = "10px";
		private String verticalAlign = "alphabetic";
		private String textAlign = "start";
		private String direction = "ltr";
		private CanvasRenderingContext2D context;
		private Font font;
		private FontMetrics metrics;
		static private final float DPI = Toolkit.getDefaultToolkit().getScreenResolution();

		CanvasTextStyle(CanvasRenderingContext2D context) {
			this.context = context;
		}

		Font getFont() {
			if (font == null) {

				Value size = new Value(fontSize);

				int style = 0;
				if ("bold".equals(fontWeight)) {
					style |= Font.BOLD;
				}
				if ("italic".equals(fontVariant)) {
					style |= Font.ITALIC;
				}

				double mm;
				double nv = size.getNumValue();
				String u = size.getUnit();
				if ("px".equals(u)) {
					mm = 0.26 * nv;
				} else if ("in".equals(u)) {
					mm = 25.4 * nv;
				} else if ("cm".equals(u)) {
					mm = 10.0 * nv;
				} else if ("pt".equals(u)) {
					mm = 25.4 * nv / DPI;
				} else if ("pc".equals(u)) {
					mm = 25.4 * nv / 6.0;
				} else if ("mm".equals(u)) {
					mm = nv;
				} else {
					mm = 4 * 25.4 * nv / DPI;
				}
				font = new Font(fontFamily, style, (int) (DPI * mm / 25.4));
			}
			return font;
		}

		FontMetrics getMetrics() {
			if (metrics == null) {
				metrics = context.graphics.getFontMetrics(getFont());
			}
			return metrics;
		}

		@SuppressWarnings("unused")
		public String getFontFamily() {
			return fontFamily;
		}

		public String getDirection() {
			return direction;
		}

		public void setFontFamily(String fontFamily) {
			this.fontFamily = fontFamily;
			this.font = null;
			metrics = null;
		}

		@SuppressWarnings("unused")
		public String getFontSize() {
			return fontSize;
		}

		public void setFontSize(String fontSize) {
			this.fontSize = fontSize;
			this.font = null;
			metrics = null;
		}

		public boolean decreaseFontSize() {
			Value size = new Value(fontSize);
			Integer nv = Integer.valueOf((int) size.getNumValue());

			if (nv - 1 <= 0) {
				return false;
			} else {
				fontSize = String.valueOf(nv - 1) + size.getUnit();
				return true;
			}
		}

		@SuppressWarnings("unused")
		public String getFontStyle() {
			return fontStyle;
		}

		@SuppressWarnings("unused")
		public String getFontVariant() {
			return fontVariant;
		}

		@SuppressWarnings("unused")
		public String getFontWeight() {
			return fontWeight;
		}

		public String getTextAlign() {
			return textAlign;
		}

		public void setTextAlign(String align) {
			textAlign = align;
		}

		public String getVerticalAlign() {
			return verticalAlign;
		}

		public void setVerticalAlign(String align) {
			verticalAlign = align;
		}

		public static void fontParser(String font, CanvasTextStyle canvasTextStyle) {
			String[] splFontParams = font.split(" ");
			StringBuilder fontFamily = new StringBuilder();
			for (String attrItem : splFontParams) {
				Value val = new Value(attrItem);
				if (!val.getValue().isEmpty() && val.getUnit() != null) {
					// font size
					canvasTextStyle.setFontSize(val.getValue());
				} else {
					fontFamily.append(val.getValue()).append(" ");
				}
			}

			String fntFamily = fontFamily.toString();
			fntFamily = fntFamily.replaceAll("\\s+$", "");

			if (!fntFamily.isEmpty()) {
				canvasTextStyle.setFontFamily(fntFamily);
			}
		}

		public static String getFontStyle(CanvasTextStyle textStyle) {
			return textStyle.fontStyle + ' ' + textStyle.fontVariant + ' ' + textStyle.fontWeight + ' '
					+ textStyle.fontSize + ' ' + textStyle.fontFamily;
		}

		public Point calculateTextPosition(String text, Double x1, Double y1, int maxWidth) {
			int x = x1.intValue();
			int y = y1.intValue();

			FontMetrics metrics = getMetrics();

			if (maxWidth != 0) {
				do {
					Float strWidth = Float.valueOf(metrics.stringWidth(text));
					if (maxWidth >= strWidth || !decreaseFontSize()) {
						break;
					}

					metrics = getMetrics();
				} while (true);
			}

			String ta = getTextAlign();
			Integer stringWidth = metrics.stringWidth(text);

			if ("center".equals(ta)) {
				x = x - metrics.stringWidth(text) / 2;
			} else if ("right".equals(ta)) {
				x = x - metrics.stringWidth(text);
			} else if ("start".equals(ta)) {
				x -= ("ltr".equals(getDirection())) ? 0 : stringWidth;
			} else if ("end".equals(ta)) {
				x -= ("ltr".equals(getDirection())) ? stringWidth : 0;
			}

			String va = getVerticalAlign();

			if ("alphabetic".equals(va)) {
			} else if ("middle".equals(va)) {
				y -= (metrics.getAscent() + metrics.getDescent()) / 2;
			} else if ("bottom".equals(va) || "ideographic".equals(va)) {
				y -= metrics.getDescent();
			} else if ("top".equals(va) || "hanging".equals(va)) {
				y += metrics.getAscent() + metrics.getLeading();
			}

			return new Point(x, y);
		}
	}

}
