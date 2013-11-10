package sk.seges.sesam.core.pap.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;

import sk.seges.sesam.core.pap.model.mutable.api.MutableDeclaredType;
import sk.seges.sesam.core.pap.model.mutable.utils.MutableProcessingEnvironment;
import sk.seges.sesam.core.pap.test.manager.CompilerManager;
import sk.seges.sesam.core.pap.test.manager.EclipseCompilerManager;
import sk.seges.sesam.core.pap.test.manager.JavacCompilerManager;
import sk.seges.sesam.core.pap.test.model.utils.TestProcessingEnvironment;
import sk.seges.sesam.core.pap.test.model.utils.TestRoundEnvironment;
import sk.seges.sesam.core.pap.utils.ClassFinder;

public abstract class AnnotationTest {

	protected static final String DIRECTORY_SEPARATOR = "\\";
	private static final String TEST_SOURCE_FOLDER = "src" + DIRECTORY_SEPARATOR + "test" + DIRECTORY_SEPARATOR + "java";
	private static final String MAIN_SOURCE_FOLDER = "src" + DIRECTORY_SEPARATOR + "main" + DIRECTORY_SEPARATOR + "java";
	protected static final String SOURCE_FILE_SUFFIX = ".java";
	protected static final String OUTPUT_FILE_SUFFIX = ".output";
	protected static final String OUTPUT_ECLIPSE_FILE_SUFFIX = "_eclipse.output";
	protected static final String OUTPUT_DIRECTORY = "target" + DIRECTORY_SEPARATOR + "generated-test";

	public enum Compiler {
		JAVAC {
			@Override
			public CompilerManager getCompiler() {
				return new JavacCompilerManager();
			}
		}, 
		ECLIPSE {
			@Override
			public CompilerManager getCompiler() {
				return new EclipseCompilerManager();
			}
		};
		
		public abstract CompilerManager getCompiler();
	}
	
	protected enum CompilerOptions {
		GENERATED_SOURCES_DIRECTORY("-s <directory>", "<directory>", "Specify where to place generated source files"), 
		GENERATED_CLASSES_DIRECTORY("-d <directory>", "<directory>", "Specify where to place generated class files"), ;

		private String option;
		private String description;
		private String parameter;

		CompilerOptions(String option, String parameter, String description) {
			this.option = option;
			this.parameter = parameter;
			this.description = description;
		}

		public String getOption() {
			return option;
		}

		public String[] getOption(String parameterValue) {
			if (parameter != null) {

				String[] result = new String[2];

				int index = option.indexOf(parameter);
				result[0] = option.substring(0, index).trim();
				result[1] = parameterValue;
				return result;
			}

			return new String[] { getOption() };
		}

		public String getDescription() {
			return description;
		}
	}

	protected MutableDeclaredType toMutable(Class<?> clazz) {
		return (MutableDeclaredType) processingEnv.getTypeUtils().toMutableType(processingEnv.getElementUtils().getTypeElement(clazz.getName()).asType());
	}
	
	protected MutableProcessingEnvironment processingEnv = new MutableProcessingEnvironment(new TestProcessingEnvironment(), getProcessors()[0].getClass(), new ArrayList<MutableDeclaredType>()) {
		@Override
		public Map<String, String> getOptions() {
			return getProcessorOptions();
		}
	};

	protected TestRoundEnvironment roundEnv = new TestRoundEnvironment(processingEnv);

	/**
	 * @return the processor instances that should be tested
	 */
	protected abstract Processor[] getProcessors();

	protected String getTestSourceFolder() {
		return TEST_SOURCE_FOLDER;
	}

	protected String getMainSourceFolder() {
		return MAIN_SOURCE_FOLDER;
	}

	protected String[] getCompilerOptions() {
		return CompilerOptions.GENERATED_SOURCES_DIRECTORY.getOption(ensureOutputDirectory().getAbsolutePath());
	}

	protected String getResource(String name) {
		//getClass().getResource
		return Thread.currentThread().getContextClassLoader().getResource(name).getFile().replaceAll("%5c", getDirectorySeparatorPattern());
	}

