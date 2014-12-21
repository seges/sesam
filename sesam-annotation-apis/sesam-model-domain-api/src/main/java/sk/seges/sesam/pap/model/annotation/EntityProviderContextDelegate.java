package sk.seges.sesam.pap.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by PeterSimun on 16.12.2014.
 */
@Target(ElementType.TYPE)
public @interface EntityProviderContextDelegate {
    EntityProviderContext value();
}
