package com.w3canvas.javacanvas.test;

import com.w3canvas.javacanvas.backend.awt.AwtGraphicsContext;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.io.InputStream;
import java.util.Scanner;

public class PrintArcTo {

    @Test
    public void printArcTo() throws Exception {
        Method method = AwtGraphicsContext.class.getDeclaredMethod("arcTo", double.class, double.class, double.class, double.class, double.class);
        System.out.println("<<<<<");
        System.out.println(getMethodSource(method));
        System.out.println(">>>>>");
    }

    private String getMethodSource(Method method) {
        try {
            String resourceName = "/" + method.getDeclaringClass().getName().replace('.', '/') + ".java";
            InputStream is = method.getDeclaringClass().getResourceAsStream(resourceName);
            if (is == null) {
                return "Could not find source for " + method.getDeclaringClass().getName();
            }
            Scanner scanner = new Scanner(is).useDelimiter("\\A");
            String source = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
            // This is a very rough way to get the method body.
            // It will probably not work if the method has overloads or complex signatures.
            String methodSignature = "void " + method.getName() + "(";
            int start = source.indexOf(methodSignature);
            int braceCount = 0;
            int end = -1;
            boolean inString = false;
            boolean inChar = false;
            for (int i = start; i < source.length(); i++) {
                char c = source.charAt(i);
                if (c == '"' && (i == 0 || source.charAt(i - 1) != '\\')) {
                    inString = !inString;
                }
                if (c == '\'' && (i == 0 || source.charAt(i - 1) != '\\')) {
                    inChar = !inChar;
                }
                if (!inString && !inChar) {
                    if (c == '{') {
                        braceCount++;
                    } else if (c == '}') {
                        braceCount--;
                        if (braceCount == 0) {
                            end = i;
                            break;
                        }
                    }
                }
            }
            if (start != -1 && end != -1) {
                return source.substring(start, end + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Could not find method source.";
    }
}
