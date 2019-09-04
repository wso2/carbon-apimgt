package org.wso2.carbon.apimgt.rest.api.util.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
//@Target(ElementType.FIELD)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Scopes {
    Scope[] value();
}


