package org.jboss.marshalling.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(SerializableClass.class)
public final class SerializableClassSubstitution {

    @Substitute
    private static <T> T invokeConstructorNoException(Constructor<T> constructor, Object... args) {
        System.out.println(
                String.format("SerializableClassSubstitution.invokeConstructorNoException(%s, %s)", constructor, args));
        if (constructor == null) {
            throw new IllegalArgumentException("No matching constructor");
        }
        try {
            //System.out.println(String.format("Invoking constructor.newInstance(%s)", args));
            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            final Throwable te = e.getTargetException();
            if (te instanceof RuntimeException) {
                throw (RuntimeException) te;
            } else if (te instanceof Error) {
                throw (Error) te;
            } else {
                throw new IllegalStateException("Unexpected exception", te);
            }
        } catch (InstantiationException e) {
            System.out.println("An exception was thrown when attempting to invoke " + constructor + " exception: " + e);
            //return (T) new LinkedHashMap(); // Ugly hack to workaround the fact that graalvm seems not to be able to call java.util.AbstractMap() cctor via reflection
            throw new IllegalStateException("Instantiation failed unexpectedly");
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Constructor is unexpectedly inaccessible");
        }
    }
}
