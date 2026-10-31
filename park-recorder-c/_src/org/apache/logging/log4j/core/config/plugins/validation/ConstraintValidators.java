/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import org.apache.logging.log4j.core.config.plugins.validation.Constraint;
import org.apache.logging.log4j.core.config.plugins.validation.ConstraintValidator;
import org.apache.logging.log4j.core.util.ReflectionUtil;

public final class ConstraintValidators {
    private ConstraintValidators() {
    }

    public static Collection<ConstraintValidator<?>> findValidators(Annotation ... annotationArray) {
        ArrayList arrayList = new ArrayList();
        for (Annotation annotation : annotationArray) {
            ConstraintValidator<? extends Annotation> constraintValidator;
            Class<? extends Annotation> clazz = annotation.annotationType();
            if (!clazz.isAnnotationPresent(Constraint.class) || (constraintValidator = ConstraintValidators.getValidator(annotation, clazz)) == null) continue;
            arrayList.add(constraintValidator);
        }
        return arrayList;
    }

    private static <A extends Annotation> ConstraintValidator<A> getValidator(A a2, Class<? extends A> object) {
        Class<? extends ConstraintValidator<? extends Annotation>> clazz = ((Class)object).getAnnotation(Constraint.class).value();
        if (object.equals(ConstraintValidators.getConstraintValidatorAnnotationType(clazz))) {
            object = ReflectionUtil.instantiate(clazz);
            object.initialize(a2);
            return object;
        }
        return null;
    }

    private static Type getConstraintValidatorAnnotationType(Class<? extends ConstraintValidator<?>> typeArray) {
        for (Type type : typeArray.getGenericInterfaces()) {
            if (!(type instanceof ParameterizedType) || !ConstraintValidator.class.equals((Object)(type = (ParameterizedType)type).getRawType())) continue;
            return type.getActualTypeArguments()[0];
        }
        return Void.TYPE;
    }
}

