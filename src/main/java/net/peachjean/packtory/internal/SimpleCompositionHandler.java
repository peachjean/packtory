package net.peachjean.packtory.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import net.peachjean.packtory.spi.CompositionHandler;
import net.peachjean.packtory.spi.FactorySpec;

import com.squareup.javawriter.JavaWriter;

import org.kohsuke.MetaInfServices;

/**
 * TODO: Document this class
 */
@MetaInfServices
public class SimpleCompositionHandler implements CompositionHandler
{

	private static final HashSet<Modifier> PRIVATE_FINAL = new HashSet<Modifier>(Arrays.asList(Modifier.FINAL, Modifier.PRIVATE));

	@Override
	public boolean canComposeFactory(final FactorySpec spec, final ProcessingEnvironment processingEnvironment)
	{
		if (spec.getCompositions().size() != 1)
		{
			return false;
		}
		TypeMirror implementationType = spec.getCompositions().get(0);
		return processingEnvironment.getTypeUtils().isSubtype(implementationType, spec.getEntryPoint());
	}

	@Override
	public List<TypeMirror> determineDependencies(final FactorySpec spec, final ProcessingEnvironment processingEnvironment)
	{
		final DeclaredType implementationType = (DeclaredType) spec.getCompositions().get(0);
		final List<ExecutableElement> availableConstructors = new ArrayList<ExecutableElement>();
		for (Element element : implementationType.asElement().getEnclosedElements())
		{
			if (element.getKind() == ElementKind.CONSTRUCTOR)
			{
				final Set<Modifier> modifiers = element.getModifiers();
				if (modifiers.contains(Modifier.PRIVATE))
				{
					continue;
				}
				else
				{
					availableConstructors.add((ExecutableElement) element);
				}
			}
		}
		if (availableConstructors.size() == 0)
		{
			return null;
		}
		else if (availableConstructors.size() == 1)
		{
			return getDepsFromConstructor(availableConstructors.get(0));
		}
		else
		{
			List<ExecutableElement> injectableConstructors = new ArrayList<ExecutableElement>();
			for (ExecutableElement constructor : availableConstructors)
			{
				if (constructor.getAnnotation(Inject.class) != null)
				{
					injectableConstructors.add(constructor);
				}
			}
			if (injectableConstructors.size() == 0)
			{
				return null;
			}
			else if (injectableConstructors.size() > 1)
			{
				return null;
			}
			else
			{
				return getDepsFromConstructor(injectableConstructors.get(0));
			}
		}
	}

	private List<TypeMirror> getDepsFromConstructor(final ExecutableElement constructor)
	{
		final List<? extends VariableElement> parameters = constructor.getParameters();
		List<TypeMirror> deps = new ArrayList<TypeMirror>();
		for (VariableElement parameter : parameters)
		{
			deps.add(parameter.asType());
		}
		return deps;
	}

	@Override
	public void writeFields(final JavaWriter javaWriter, final Map<TypeMirror, String> parameterNameMap) throws IOException
	{
		for (Map.Entry<TypeMirror, String> parameter : parameterNameMap.entrySet())
		{
			javaWriter.emitField(parameter.getKey().toString(), parameter.getValue(), PRIVATE_FINAL);
		}
	}

	@Override
	public void writeConstructorBody(final JavaWriter javaWriter, final Map<TypeMirror, String> parameterNameMap) throws IOException
	{
		for (Map.Entry<TypeMirror, String> parameter : parameterNameMap.entrySet())
		{
			javaWriter.emitStatement("this.%s = %s", parameter.getValue(), parameter.getValue());
		}
	}

	@Override
	public void writeCreateMethodBody(final JavaWriter javaWriter, final TypeMirror returnType, final FactorySpec spec, final Map<TypeMirror,
			String> parameterNameMap) throws IOException
	{
		javaWriter.emitStatement("return new %s(%s)", (DeclaredType) spec.getCompositions().get(0), createParamList(parameterNameMap));
	}

	private Object createParamList(final Map<TypeMirror, String> parameterNameMap)
	{
		StringBuffer sb = new StringBuffer();
		for (String name : parameterNameMap.values())
		{
			if (sb.length() > 0)
			{
				sb.append(",");
			}
			sb.append(name);
		}
		return sb.toString();
	}
}