	private String getDirectorySeparatorPattern() {
		return DIRECTORY_SEPARATOR.equals("\\") ? "\\\\" : DIRECTORY_SEPARATOR;
	}

	protected String getWorkingDirectory() {
		String relativeClassPath = toPath(getClass().getPackage());
		String targetClassesDirectory = getResource(relativeClassPath + DIRECTORY_SEPARATOR + getClass().getSimpleName().toString() + ".class").replaceAll(relativeClassPath.replaceAll("\\\\", "\\\\\\\\"), "");
		return targetClassesDirectory.substring(0, targetClassesDirectory.indexOf("target"));
	}

	protected String toPath(Package packageName) {
		return toPath(packageName.getName());
	}

	protected String toPath(String packageName) {
		return packageName.replaceAll("\\.", getDirectorySeparatorPattern());
	}

	protected File getEclipseResourceFile(Class<?> clazz) {
		return getEclipseResourceFile(null, clazz);
	}
	
	protected File getEclipseResourceFile(String directorySuffix, Class<?> clazz) {
		String resource = getResource(
				DIRECTORY_SEPARATOR + toPath(clazz.getPackage()) + DIRECTORY_SEPARATOR + (directorySuffix != null ? (directorySuffix + DIRECTORY_SEPARATOR) : "") + clazz.getSimpleName() + OUTPUT_ECLIPSE_FILE_SUFFIX);
		
		if (resource == null) {
			throw new RuntimeException("Unable to find output file " +
					DIRECTORY_SEPARATOR + toPath(clazz.getPackage()) + DIRECTORY_SEPARATOR + (directorySuffix != null ? (directorySuffix + DIRECTORY_SEPARATOR) : "") + clazz.getSimpleName() + OUTPUT_ECLIPSE_FILE_SUFFIX);
		}

		return new File(resource);
	}

	protected File getResourceFile(Class<?> clazz) {
		return getResourceFile(null, clazz);
	}
	
	protected File getResourceFile(String directorySuffix, Class<?> clazz) {

		String resource = getResource(
				DIRECTORY_SEPARATOR + toPath(clazz.getPackage()) + DIRECTORY_SEPARATOR + (directorySuffix != null ? (directorySuffix + DIRECTORY_SEPARATOR) : "") + clazz.getSimpleName() + OUTPUT_FILE_SUFFIX);
		
		if (resource == null) {
			throw new RuntimeException("Unable to find output file " +
					DIRECTORY_SEPARATOR + toPath(clazz.getPackage()) + DIRECTORY_SEPARATOR + (directorySuffix != null ? (directorySuffix + DIRECTORY_SEPARATOR) : "") + clazz.getSimpleName() + OUTPUT_FILE_SUFFIX );
		}

		return new File(resource);
	}

	protected File ensureOutputDirectory() {
		File file = new File(OUTPUT_DIRECTORY);
		if (!file.exists()) {
			file.mkdirs();
		}

		return file;
	}

	protected File toFile(MutableDeclaredType type) {
		return new File(OUTPUT_DIRECTORY, toPath(type.getPackageName()) + DIRECTORY_SEPARATOR + type.getSimpleName() + SOURCE_FILE_SUFFIX);
	}

	protected static void assertOutput(File expectedResult, File output) {
		String[] expectedContent = getContents(expectedResult);
		String[] outputContent = getContents(output);
		assertEquals(expectedContent.length, outputContent.length);

		for (int i = 0; i < expectedContent.length; i++) {
			assertEquals(expectedContent[i].trim(), outputContent[i].trim());
		}
	}

	private static String[] getContents(File file) {
		List<String> content = new ArrayList<String>();

		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			try {
				String line = null; // not declared within while loop
				while ((line = input.readLine()) != null) {
					content.add(line);
				}
			} finally {
				input.close();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}

		return content.toArray(new String[] {});
	}

	protected List<Diagnostic<? extends JavaFileObject>> compileFiles(Compiler environmentOptions, Type... compilationUnits) {
		assert (compilationUnits != null);

		List<File> files = new ArrayList<File>();

		addCollection(files, compilationUnits);

		Set<Class<?>> classes = new HashSet<Class<?>>();
		for (Type type: compilationUnits) {
			if (type instanceof Class<?>) {
				classes.add((Class<?>)type);
			}
		}
		
		roundEnv.setRootElements(classes);

		return compileFiles(environmentOptions, files);
	}
	
