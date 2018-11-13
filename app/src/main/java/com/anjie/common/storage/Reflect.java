package com.anjie.common.storage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 反射
 * 
 * @author jimmy
 */
@SuppressWarnings("rawtypes")
public class Reflect
{
    public static boolean hasField(Class class1, String field)
    {
        try
        {
            class1.getDeclaredField(field);
        }
        catch (NoSuchFieldException e)
        {
            return false;
        }
        return true;
    }

    public static int getInt(Class class1, String method)
    {
        int i = 0;
        Field field;
        try
        {
            field = class1.getDeclaredField(method);
            i = field.getInt(field.getInt(class1));
        }
        catch (NoSuchFieldException e)
        {
            return -1;
        }
        catch (IllegalArgumentException e)
        {
            return -1;
        }
        catch (IllegalAccessException e)
        {
            return -1;
        }
        return i;
    }

    @SuppressWarnings("unchecked")
    public static boolean hasMethod(Class class1, String method, Class[] args)
    {
        try
        {
            class1.getDeclaredMethod(method, args);
        }
        catch (NoSuchMethodException e)
        {
            return false;
        }
        return true;
    }

    public static Method getMethod(Object object, String cmd, Class[] parTypes)
    {
        try
        {
            return object.getClass().getMethod(cmd, parTypes);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }

    public static Object invoke(Object object, Method method, Object[] args)
    {
        try
        {
            return method.invoke(object, args);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
        catch (IllegalAccessException e)
        {
            return null;
        }
        catch (InvocationTargetException e)
        {
            return null;
        }

    }
}
