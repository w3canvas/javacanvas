package com.w3canvas.javacanvas.js.impl.gradient;

import java.awt.Color;
import java.awt.Paint;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import net.sf.css4j.Value;
import com.w3canvas.javacanvas.js.impl.node.ProjectScriptableObject;

@SuppressWarnings("serial")
public abstract class CanvasGradient extends ProjectScriptableObject
{

    private Double x1;
    private Double y1;
    private Double x2;
    private Double y2;
    private Double r1;
    private Double r2;
    private TreeMap<Float, String> stops = new TreeMap<Float, String>();
    private Paint paint;

    protected void addColorStop(Double where, String color)
    {
        if (stops.get(where.floatValue()) != null)
        {
            where += where / 1000;
        }

        stops.put(where.floatValue(), color);
    }

    public Paint getPaint()
    {
        if (paint == null)
        {
            float[] where = new float[stops.size()];
            Color[] color = new Color[stops.size()];

            int i = 0;

            for(Entry<Float, String> e : stops.entrySet()) {
                where[i] = ((Float) e.getKey()).floatValue();
                color[i] = new Color(new Value((String) e.getValue()).getColor(), true);
                i++;
            }

            paint = createGradientPaint(where, color);
        }

        return paint;
    }

    protected abstract Paint createGradientPaint(float[] where, Color[] color);

    public abstract void jsFunction_addColorStop(Double where, String color);

    public Double getX1()
    {
        return x1;
    }

    public void setX1(Double x1)
    {
        this.x1 = x1;
    }

    public Double getY1()
    {
        return y1;
    }

    public void setY1(Double y1)
    {
        this.y1 = y1;
    }

    public Double getX2()
    {
        return x2;
    }

    public void setX2(Double x2)
    {
        this.x2 = x2;
    }

    public Double getY2()
    {
        return y2;
    }

    public void setY2(Double y2)
    {
        this.y2 = y2;
    }

	public Double getR1() {
		return r1;
	}

	public void setR1(Double r1) {
		this.r1 = r1;
	}

	public Double getR2() {
		return r2;
	}

	public void setR2(Double r2) {
		this.r2 = r2;
	}

}