	/**
	 * Attempts to compile the given compilation units using the Java Compiler API.
	 * <p>
	 * The compilation units and all their dependencies are expected to be on the classpath.
	 * 
	 * @param compilationUnits
	 *            the classes to compile
	 * @return the {@link Diagnostic diagnostics} returned by the compilation, as demonstrated in the documentation for
	 *         {@link JavaCompiler}
	 */
	protected List<Diagnostic<? extends JavaFileObject>> compileFiles(Type... compilationUnits) {
		return compileFiles(Compiler.JAVAC, compilationUnits);
	}

	protected List<Diagnostic<? extends JavaFileObject>> compileFiles(Package... compilationUnits) {
		return compileFiles(Compiler.JAVAC, compilationUnits);
	}
	
	protected List<Diagnostic<? extends JavaFileObject>> compileFiles(Compiler environmentOptions, Package... compilationUnits) {
		assert (compilationUnits != null);

		List<File> files = new ArrayList<File>();

		addCollection(files, compilationUnits);

		return compileFiles(environmentOptions, files);
	}

	private void addCollection(List<File> files, Package[] compilationUnits) {
		if (compilationUnits == null) {
			return;
		}
		for (Package element: compilationUnits) {
			assert (element != null);

			ClassFinder classFinder = new ClassFinder();
			addCollection(files, classFinder.findClassesInPackage(((Package)element).getName()));
		}
	}

	protected <T extends AnnotatedElement> void addCollection(List<File> files, Collection<T> compilationUnits) {
		if (compilationUnits == null) {
			return;
		}
		addCollection(files, compilationUnits.toArray(new Type[] {}));
	}

	protected <T extends Type> void addCollection(List<File> files, T... compilationUnits) {

		File currentDirectory = new File(getWorkingDirectory());

		if (compilationUnits == null) {
			return;
		}
		for (T element : compilationUnits) {
			assert (element != null);

			if (element instanceof Class<?>) {
				File file = toFile(currentDirectory, ((Class<?>) element));
				if (file != null) {
					files.add(file);
				} else {
					// These are innerclasses, etc ... that should not be defined in this way
				}
			} else if (element instanceof Package) {
				ClassFinder classFinder = new ClassFinder();
				addCollection(files, classFinder.findClassesInPackage(((Package) element).getName()));
			}
		}
	}

	private String convertClassNameToResourcePath(String name) {
		return name.replace(".", DIRECTORY_SEPARATOR);
	}

	private File toFile(File currentDirectory, Class<?> clazz) {

		File file = new File(currentDirectory, getTestSourceFolder() + DIRECTORY_SEPARATOR
				+ convertClassNameToResourcePath(clazz.getCanonicalName()) + SOURCE_FILE_SUFFIX);
		if (!file.exists()) {
			file = new File(currentDirectory, getMainSourceFolder() + DIRECTORY_SEPARATOR
					+ convertClassNameToResourcePath(clazz.getCanonicalName()) + SOURCE_FILE_SUFFIX);
			if (!file.exists()) {
				if (clazz.getEnclosingClass() != null) {
					return toFile(currentDirectory, clazz.getEnclosingClass());
				}
				return null;
			}
		}
		return file;
	}

	private List<String> mergeCompilerOptions(List<String> options) {

		if (options == null) {
			return Arrays.asList(getCompilerOptions());
		}
		List<String> result = new ArrayList<String>();

		for (String option : options) {
			result.add(option);
		}

		for (String option : getCompilerOptions()) {
			result.add(option);
		}

		return result;
	}

	public static String getClassPath() {
		String classPath = System.getProperty("maven.test.class.path");
		if (classPath == null || classPath.length() == 0) {
			return System.getProperty("java.class.path");
		}

		classPath = classPath.replaceAll(", ", isWindows() ? ";" : ":").trim();
		return "\"" + classPath.substring(1, classPath.length() - 2).trim() + ";"
				+ new File("target" + DIRECTORY_SEPARATOR + "classes").getAbsolutePath() + "\"";
	}

