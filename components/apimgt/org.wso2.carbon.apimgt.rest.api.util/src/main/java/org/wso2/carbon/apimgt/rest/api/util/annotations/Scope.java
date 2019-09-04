package org.wso2.carbon.apimgt.rest.api.util.annotations;

import java.lang.annotation.*;


/**
 *

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Repeatable(Scopes.class)
public @interface Scope {
    String name();
    String description();
    //String value();
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Scopes {
    Scope[] value();
}

 */

@Repeatable(Scopes.class)
@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.FIELD)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Scope {
    String description();
    String value();
    String name();
}