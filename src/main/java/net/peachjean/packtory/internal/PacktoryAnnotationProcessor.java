package net.peachjean.packtory.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import net.peachjean.packtory.Factory;
import net.peachjean.packtory.spi.FactorySpec;

import org.kohsuke.MetaInfServices;

/**
 * TODO: Document this class
 */
@MetaInfServices(Processor.class)
public class PacktoryAnnotationProcessor extends AbstractProcessor
{
	private final ErrorReporter errorReporter = new ErrorReporter();
	private FactorySourceGenerator sourceGenerator;

	@Override
	public synchronized void init(final ProcessingEnvironment processingEnv)
	{
		super.init(processingEnv);
		this.sourceGenerator = new FactorySourceGenerator(processingEnv);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes()
	{
		return Collections.singleton(Factory.class.getName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion()
	{
		return SourceVersion.latest();
	}

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv)
	{
		for (TypeElement annotationType : annotations)
		{
			for (Element factoryPackage : roundEnv.getElementsAnnotatedWith(annotationType))
			{
				if (factoryPackage.getKind() != ElementKind.PACKAGE)
				{
					errorReporter.reportAnnotatedElementNotPackage(factoryPackage);
				}
				else
				{
					handlePackage((PackageElement) factoryPackage, annotationType);
				}
			}
		}
		return true;
	}

	private void handlePackage(final PackageElement factoryPackage, final TypeElement annotationType)
	{
		final AnnotationMirror factoryAnnotation = locateAnnotationMirror(factoryPackage, annotationType);
		if (factoryAnnotation == null)
		{
			// short circuit, if this is null then an error was reported and the compiler will handle failing
			return;
		}
		final FactorySpec factorySpec = buildFactorySpec(factoryPackage.getQualifiedName().toString(), factoryAnnotation);
		try
		{
			final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(factorySpec.getFullyQualifiedFactoryName(), factoryPackage);
			sourceGenerator.generate(factorySpec, sourceFile);
		}
		catch (IOException e)
		{
			errorReporter.reportFailureWritingSource(e, factorySpec.getFullyQualifiedFactoryName(), factoryPackage);
			return;
		}
	}

	private FactorySpec buildFactorySpec(final String packageName, final AnnotationMirror factoryAnnotation)
	{
		TypeMirror entryPoint = null;
		List<TypeMirror> compositions = new ArrayList<TypeMirror>();
		String factoryName = "Factory";
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : processingEnv.getElementUtils().getElementValuesWithDefaults(factoryAnnotation).entrySet())
		{
			final String name = entry.getKey().getSimpleName().toString();
			if ("entryPoint".equals(name))
			{
				entryPoint = (TypeMirror) entry.getValue().getValue();
			}
			else if ("composition".equals(name))
			{
				final List<AnnotationValue> values = (List<AnnotationValue>) entry.getValue().getValue();
				for (AnnotationValue value : values)
				{
					compositions.add((TypeMirror) value.getValue());
				}
			}
			else if ("factoryName".equals(name))
			{
				factoryName = (String) entry.getValue().getValue();
			}
		}
		return new FactorySpec(packageName, factoryName, entryPoint, compositions);
	}

	private AnnotationMirror locateAnnotationMirror(final PackageElement factoryPackage, final TypeElement annotationType)
	{
		for (AnnotationMirror annotationMirror : factoryPackage.getAnnotationMirrors())
		{
			if (annotationMirror.getAnnotationType().equals(annotationType.asType()))
			{
				return annotationMirror;
			}
		}
		errorReporter.reportAnnotationNotFound(factoryPackage, annotationType);
		return null;
	}

	private class ErrorReporter
	{
		void reportAnnotatedElementNotPackage(Element element)
		{
			processingEnv.getMessager().printMessage(Kind.ERROR, "Only packages may be annotated with @Factory.", element);
		}

		public void reportAnnotationNotFound(final PackageElement factoryPackage, final TypeElement annotationType)
		{
			processingEnv.getMessager().printMessage(Kind.ERROR, "Annotation " + annotationType.getQualifiedName() + " not found on package.", factoryPackage);
		}

		public void reportFailureWritingSource(final IOException e, final CharSequence factoryClassName, final PackageElement factoryPackage)
		{
			processingEnv.getMessager()
			             .printMessage(Kind.ERROR, "Failed to write new factory source, " + factoryClassName + " due to IOException: " + e.getMessage(), factoryPackage);
		}
	}
}
