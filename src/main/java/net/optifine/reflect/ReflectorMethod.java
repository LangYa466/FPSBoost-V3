package net.optifine.reflect;

import net.optifine.Log;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

// 狼牙这招太狠了反射你赢了
public class ReflectorMethod implements IResolvable {
    private ReflectorClass reflectorClass;
    private String targetMethodName;
    private Class[] targetMethodParameterTypes;
    private boolean checked;
    private Method targetMethod;
    private MethodHandle targetMethodHandle;

    public ReflectorMethod(ReflectorClass reflectorClass, String targetMethodName) {
        this(reflectorClass, targetMethodName, null);
    }

    public ReflectorMethod(ReflectorClass reflectorClass, String targetMethodName, Class[] targetMethodParameterTypes) {
        this.reflectorClass = null;
        this.targetMethodName = null;
        this.targetMethodParameterTypes = null;
        this.checked = false;
        this.targetMethod = null;
        this.targetMethodHandle = null;
        this.reflectorClass = reflectorClass;
        this.targetMethodName = targetMethodName;
        this.targetMethodParameterTypes = targetMethodParameterTypes;
        ReflectorResolver.register(this);
    }

    public Method getTargetMethod() {
        if (this.checked) {
            return this.targetMethod;
        } else {
            this.checked = true;
            Class oclass = this.reflectorClass.getTargetClass();

            if (oclass == null) {
                return null;
            } else {
                try {
                    if (this.targetMethodParameterTypes == null) {
                        Method[] amethod = getMethods(oclass, this.targetMethodName);

                        if (amethod.length <= 0) {
                            return null;
                        }

                        if (amethod.length > 1) {
                            for (int i = 0; i < amethod.length; ++i) {
                                Method method = amethod[i];
                                Log.warn("(Reflector)  - " + method);
                            }
                            return null;
                        }

                        this.targetMethod = amethod[0];
                    } else {
                        this.targetMethod = getMethod(oclass, this.targetMethodName, this.targetMethodParameterTypes);
                    }

                    if (this.targetMethod == null) {
                        return null;
                    } else {
                        this.targetMethod.setAccessible(true);
                        return this.targetMethod;
                    }
                } catch (Throwable throwable) {
                    return null;
                }
            }
        }
    }

    public MethodHandle getTargetMethodHandle() {
        if (this.checked) {
            return this.targetMethodHandle;
        } else {
            this.checked = true;
            Class<?> targetClass = this.reflectorClass.getTargetClass();

            if (targetClass == null) {
                return null;
            } else {
                try {
                    MethodType methodType;
                    if (this.targetMethodParameterTypes == null) {
                        methodType = MethodType.methodType(void.class);
                    } else {
                        methodType = MethodType.methodType(Object.class, this.targetMethodParameterTypes);
                    }

                    MethodHandles.Lookup lookup = MethodHandles.lookup();
                    this.targetMethodHandle = lookup.findVirtual(targetClass, this.targetMethodName, methodType);

                    return this.targetMethodHandle;
                } catch (Throwable t) {
                    Log.warn("(Reflector) ASM method handle error: " + t);
                    return null;
                }
            }
        }
    }

    public boolean exists() {
        return this.checked ? (this.targetMethod != null || this.targetMethodHandle != null)
                : (this.getTargetMethod() != null || this.getTargetMethodHandle() != null);
    }

    public Class getReturnType() {
        Method method = this.getTargetMethod();
        if (method != null) {
            return method.getReturnType();
        }
        MethodHandle mh = this.getTargetMethodHandle();
        return mh == null ? null : mh.type().returnType();
    }

    public void deactivate() {
        this.checked = true;
        this.targetMethod = null;
        this.targetMethodHandle = null;
    }

    public Object call(Object... params) {
        return Reflector.call(this, params);
    }

    public boolean callBoolean(Object... params) {
        return Reflector.callBoolean(this, params);
    }

    public int callInt(Object... params) {
        return Reflector.callInt(this, params);
    }

    public float callFloat(Object... params) {
        return Reflector.callFloat(this, params);
    }

    public double callDouble(Object... params) {
        return Reflector.callDouble(this, params);
    }

    public String callString(Object... params) {
        return Reflector.callString(this, params);
    }

    public Object call(Object param) {
        return Reflector.call(this, param);
    }

    public boolean callBoolean(Object param) {
        return Reflector.callBoolean(this, param);
    }

    public int callInt(Object param) {
        return Reflector.callInt(this, param);
    }

    public float callFloat(Object param) {
        return Reflector.callFloat(this, param);
    }

    public double callDouble(Object param) {
        return Reflector.callDouble(this, param);
    }

    public String callString1(Object param) {
        return Reflector.callString(this, param);
    }

    public void callVoid(Object... params) {
        Reflector.callVoid(this, params);
    }

    public Object callDirect(Object instance, Object... params) {
        try {
            return getTargetMethodHandle().invokeWithArguments(prepend(instance, params));
        } catch (Throwable t) {
            Log.warn("(Reflector) Direct call error: " + t);
            return null;
        }
    }

    public boolean callDirectBoolean(Object instance, Object... params) {
        Object result = callDirect(instance, params);
        return result instanceof Boolean ? (Boolean) result : false;
    }

    public int callDirectInt(Object instance, Object... params) {
        Object result = callDirect(instance, params);
        return result instanceof Integer ? (Integer) result : 0;
    }

    public float callDirectFloat(Object instance, Object... params) {
        Object result = callDirect(instance, params);
        return result instanceof Float ? (Float) result : 0.0f;
    }

    public double callDirectDouble(Object instance, Object... params) {
        Object result = callDirect(instance, params);
        return result instanceof Double ? (Double) result : 0.0;
    }

    public String callDirectString(Object instance, Object... params) {
        Object result = callDirect(instance, params);
        return result != null ? result.toString() : null;
    }

    public void callDirectVoid(Object instance, Object... params) {
        callDirect(instance, params);
    }

    private Object[] prepend(Object first, Object[] rest) {
        Object[] result = new Object[rest.length + 1];
        result[0] = first;
        System.arraycopy(rest, 0, result, 1, rest.length);
        return result;
    }

    public static Method getMethod(Class cls, String methodName, Class[] paramTypes) {
        Method[] amethod = cls.getDeclaredMethods();

        for (int i = 0; i < amethod.length; ++i) {
            Method method = amethod[i];

            if (method.getName().equals(methodName)) {
                Class[] aclass = method.getParameterTypes();

                if (Reflector.matchesTypes(paramTypes, aclass)) {
                    return method;
                }
            }
        }

        return null;
    }

    public static Method[] getMethods(Class cls, String methodName) {
        List list = new ArrayList();
        Method[] amethod = cls.getDeclaredMethods();

        for (int i = 0; i < amethod.length; ++i) {
            Method method = amethod[i];

            if (method.getName().equals(methodName)) {
                list.add(method);
            }
        }

        Method[] amethod1 = (Method[]) list.toArray(new Method[list.size()]);
        return amethod1;
    }

    public void resolve() {
        this.getTargetMethod();
        this.getTargetMethodHandle();
    }
}