	public static String getOsName() {
      return System.getProperty("os.name");
	}

    public static boolean isWindows() {
		return getOsName().startsWith("Windows");
	}

	private List<String> getCompilerInternalOptions() {
		List<String> options = new ArrayList<String>();
		options.add("-proc:only");
		options.add("-classpath");
		options.add(getClassPath());

		for (Map.Entry<String, String> option: getProcessorOptions().entrySet()) {
			options.add("-A" + option.getKey() + "=" + option.getValue());
		}

		return mergeCompilerOptions(options);
	}

	protected Map<String, String> getProcessorOptions() {
		Map<String, String> options = new HashMap<String, String>();
		options.put("classpath", getClassPath());
		options.put("projectName", getClass().getSimpleName());
		return options;
	}

	protected List<Diagnostic<? extends JavaFileObject>> compileFiles(Collection<File> compilationUnits) {
		return compileFiles(Compiler.JAVAC, compilationUnits);
	}
	
	protected List<Diagnostic<? extends JavaFileObject>> compileFiles(Compiler options, Collection<File> compilationUnits) {
		CompilerManager compiler = options.getCompiler();
		compiler.setProcessors(Arrays.asList(getProcessors()));
		compiler.setOptions(getCompilerInternalOptions());
		return compiler.compile(compilationUnits);
	}

	protected static void assertCompilationSuccessful(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		assert (diagnostics != null);

		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
			System.out.println(diagnostic.toString());
		}

		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
			assertFalse("Expected no errors", diagnostic.getKind().equals(Kind.ERROR));
		}

	}

	/**
	 * Asserts that the compilation produced results of the following {@link Kind Kinds} at the given line numbers,
	 * where the <em>n</em>th kind is expected at the <em>n</em>th line number.
	 * <p>
	 * Does not check that these is the <em>only</em> diagnostic kinds returned!
	 * 
	 * @param expectedDiagnosticKinds
	 *            the kinds of diagnostic expected
	 * @param expectedLineNumbers
	 *            the line numbers at which the diagnostics are expected
	 * @param diagnostics
	 *            the result of the compilation
	 * @see #assertCompilationSuccessful(List)
	 * @see #assertCompilationReturned(Kind, long, List)
	 */
	protected static void assertCompilationReturned(Kind[] expectedDiagnosticKinds, long[] expectedLineNumbers,
			List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		assert ((expectedDiagnosticKinds != null) && (expectedLineNumbers != null) && (expectedDiagnosticKinds.length == expectedLineNumbers.length));

		for (int i = 0; i < expectedDiagnosticKinds.length; i++) {
			assertCompilationReturned(expectedDiagnosticKinds[i], expectedLineNumbers[i], diagnostics);
		}

	}

	/**
	 * Asserts that the compilation produced a result of the following {@link Kind} at the given line number.
	 * <p>
	 * Does not check that this is the <em>only</em> diagnostic kind returned!
	 * 
	 * @param expectedDiagnosticKind
	 *            the kind of diagnostic expected
	 * @param expectedLineNumber
	 *            the line number at which the diagnostic is expected
	 * @param diagnostics
	 *            the result of the compilation
	 * @see #assertCompilationSuccessful(List)
	 * @see #assertCompilationReturned(Kind[], long[], List)
	 */
	protected static void assertCompilationReturned(Kind expectedDiagnosticKind, long expectedLineNumber,
			List<Diagnostic<? extends JavaFileObject>> diagnostics) {
		assert ((expectedDiagnosticKind != null) && (diagnostics != null));
		boolean expectedDiagnosticFound = false;

		for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {

			if (diagnostic.getKind().equals(expectedDiagnosticKind)
					&& (diagnostic.getLineNumber() == expectedLineNumber)) {
				expectedDiagnosticFound = true;
			}
		}

		assertTrue("Expected a result of kind " + expectedDiagnosticKind + " at line " + expectedLineNumber,
				expectedDiagnosticFound);
	}
}