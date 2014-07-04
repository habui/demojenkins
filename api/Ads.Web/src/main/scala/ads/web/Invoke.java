package ads.web;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Invoke {
    String Parameters() default "";
    boolean bypassFilter() default false;
}