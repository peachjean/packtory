package net.peachjean.packtory.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import net.peachjean.packtory.spi.CompositionHandler;
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

	public void generate(final FactorySpec factorySpec, final JavaFileObject sourceFile) throws IOException
	{
		CompositionHandler compositionHandler = locateCompositionHandler(factorySpec);
		final JavaWriter javaWriter = new JavaWriter(sourceFile.openWriter());
		javaWriter.emitPackage(factorySpec.getPackageName());
		javaWriter.beginType(factorySpec.getFullyQualifiedFactoryName(), "class", FINAL_PUBLIC);
		javaWriter.emitEmptyLine();

		final List<TypeMirror> dependencies = compositionHandler.determineDependencies(factorySpec, processingEnvironment);
		writeConstructor(javaWriter, dependencies, compositionHandler);
		javaWriter.emitEmptyLine();
		writeCreateMethod(factorySpec.getEntryPoint(), "create", javaWriter);

		javaWriter.endType();
		javaWriter.close();
	}

	private void writeConstructor(final JavaWriter javaWriter, final List<TypeMirror> dependencies, final CompositionHandler compositionHandler) throws IOException
	{
		Map<TypeMirror, String> parameterNameMap = new LinkedHashMap<TypeMirror, String>();
		List<String> parameters = new ArrayList<String>();
		for(TypeMirror dep: dependencies)
		{
			final String paramName = dep.toString().substring(0, 1).toLowerCase() + dep.toString().substring(1);
			parameterNameMap.put(dep, paramName);
			parameters.add(dep.toString());
			parameters.add(paramName);
		}
		javaWriter.emitAnnotation(Inject.class);
		javaWriter.beginConstructor(PUBLIC, parameters, Collections.<String>emptyList());
		compositionHandler.writeConstructorBody(javaWriter, parameterNameMap);
		javaWriter.endConstructor();

	}

	private CompositionHandler locateCompositionHandler(final FactorySpec factorySpec)
	{
		//TODO: use a service loader!
		CompositionHandler handler = new SimpleCompositionHandler();
		assert handler.canComposeFactory(factorySpec, this.processingEnvironment);
		return handler;
	}

	private void writeCreateMethod(final TypeMirror entryPoint, final String methodName, final JavaWriter javaWriter) throws IOException
	{
		javaWriter.beginMethod(entryPoint.toString(), methodName, FINAL_PUBLIC);
		javaWriter.emitStatement("return null");
		javaWriter.endMethod();
	}
}
