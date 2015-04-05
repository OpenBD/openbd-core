package org.m0zilla.javascript.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks a Java method as JavaScript setter. This can
 * be used as an alternative to the <code>jsSet_</code> prefix desribed in
 * {@link org.m0zilla.javascript.ScriptableObject#defineClass(org.m0zilla.javascript.Scriptable, java.lang.Class)}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JSSetter {
    String value() default "";
}
