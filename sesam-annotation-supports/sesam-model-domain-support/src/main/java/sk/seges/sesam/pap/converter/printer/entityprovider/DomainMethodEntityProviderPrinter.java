package sk.seges.sesam.pap.converter.printer.entityprovider;

import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;

/**
 * Created by PeterSimun on 14.12.2014.
 */
public class DomainMethodEntityProviderPrinter extends AbstractDomainMethodEntityProviderPrinter {

    public DomainMethodEntityProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
        super(processingEnv, pw);
    }
}