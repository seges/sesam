package sk.seges.sesam.pap.model.printer.entity;

/**
 * Created by PeterSimun on 14.12.2014.
 */
public enum EntityTargetType {
    DTO {
        @Override
        public String getProviderMethodName() {
            return "getDomainEntityForDto";
        }
    },
    DOMAIN {
        @Override
        public String getProviderMethodName() {
            return "getDtoClassForDomain";
        }
    }
    ;
    public abstract String getProviderMethodName();
}