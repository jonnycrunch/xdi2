package xdi2.messaging.target.contributor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ContributorMount {

	String[] contributorXris() default { };
	String[] operationXris() default { };
	String[] contextNodeArcXris() default { };
	String[] relationArcXris() default { };
	String[] targetContextNodeXris() default { };
}
