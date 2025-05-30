package com.potato.apicompiler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
public @interface ApiLog {
    String nameEnglish();

    String nameChinese() default "";
}
