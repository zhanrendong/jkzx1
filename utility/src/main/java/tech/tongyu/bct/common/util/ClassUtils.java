package tech.tongyu.bct.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ClassUtils {

    private static final String SLASH = "/";

    /**
     * 改变private/protected的成员变量为public，尽量不调用实际改动的语句，避免JDK的SecurityManager抱怨。
     */
    public static void makeAccessible(Field field) {
        if ((!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers()) || Modifier
                .isFinal(field.getModifiers())) && !field.isAccessible()) {
            field.setAccessible(true);
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.
     *
     * 如向上转型到Object仍无法找到, 返回null.
     */
    public static Optional<Field> getAccessibleField(final Object obj, final String fieldName) {
        Validate.notNull(obj, "object can't be null");
        Validate.notBlank(fieldName, "fieldName can't be blank");
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                makeAccessible(field);
                return Optional.of(field);
            } catch (NoSuchFieldException e) {//NOSONAR
                // Field不在当前类定义,继续向上转型
                continue;// new add
            }
        }
        return Optional.empty();
    }

    /**
     * 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
     */
    public static Optional getFieldValue(final Object obj, final String fieldName) {
        if(Objects.isNull(obj))
            return Optional.empty();

        return getAccessibleField(obj, fieldName)
                .map(field -> {
                    try {
                        return Optional.ofNullable(field.get(obj));
                    } catch (IllegalAccessException e) {
                        // do nothing...
                        return Optional.empty();
                    }
                }).orElseThrow(() -> new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + obj + "]"));
    }

    public static Optional<Method> getMethod(Class<?> cls, String methodName, Class<?>... paramTypes){
        if(Objects.isNull(cls)) throw new IllegalArgumentException("Class must not be null");
        if(StringUtils.isBlank(methodName)) throw new IllegalArgumentException("Method name must not be null");

        if (paramTypes != null) {
            try {
                return Optional.of(cls.getMethod(methodName, paramTypes));
            }
            catch (NoSuchMethodException ex) {
                return Optional.empty();
            }
        }
        else {
            Set<Method> candidates = new HashSet<Method>(1);
            Method[] methods = cls.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return Optional.of(candidates.iterator().next());
            }
            else if (candidates.isEmpty()) {
                return Optional.empty();
            }
            else {
                throw new IllegalStateException("Multiple methods found: " + cls.getName() + '.' + methodName);
            }
        }
    }

    public static <T> Optional<T> newInstance(Class<T> clazz){
        try{
            return Optional.of(clazz.newInstance());
        }catch (InstantiationException| IllegalAccessException e){
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<T> newInstance(Class<? extends T> clazz, Class<T> targetClass){
        Optional optional = newInstance(clazz);
        return optional.isPresent() ? Optional.of((T)(optional.get())) : optional;
    }

    public static String getPackageClassPathStr(Class clazz){
        return clazz.getPackage().getName().replaceAll("\\.",SLASH) + SLASH;
    }
}
