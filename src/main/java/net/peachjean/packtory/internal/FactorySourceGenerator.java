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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Generated;
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
		addGeneratedAnnotation(javaWriter);
		javaWriter.beginType(factorySpec.getFullyQualifiedFactoryName(), "class", FINAL_PUBLIC);
		javaWriter.emitEmptyLine();

		final List<TypeMirror> dependencies = compositionHandler.determineDependencies(factorySpec, processingEnvironment);
		compositionHandler.writeFields(javaWriter, getDependencyParameterMap(dependencies));
		javaWriter.emitEmptyLine();
		writeConstructor(javaWriter, dependencies, compositionHandler);
		javaWriter.emitEmptyLine();
		writeCreateMethod(factorySpec.getEntryPoint(), "create", javaWriter, factorySpec, compositionHandler, dependencies);

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

	private void writeConstructor(final JavaWriter javaWriter, final List<TypeMirror> dependencies, final CompositionHandler compositionHandler) throws IOException
	{
		Map<TypeMirror, String> parameterNameMap = getDependencyParameterMap(dependencies);
		List<String> parameters = new ArrayList<String>();
		for(Map.Entry<TypeMirror, String> parameterEntry: parameterNameMap.entrySet())
		{
			parameters.add(parameterEntry.getKey().toString());
			parameters.add(parameterEntry.getValue());
		}
		javaWriter.emitAnnotation(Inject.class);
		javaWriter.beginConstructor(PUBLIC, parameters, Collections.<String>emptyList());
		compositionHandler.writeConstructorBody(javaWriter, parameterNameMap);
		javaWriter.endConstructor();

	}

	private Map<TypeMirror, String> getDependencyParameterMap(final List<TypeMirror> dependencies)
	{
		Map<TypeMirror, String> parameterNameMap = new LinkedHashMap<TypeMirror, String>();
		for(TypeMirror dep: dependencies)
		{
			String depName = dep.toString();
			depName = depName.substring(depName.lastIndexOf(".") + 1);
			final String paramName = depName.substring(0, 1).toLowerCase() + depName.substring(1);
			parameterNameMap.put(dep, paramName);
		}
		return parameterNameMap;
	}

	private CompositionHandler locateCompositionHandler(final FactorySpec factorySpec)
	{
		//TODO: use a service loader!
		CompositionHandler handler = new SimpleCompositionHandler();
		assert handler.canComposeFactory(factorySpec, this.processingEnvironment);
		return handler;
	}

	private void writeCreateMethod(final TypeMirror entryPoint, final String methodName, final JavaWriter javaWriter, final FactorySpec factorySpec,
	                               final CompositionHandler compositionHandler,
	                               List<TypeMirror> dependencies) throws IOException
	{
		javaWriter.beginMethod(entryPoint.toString(), methodName, FINAL_PUBLIC);
		compositionHandler.writeCreateMethodBody(javaWriter, entryPoint, factorySpec, getDependencyParameterMap(dependencies));
		javaWriter.endMethod();
	}
}
