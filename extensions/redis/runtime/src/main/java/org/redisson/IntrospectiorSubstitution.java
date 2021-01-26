package org.redisson;

import java.lang.annotation.Annotation;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import org.redisson.liveobject.misc.Introspectior;

@TargetClass(Introspectior.class)
@Substitute
public final class IntrospectiorSubstitution {

    @Substitute
    public static FieldList<FieldDescription.InDefinedShape> getFieldsWithAnnotation(Class<?> c,
            Class<? extends Annotation> a) {
        throw new UnsupportedOperationException(
                "Introspectior.getFieldsWithAnnotation(Class<?>, Class<? extends Annotation>) is not supported in native mode");
    }
}
