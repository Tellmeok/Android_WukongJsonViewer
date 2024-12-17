package com.android.wukong.viewer.util;


import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WuKongReflect {

    public static Class getMethodGenericReturnType(Method method) {
        // https://stackoverflow.com/a/15702911
        Class<?> genericClazz = null;
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericReturnType;
            Type[] argTypes = paramType.getActualTypeArguments();
            if (argTypes.length > 0) {
                genericClazz = (Class<?>) argTypes[0];
            }
        }
        return genericClazz;
    }

    public static Object newInstanceOf(Class clazz) {
        try {
            Constructor<?>[] constructors = clazz.getDeclaredConstructors();
            for (int j = 0; j < constructors.length; j++) {
                Constructor c = constructors[j];
                c.setAccessible(true);
                Class<?>[] parameterTypes = c.getParameterTypes();
                if (parameterTypes.length == 0) {
                    // 无参构造函数
                    return c.newInstance();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newInstanceOf(Class clazz, Class[] parameterTypes, Object[] parameterValues) {
        Constructor c = null;
        while (c == null && clazz != null) {
            try {
                c = clazz.getDeclaredConstructor(parameterTypes);
            } catch (Exception e) {
                e.printStackTrace();
            }
            clazz = clazz.getSuperclass();
        }

        Object instance = null;
        if (c != null) {
            try {
                c.setAccessible(true);
                instance = c.newInstance(parameterValues);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    public interface IterateFieldHandler {
        public boolean action(Class<?> clazz, Field field, String fieldName);
    }

    public static void iterateFields(Class<?> clazz, IterateFieldHandler handler) {
        Field[] fields = clazz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true);
            String fieldName = field.getName();
            if (handler.action(clazz, field, fieldName)) {
                break;
            }
        }
    }

    /**
     * Call Object Fields And Values Using Reflect
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        Field field = null; // do not use searchField() method, cause here neen to change clazz = ...
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (field == null) {
            return null;
        }

        try {
            // clazz now may be super class now
            return field.get(Modifier.isStatic(field.getModifiers()) ? clazz : obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setFieldValue(Object obj, String fieldName, Object fieldValue) {
        if (obj == null) {
            return;
        }
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

        Field field = null; // do not use searchField() method, cause here neen to change clazz = ...
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (field == null) {
            return;
        }

        try {
            // clazz now may be super class now
            field.set(Modifier.isStatic(field.getModifiers()) ? clazz : obj, fieldValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Search field with superclass
     */
    public static Field searchField(Object obj, String fieldName) {
        return searchField(obj, fieldName, 0, null);
    }

    public static Field searchField(Object obj, String fieldName, int depth, Class<?> untilSuperClass) {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
        Field field = null;
        while (clazz != null) {
            try {
                field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }

            if (--depth == 0 || clazz == untilSuperClass) {
                break;
            }
        }
        return field;
    }


    public static List<Field> searchFieldsWithType(Object obj, String fieldClassName, int depth, Class<?> untilSuperClass) {
        if (obj == null) {
            return null;
        }
        List<Field> results = new ArrayList<>();
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (Field f : fields) {
                Class<?> type = f.getType();
                if (type.getName().equals(fieldClassName)) {
                    // --------------------
                    results.add(f);
                    // --------------------
                }
            }
            clazz = clazz.getSuperclass();

            if (--depth == 0 || clazz == untilSuperClass) {
                break;
            }
        }
        return results;
    }

    public static Field searchFieldWithType(Object obj, String fieldClassName, int depth, Class<?> untilSuperClass) {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; fields != null && i < fields.length; i++) {
                Field f = fields[i];
                Class<?> type = f.getType();
                if (type.getName().equals(fieldClassName)) {
                    return f;
                }
            }
            clazz = clazz.getSuperclass();

            if (--depth == 0 || clazz == untilSuperClass) {
                break;
            }
        }
        return null;
    }

    public static Object getFieldValueWithType(Object obj, String fieldClassName, int depth, Class<?> untilSuperClass) {
        if (obj == null) {
            return null;
        }
        Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; fields != null && i < fields.length; i++) {
                Field f = fields[i];
                Class<?> type = f.getType();
                if (type.getName().equals(fieldClassName)) {
                    // --------------------
                    f.setAccessible(true);
                    Object v = null;
                    try {
                        v = f.get(obj);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return v;
                    // --------------------
                }
            }
            clazz = clazz.getSuperclass();

            if (--depth == 0 || clazz == untilSuperClass) {
                break;
            }
        }
        return null;
    }


    public interface FieldFilter {
        boolean filterAction(Object obj, Field field);
    }

    public static Map<?, ?> objectFieldNameValues(Object obj) {
        return objectFieldNameValues(0, obj, null);
    }

    public static Map<?, ?> objectFieldNameValues(Object obj, FieldFilter fieldFilter) {
        return objectFieldNameValues(0, obj, fieldFilter);
    }

    public static Map<?, ?> objectFieldNameValues(int depth, Object obj, FieldFilter fieldFilter) {
        Map<String, Object> result = new HashMap<String, Object>();

        Boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();

        do {

            Field[] fields = clazz.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                try {
                    Field field = fields[i];
                    if (fieldFilter != null && fieldFilter.filterAction(obj, field)) {
                        continue;
                    }
                    field.setAccessible(true);

                    String name = field.getName();
                    Object value = field.get(Modifier.isStatic(field.getModifiers()) ? clazz : obj);

                    if (value == null) {
                        value = "<NULL>";
                    }
                    result.put(name, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // get super class fields
            depth--;
            clazz = clazz.getSuperclass();

        } while (depth >= 0 && clazz != null);


        return result;
    }

    /**
     * Call Object all methods with return value and without parameters
     */
    public interface MethodFilter {
        boolean filterAction(Method method);
    }

    public static Map<?, ?> invokeObjectAllNonVoidZeroArgsMethods(Object obj) {
        return invokeObjectAllNonVoidZeroArgsMethods(obj, null);
    }

    public static Map<?, ?> invokeObjectAllNonVoidZeroArgsMethods(Object obj, MethodFilter filter) {
        Map<String, Object> result = new HashMap<String, Object>();

        boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (filter != null && filter.filterAction(method)) {
                continue;
            }
            method.setAccessible(true);
            String methodName = method.getName();
            try {
                Class<?>[] types = method.getParameterTypes();
                Class<?> returnType = method.getReturnType();
                if (returnType != void.class && types.length == 0) {
                    Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                    result.put(methodName, value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Call Object all "get" methods without parameters
     */
    public static Map<?, ?> invokeObjectAllGetMethods(Object obj) {
        Map<String, Object> result = new HashMap<String, Object>();

        boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            method.setAccessible(true);
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                try {
                    Class<?>[] types = method.getParameterTypes();
                    if (types.length == 0) {
                        Object value = method.invoke(Modifier.isStatic(method.getModifiers()) ? clazz : obj, new Object[]{});
                        result.put(methodName, value);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    /**
     * About Field Signature
     */
    public static String getFieldSignature(Class<?> clz, String fieldName) {
        String result = "";
        Field[] fields = clz.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = fields[i];
                String name = field.getName();
                if (name.equals(fieldName)) {
                    Method sigMethod = field.getClass().getDeclaredMethod("getSignatureAttribute", new Class[]{});
                    sigMethod.setAccessible(true);
                    String signatureStr = (String) sigMethod.invoke(field, new Object[]{});
                    result = signatureStr;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * About Method Signature
     */
    public static String getMethodSignature(Class<?> clz, String methodName) {
        String result = null;
        Method target = null;
        Method[] methods = clz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            try {
                Method method = methods[i];
                String name = method.getName();
                if (name.equals(methodName)) {
                    target = method;

                    Method getSigMethod = searchMethod(method, "getSignatureAttribute", new Class[]{});
                    getSigMethod.setAccessible(true);
                    String signatureStr = (String) getSigMethod.invoke(method, new Object[]{});

                    if (signatureStr != null && !signatureStr.isEmpty()) {
                        result = signatureStr;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (result == null) {
            result = calculateMethodSignature(target);
        }

        return result == null ? "" : result;
    }

    public static String calculateMethodSignature(Method method) {
        // https://stackoverflow.com/a/8066268
        /**
         Signature    Java Type
         Z    boolean
         B    byte
         C    char
         S    short
         I    int
         J    long
         F    float
         D    double
         V    void
         L fully-qualified-class ;    fully-qualified-class
         [ type   type[]

         */
        String signature = "";
        if (method != null) {
            signature += "(";
            for (Class<?> type : method.getParameterTypes()) {
                String Lsig = Array.newInstance(type, 1).getClass().getName();
                Lsig = Lsig.substring(1);
                signature += Lsig;
            }
            signature += ")";

            Class<?> returnType = method.getReturnType();
            if (returnType == void.class) {
                signature += "V";
            } else {
                String Lsig = Array.newInstance(returnType, 1).getClass().getName();
                Lsig = Lsig.substring(1);
                signature += Lsig;
            }
            signature = signature.replace('.', '/');
        }

        return signature;
    }

    /**
     * About Method Parameters Types
     */
    public static List<Class<?>[]> getMethodParameterTypes(String className, String methodName) {
        List<Class<?>[]> result = new ArrayList<Class<?>[]>();
        try {
            Class<?> clz = Class.forName(className);
            Method[] methods = clz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];
                String name = method.getName();
                if (name.equals(methodName)) {
                    Class<?>[] types = method.getParameterTypes();
                    result.add(types);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Call Object Method Using Reflect
     */
    public static Object invokeClassMethod(String className, String methodName, Class<?>[] argsTypes, Object[] args) {
        ClassLoader classLoader = String.class.getClassLoader();
        return invokeClassMethod(className, classLoader, methodName, argsTypes, args);
    }

    public static Object invokeClassMethod(String className, ClassLoader classLoader, String methodName, Class<?>[] argsTypes, Object[] args) {
        try {
            Class<?> clazz = classLoader.loadClass(className);
            return invokeMethod(clazz, methodName, argsTypes, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object invokeMethod(Object obj, String methodName) {
        return invokeMethod(obj, methodName, null);
    }

    public static Object executeMethod(Object obj, String methodName, Object... args) {
        return invokeMethod(obj, methodName, args);
    }

    public static Object invokeMethod(Object obj, String methodName, Object[] args) {
        Class<?>[] argsTypes = null;
        if (args != null) {
            argsTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argsTypes[i] = args[i].getClass();
            }
        }
        return invokeMethod(obj, methodName, argsTypes, args);
    }

    public static Object invokeMethod(Object obj, String methodName, Class<?>[] argsTypes, Object[] args) {
        try {
            Method method = searchMethod(obj, methodName, argsTypes);
            method.setAccessible(true);
            return method.invoke(obj, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Search Method With Class Types
     */
    public static Method getMethodWithName(Class clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        for (int i = 0; i < methods.length; i++) {
            try {
                Method method = methods[i];
                String name = method.getName();
                if (name.equals(methodName)) {
                    return method;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Method searchMethodWithName(Class clazz, String methodName) {
        Method result = null;
        Class superClazz = clazz;
        while (result == null && superClazz != null) {
            Method[] methods = superClazz.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                try {
                    Method method = methods[i];
                    String name = method.getName();
                    if (name.equals(methodName)) {
                        result = method;
                        return method;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            superClazz = superClazz.getSuperclass();
        }
        return null;
    }

    public static Method searchMethod(Object obj, String methodName, String[] classTypesStringPresentations) throws NoSuchMethodException {
        Class<?>[] argsTypes = parseClassTypes(classTypesStringPresentations);
        return searchMethod(obj, methodName, argsTypes);
    }

    public static Method searchMethod(Object obj, String methodName, Class<?>[] argsTypes) throws NoSuchMethodException {
        if (obj == null) {
            return null;
        }
        boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();
        try {
            return clazz.getDeclaredMethod(methodName, argsTypes);
        } catch (NoSuchMethodException e) {
            Method method = recursiveSearchMethod(clazz.getSuperclass(), methodName, argsTypes);
            if (method == null) {
                throw e;
            } else {
                return method;
            }
        }
    }

    public static Method recursiveSearchMethod(Object obj, String methodName, Class<?>[] argsTypes) {
        if (obj == null) {
            return null;
        }
        boolean isClass = obj instanceof Class;
        Class<?> clazz = isClass ? (Class<?>) obj : obj.getClass();
        try {
            Method method = clazz.getDeclaredMethod(methodName, argsTypes);
            return method;
        } catch (NoSuchMethodException e) {
            return recursiveSearchMethod(clazz.getSuperclass(), methodName, argsTypes);
        }
    }

    public static Class<?>[] parseClassTypes(String[] classNames) {
        Class<?>[] classTypes = new Class[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
            String type = classNames[i];
            Class clazz = parseClassType(type);
            classTypes[i] = clazz;
        }
        return classTypes;
    }

    public static Class<?> parseClassType(String className) {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "short":
                return short.class;
            case "int":
                return int.class; // Integer.TYPE; // (Class<Integer>) Class.getPrimitiveClass("int");
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            case "char":
                return char.class;
            case "void":
                return void.class;
            default:
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
        }
        return null;
    }

    public static Object parseValueOfNumber(Class<?> fieldType, Object fieldValue) {
        if (fieldValue instanceof Number) {
            if (fieldType == byte.class || fieldType == Byte.class) {
                fieldValue = ((Number) fieldValue).byteValue();
            } else if (fieldType == double.class || fieldType == Double.class) {
                fieldValue = ((Number) fieldValue).doubleValue();
            } else if (fieldType == float.class || fieldType == Float.class) {
                fieldValue = ((Number) fieldValue).floatValue();
            } else if (fieldType == int.class || fieldType == Integer.class) {
                fieldValue = ((Number) fieldValue).intValue();
            } else if (fieldType == long.class || fieldType == Long.class) {
                fieldValue = ((Number) fieldValue).longValue();
            } else if (fieldType == short.class || fieldType == Short.class) {
                fieldValue = ((Number) fieldValue).shortValue();
            } else if (fieldType == char.class || fieldType == Character.class) {
                fieldValue = (char) ((Number) fieldValue).intValue();
            } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                fieldValue = ((Number) fieldValue).intValue() == 1;
            }
        }
        return fieldValue;
    }

    public static Object parseValueOfString(Class<?> fieldType, Object fieldValue) {
        if (fieldValue instanceof String) {
            try {
                if (fieldType == byte.class || fieldType == Byte.class) {
                    return Byte.valueOf((String) fieldValue);
                } else if (fieldType == double.class || fieldType == Double.class) {
                    return Double.valueOf((String) fieldValue);
                } else if (fieldType == float.class || fieldType == Float.class) {
                    return Float.valueOf((String) fieldValue);
                } else if (fieldType == int.class || fieldType == Integer.class) {
                    return Integer.valueOf((String) fieldValue);
                } else if (fieldType == long.class || fieldType == Long.class) {
                    return Long.valueOf((String) fieldValue);
                } else if (fieldType == short.class || fieldType == Short.class) {
                    return Short.valueOf((String) fieldValue);
                } else if (fieldType == char.class || fieldType == Character.class) {
                    char[] chars = ((String) fieldValue).toCharArray();
                    return chars.length >= 1 ? chars[0] : fieldValue;
                } else if (fieldType == boolean.class || fieldType == Boolean.class) {
                    return Boolean.valueOf((String) fieldValue);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean setValueOfNumber(Object array, int index, Class<?> element_class, Object element_value) {
        if (element_value instanceof Number) {
            // primitive array types

            if (element_class == byte.class) {
                ((byte[]) array)[index] = ((Number) element_value).byteValue();
                return true;
            }
            if (element_class == double.class) {
                ((double[]) array)[index] = ((Number) element_value).doubleValue();
                return true;
            }
            if (element_class == float.class) {
                ((float[]) array)[index] = ((Number) element_value).floatValue();
                return true;
            }
            if (element_class == int.class) {
                ((int[]) array)[index] = ((Number) element_value).intValue();
                return true;
            }
            if (element_class == long.class) {
                ((long[]) array)[index] = ((Number) element_value).longValue();
                return true;
            }
            if (element_class == short.class) {
                ((short[]) array)[index] = ((Number) element_value).shortValue();
                return true;
            }
            if (element_class == char.class) {
                ((char[]) array)[index] = (char) ((Number) element_value).intValue();
                return true;
            }
            if (element_class == boolean.class) {
                ((boolean[]) array)[index] = ((Number) element_value).intValue() == 1;
                return true;
            }

            // object array types

            if (element_class == Byte.class) {
                ((Byte[]) array)[index] = ((Number) element_value).byteValue();
                return true;
            }
            if (element_class == Double.class) {
                ((Double[]) array)[index] = ((Number) element_value).doubleValue();
                return true;
            }
            if (element_class == Float.class) {
                ((Float[]) array)[index] = ((Number) element_value).floatValue();
                return true;
            }
            if (element_class == Integer.class) {
                ((Integer[]) array)[index] = ((Number) element_value).intValue();
                return true;
            }
            if (element_class == Long.class) {
                ((Long[]) array)[index] = ((Number) element_value).longValue();
                return true;
            }
            if (element_class == Short.class) {
                ((Short[]) array)[index] = ((Number) element_value).shortValue();
                return true;
            }
            if (element_class == Character.class) {
                ((Character[]) array)[index] = (char) ((Number) element_value).intValue();
                return true;
            }
            if (element_class == Boolean.class) {
                ((Boolean[]) array)[index] = ((Number) element_value).intValue() == 1;
                return true;
            }

            if (element_class == Number.class) {
                ((Number[]) array)[index] = (Number) element_value;
                return true;
            }
        }
        return false;
    }

    public static Class<?> getElementClassForArray(Object array) {
        if (!array.getClass().isArray()) {
            return null;
        }

        Class<?> element_clazz = null;
        if (array instanceof Boolean[]) {
            element_clazz = Boolean.class;
        } else if (array instanceof Character[]) {
            element_clazz = Character.class;
        } else if (array instanceof Byte[]) {
            element_clazz = Byte.class;
        } else if (array instanceof Double[]) {
            element_clazz = Double.class;
        } else if (array instanceof Float[]) {
            element_clazz = Float.class;
        } else if (array instanceof Integer[]) {
            element_clazz = Integer.class;
        } else if (array instanceof Long[]) {
            element_clazz = Long.class;
        } else if (array instanceof Short[]) {
            element_clazz = Short.class;
        } else if (array instanceof Number[]) {
            element_clazz = Number.class;
        } else if (array instanceof Object[]) {
            element_clazz = Object.class;
        } else if (array instanceof boolean[]) {
            element_clazz = boolean.class;
        } else if (array instanceof char[]) {
            element_clazz = char.class;
        } else if (array instanceof byte[]) {
            element_clazz = byte.class;
        } else if (array instanceof double[]) {
            element_clazz = double.class;
        } else if (array instanceof float[]) {
            element_clazz = float.class;
        } else if (array instanceof int[]) {
            element_clazz = int.class;
        } else if (array instanceof long[]) {
            element_clazz = long.class;
        } else if (array instanceof short[]) {
            element_clazz = short.class;
        } else {
            return null;     // not support array for java.lang.reflect.Array.java
        }
        return element_clazz;
    }

    /**
     * 方法签名
     */
    public static String getDesc(Method method) {
        StringBuffer buf = new StringBuffer();
        buf.append("(");
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; ++i) {
            buf.append(getDesc(types[i]));
        }
        buf.append(")");
        buf.append(getDesc(method.getReturnType()));
        return buf.toString();
    }

    public static String getDesc(Class<?> returnType) {
        if (returnType.isPrimitive()) {
            return getPrimitiveLetter(returnType);
        }
        if (returnType.isArray()) {
            return "[" + getDesc(returnType.getComponentType());
        }
        return "L" + getType(returnType) + ";";
    }

    public static String getType(Class<?> parameterType) {
        if (parameterType.isArray()) {
            return "[" + getDesc(parameterType.getComponentType());
        }
        if (!parameterType.isPrimitive()) {
            String clsName = parameterType.getName();
            return clsName.replaceAll("\\.", "/");
        }
        return getPrimitiveLetter(parameterType);
    }

    public static String getPrimitiveLetter(Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        } else if (Void.TYPE.equals(type)) {
            return "V";
        } else if (Boolean.TYPE.equals(type)) {
            return "Z";
        } else if (Character.TYPE.equals(type)) {
            return "C";
        } else if (Byte.TYPE.equals(type)) {
            return "B";
        } else if (Short.TYPE.equals(type)) {
            return "S";
        } else if (Float.TYPE.equals(type)) {
            return "F";
        } else if (Long.TYPE.equals(type)) {
            return "J";
        } else if (Double.TYPE.equals(type)) {
            return "D";
        }
        return null;
    }

    public static Type getMethodType(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName, (Class<?>[]) new Class[0]);
            return method.getGenericReturnType();
        } catch (Exception ex) {
            return null;
        }
    }

    public static Type getFieldType(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getField(fieldName);
            return field.getGenericType();
        } catch (Exception ex) {
            return null;
        }
    }

    public static Class<?> getFieldGenericElementType(Field field) {
        ArrayList<Class<?>> genericElementTypes = getFieldGenericElementTypes(field);
        if (genericElementTypes != null && !genericElementTypes.isEmpty()) {
            return genericElementTypes.get(0);
        }
        return null;
    }

    public static ArrayList<Class<?>> getFieldGenericElementTypes(Field field) {
        Type genericType = field.getGenericType();

        ArrayList<Class<?>> genericClasses = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            for (Type t : actualTypeArguments) {
                if (t instanceof Class) {
                    if (genericClasses == null) {
                        genericClasses = new ArrayList<>();
                    }
                    genericClasses.add((Class<?>) t);
                }
            }
        }
        return genericClasses;
    }

}
