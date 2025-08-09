package com.w3canvas.javacanvas.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertiesHolder
{

    private static PropertiesHolder instance;

    private Properties props;

    private static final String JS_PACKAGE_KEY = "jsPackage";
    private static final String CLP_DEFAULT_PHOTO = "-DdefaultPhoto";
    private static final String[] COMMAND_LINE_PARAMS = new String[]{CLP_DEFAULT_PHOTO};
    private static final String APP_TITLE_KEY = "appTitle";

    private PropertiesHolder()
    {
        props = new Properties();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("META-INF/config.properties");

        boolean propertiesLoaded = false;
        try
        {
            if (in != null)
            {
                props.load(in);
                propertiesLoaded = true;
            }
        }
        catch (IOException e)
        {
            // handle correct exception
            e.printStackTrace();
        }
        if (!propertiesLoaded)
        {
            System.out.println("Warning: could not load META-INF/config.properties. Using default values.");
        }
    }

    public static PropertiesHolder getInstance()
    {
        if (instance == null)
        {
            instance = new PropertiesHolder();
        }

        return instance;
    }

    public Properties getProperties()
    {
        return props;
    }

    private static String getJSClassName(String className)
    {
        String[] strArr = className.split("\\.");
        StringBuffer classNameBuffer = new StringBuffer();

        for (int i = 0; i < strArr.length - 1; i++)
        {
            if (classNameBuffer.length() > 0)
            {
                classNameBuffer.append("_");
            }
            classNameBuffer.append(strArr[i]);
        }

        return classNameBuffer.toString();
    }

    public static List<String> getJSClasses(Properties properties)
    {
        List<String> classes = new ArrayList<String>();
        String value = (String) properties.get("jsFiles");
        String packageName = properties.get(JS_PACKAGE_KEY) == null ? "com.Project.js" : (String) properties
            .get(JS_PACKAGE_KEY);

        String[] arrJsClasses = value.split(",");
        for (String className : arrJsClasses)
        {
            classes.add(packageName + "." + getJSClassName(className));
        }

        return classes;
    }

    public String getAppTitle()
    {
        return props.getProperty(APP_TITLE_KEY) == null ? "Project v. 0.1a" : props.getProperty(APP_TITLE_KEY);
    }

    public void processCommandLineParams(String[] args)
    {
        for (String paramItem : args)
        {
            for (String paramKey : COMMAND_LINE_PARAMS)
            {
                if (paramItem.startsWith(paramKey))
                {
                    String[] arrParamItem = paramItem.split("=");
                    if (arrParamItem.length == 2 && arrParamItem[0].trim().equals(paramKey))
                    {
                        props.put(paramKey, arrParamItem[1].trim());
                    }
                }
            }
        }
    }

    public String getBaseDir()
    {
        ClassLoader cld = Thread.currentThread().getContextClassLoader();
        return cld.getResource("").toString();
    }

}
