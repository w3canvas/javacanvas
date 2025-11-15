package com.w3canvas.javacanvas.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single CSS filter function (e.g., blur(5px), brightness(150%))
 */
public class FilterFunction {

    public enum FilterType {
        BLUR,
        BRIGHTNESS,
        CONTRAST,
        GRAYSCALE,
        SEPIA,
        SATURATE,
        HUE_ROTATE,
        INVERT,
        OPACITY,
        DROP_SHADOW,
        NONE
    }

    private final FilterType type;
    private final List<Object> params;

    public FilterFunction(FilterType type) {
        this.type = type;
        this.params = new ArrayList<>();
    }

    public FilterFunction(FilterType type, List<Object> params) {
        this.type = type;
        this.params = params;
    }

    public FilterType getType() {
        return type;
    }

    public List<Object> getParams() {
        return params;
    }

    public void addParam(Object param) {
        params.add(param);
    }

    /**
     * Get parameter as double value
     */
    public double getDoubleParam(int index) {
        if (index < params.size() && params.get(index) instanceof Number) {
            return ((Number) params.get(index)).doubleValue();
        }
        return 0.0;
    }

    /**
     * Get parameter as string value
     */
    public String getStringParam(int index) {
        if (index < params.size()) {
            return params.get(index).toString();
        }
        return "";
    }

    /**
     * Get the number of parameters
     */
    public int getParamCount() {
        return params.size();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name().toLowerCase().replace('_', '-'));
        sb.append("(");
        for (int i = 0; i < params.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(params.get(i));
        }
        sb.append(")");
        return sb.toString();
    }
}
