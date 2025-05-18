package net.optifine.reflect;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;

// 狼牙这招太狠了反射你赢了
public class ReflectorField implements IResolvable {
    private IFieldLocator fieldLocator;
    private boolean checked;
    private Field targetField;
    private VarHandle targetVarHandle;

    public ReflectorField(ReflectorClass reflectorClass, String targetFieldName) {
        this(new FieldLocatorName(reflectorClass, targetFieldName));
    }

    public ReflectorField(ReflectorClass reflectorClass, Class targetFieldType) {
        this(reflectorClass, targetFieldType, 0);
    }

    public ReflectorField(ReflectorClass reflectorClass, Class targetFieldType, int targetFieldIndex) {
        this(new FieldLocatorType(reflectorClass, targetFieldType, targetFieldIndex));
    }

    public ReflectorField(Field field) {
        this(new FieldLocatorFixed(field));
    }

    public ReflectorField(IFieldLocator fieldLocator) {
        this.fieldLocator = fieldLocator;
        this.checked = false;
        this.targetField = null;
        this.targetVarHandle = null;
        ReflectorResolver.register(this);
    }

    public Field getTargetField() {
        if (this.checked) {
            return this.targetField;
        } else {
            this.checked = true;
            this.targetField = this.fieldLocator.getField();

            if (this.targetField != null) {
                this.targetField.setAccessible(true);
                try {
                    MethodHandles.Lookup lookup = MethodHandles.lookup();
                    this.targetVarHandle = lookup.unreflectVarHandle(this.targetField);
                } catch (Throwable t) {
                    // Fallback: use Field only
                }
            }

            return this.targetField;
        }
    }

    public VarHandle getTargetVarHandle() {
        if (!this.checked) this.getTargetField();
        return this.targetVarHandle;
    }

    public Object getValue() {
        return Reflector.getFieldValue(null, this);
    }

    public void setValue(Object value) {
        Reflector.setFieldValue(null, this, value);
    }

    public void setValue(Object obj, Object value) {
        Reflector.setFieldValue(obj, this, value);
    }

    public Object callDirectGet(Object instance) {
        VarHandle vh = getTargetVarHandle();
        if (vh != null) {
            try {
                return vh.get(instance);
            } catch (Throwable t) {
                return null;
            }
        }
        Field field = getTargetField();
        if (field != null) {
            try {
                return field.get(instance);
            } catch (Throwable t) {
                return null;
            }
        }
        return null;
    }

    public void callDirectSet(Object instance, Object value) {
        VarHandle vh = getTargetVarHandle();
        if (vh != null) {
            try {
                vh.set(instance, value);
                return;
            } catch (Throwable t) {
                // Fallback
            }
        }
        Field field = getTargetField();
        if (field != null) {
            try {
                field.set(instance, value);
            } catch (Throwable ignored) {}
        }
    }

    public boolean exists() {
        return this.getTargetField() != null;
    }

    public void resolve() {
        this.getTargetField();
    }
}