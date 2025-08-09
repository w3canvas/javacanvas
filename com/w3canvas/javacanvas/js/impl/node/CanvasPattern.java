package com.w3canvas.javacanvas.js.impl.node;

import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;

public class CanvasPattern extends ProjectScriptableObject
{

    enum RepetitionType
    {
        REPEAT("repeat"), REPEAT_X("repeat-x"), REPEAT_Y("repeat-y"), NO_REPEAT("no-repeat");

        private String repetitionValue;

        RepetitionType(String value)
        {
            repetitionValue = value;
        }

        public String getItemValue()
        {
            return repetitionValue;
        }

        public static RepetitionType getValue(String val)
        {
            for (RepetitionType item : RepetitionType.values())
            {
                if (item.getItemValue().equalsIgnoreCase(val))
                {
                    return item;
                }
            }

            throw new IllegalArgumentException("No enum const class " + val);
        }
    }

    private Paint paint;

    private RepetitionType repetitionType;

    public CanvasPattern()
    {
    }

    public CanvasPattern(Image image, String repetition)
    {
        repetitionType = RepetitionType.getValue(repetition);

        if (!RepetitionType.REPEAT.equals(repetitionType))
        {
            System.out.println("Currently unsupported CanvasPattern repetition: " + repetition);
        }

        paint = new TexturePaint(image.getImage(), new Rectangle2D.Float(0, 0, image.getRealWidth(), image.getRealHeight()));
    }

    public Paint getPaint()
    {
        return paint;
    }
}