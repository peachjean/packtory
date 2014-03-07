package net.peachjean.packtory;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

/**
 * TODO: Document this class
 */
@Retention(RUNTIME)
@Target(PACKAGE)
@Inherited
public @interface Factory
{
	Class<?> entryPoint();
	Class<?>[] composition() default {};
	String factoryName() default "Factory";
}
