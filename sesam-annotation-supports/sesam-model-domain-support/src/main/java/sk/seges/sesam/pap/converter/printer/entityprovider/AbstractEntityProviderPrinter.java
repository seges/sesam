package sk.seges.sesam.pap.converter.printer.entityprovider;

import sk.seges.sesam.core.pap.writer.FormattedPrintWriter;
import sk.seges.sesam.pap.model.model.TransferObjectProcessingEnvironment;
import sk.seges.sesam.pap.model.printer.base.ProviderPrinter;

/**
 * Created by PeterSimun on 14.12.2014.
 */
public abstract class AbstractEntityProviderPrinter extends ProviderPrinter {

    protected AbstractEntityProviderPrinter(TransferObjectProcessingEnvironment processingEnv, FormattedPrintWriter pw) {
        super(processingEnv, pw);
    }


}
