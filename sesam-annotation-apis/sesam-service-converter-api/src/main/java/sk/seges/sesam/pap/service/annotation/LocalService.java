package sk.seges.sesam.pap.service.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
public @interface LocalService {
	
	public @interface SecurityProvider {
		
		Class<?> entityClass();
		
		Class<?> securityProvider();
		
	}
	
	Class<?> localInterace() default Void.class;

	SecurityProvider[] securityProviders() default {};
}