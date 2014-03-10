package net.peachjean.packtory.internal;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import net.peachjean.packtory.spi.CompositionHandler;
import net.peachjean.packtory.spi.CompositionHandlerSource;
import net.peachjean.packtory.spi.FactorySpec;

import com.squareup.javawriter.JavaWriter;

/**
 * TODO: Document this class
 */
class FactorySourceGenerator
{
	private final ProcessingEnvironment processingEnvironment;

	private static final Set<Modifier> FINAL_PUBLIC = new HashSet<Modifier>(Arrays.asList(Modifier.PUBLIC, Modifier.FINAL));
	private static final Set<Modifier> PUBLIC = new HashSet<Modifier>(Arrays.asList(Modifier.PUBLIC));

	FactorySourceGenerator(final ProcessingEnvironment processingEnvironment)
	{
		this.processingEnvironment = processingEnvironment;
	}

	public void generate(final FactorySpec factorySpec, final JavaFileObject sourceFile) throws IOException, NoApplicationCompositionHandler
	{
		CompositionHandler compositionHandler = locateCompositionHandler(factorySpec);
		final JavaWriter javaWriter = new JavaWriter(sourceFile.openWriter());
		javaWriter.emitPackage(factorySpec.getPackageName());
		addGeneratedAnnotation(javaWriter);
		javaWriter.beginType(factorySpec.getFullyQualifiedFactoryName(), "class", FINAL_PUBLIC);
		javaWriter.emitEmptyLine();

		final Map<String, TypeMirror> dependencies = compositionHandler.getDependencies();
		compositionHandler.writeFields(javaWriter);
		javaWriter.emitEmptyLine();
		writeConstructor(javaWriter, dependencies, compositionHandler);
		javaWriter.emitEmptyLine();
		writeCreateMethod(factorySpec.getEntryPoint(), "create", javaWriter, compositionHandler);

		javaWriter.endType();
		javaWriter.close();
	}

	private JavaWriter addGeneratedAnnotation(final JavaWriter javaWriter) throws IOException
	{
		final Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("value", JavaWriter.stringLiteral("packtory"));
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
		parameters.put("date", JavaWriter.stringLiteral(df.format(new Date())));
		return javaWriter.emitAnnotation(Generated.class, parameters);
	}

	private void writeConstructor(final JavaWriter javaWriter, final Map<String, TypeMirror> dependencies, final CompositionHandler compositionHandler) throws IOException
	{
		List<String> parameters = new ArrayList<String>();
		for(Entry<String, TypeMirror> parameterEntry: dependencies.entrySet())
		{
			parameters.add(parameterEntry.getValue().toString());
			parameters.add(parameterEntry.getKey());
		}
		javaWriter.emitAnnotation(Inject.class);
		javaWriter.beginConstructor(PUBLIC, parameters, Collections.<String>emptyList());
		compositionHandler.writeConstructorBody(javaWriter);
		javaWriter.endConstructor();

	}

	private CompositionHandler locateCompositionHandler(final FactorySpec factorySpec) throws NoApplicationCompositionHandler
	{
		for(CompositionHandlerSource source: ServiceLoader.load(CompositionHandlerSource.class))
		{
			CompositionHandler handler = source.createHandlerIfCapable(factorySpec, this.processingEnvironment);
			if(handler != null)
			{
				return handler;
			}
		}
		throw new NoApplicationCompositionHandler(factorySpec);
	}

	private void writeCreateMethod(final TypeMirror entryPoint, final String methodName, final JavaWriter javaWriter, final CompositionHandler compositionHandler) throws IOException
	{
		javaWriter.beginMethod(entryPoint.toString(), methodName, FINAL_PUBLIC);
		compositionHandler.writeCreateMethodBody(javaWriter, entryPoint);
		javaWriter.endMethod();
	}
}
