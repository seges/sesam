package sk.seges.sesam.core.pap.builder;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import sk.seges.sesam.core.pap.builder.api.ClassPathSources;
import sk.seges.sesam.core.pap.model.SourceFile;
import sk.seges.sesam.core.pap.model.api.Source;
import sk.seges.sesam.pap.model.TypeElementsList;

public class ClassPathSourceUtils extends ClassPathFinder implements ClassPathSources {

	public class SourceFileTypeHandler implements FileTypeHandler {

		private final String canonicalName;
		
		public SourceFileTypeHandler(String canonicalName) {
			this.canonicalName = canonicalName;
		}
		
		@Override
		public boolean isFileOfType(String canonicalName) {
			return this.canonicalName.equals(canonicalName);
		}

		@Override
		public String getExtension() {
			return ".java";
		}

		@Override
		public void handleFile(InputStreamProvider inputStreamProvider, String canonicalName, ProcessingEnvironment processingEnv, Map<String, TypeElementsList> annotatedClasses) {
			inputStream = inputStreamProvider.getInputStream();
		}

		@Override
		public boolean continueProcessing() {
			return false;
		}		
	}

	private InputStream inputStream;
	
	public ClassPathSourceUtils(ProcessingEnvironment processingEnv, String packageName) {
		super(processingEnv, packageName);
	}

	@Override
	public Source getElementSourceFile(TypeElement a) {

		inputStream = null;
		
		//TODO convert to canonical name some day
		String qualifiedName = a.getQualifiedName().toString();

		packageName = processingEnv.getElementUtils().getPackageOf(a).toString();
		getSubclasses(System.getProperty(PROCESSOR_CLASS_PATH), new SourceFileTypeHandler(qualifiedName));

		if (inputStream != null) {
			return new SourceFile(inputStream);
		}
		
		return null;
	}
}