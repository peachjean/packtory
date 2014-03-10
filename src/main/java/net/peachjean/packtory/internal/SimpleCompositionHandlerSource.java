package net.peachjean.packtory.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
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
import net.peachjean.packtory.spi.CompositionHandlerSource;
import net.peachjean.packtory.spi.FactorySpec;

import com.squareup.javawriter.JavaWriter;

import org.kohsuke.MetaInfServices;

/**
 * TODO: Document this class
 */
@MetaInfServices
public class SimpleCompositionHandlerSource implements CompositionHandlerSource
{
	private static final HashSet<Modifier> PRIVATE_FINAL = new HashSet<Modifier>(Arrays.asList(Modifier.FINAL, Modifier.PRIVATE));

	@Nullable
	@Override
	public CompositionHandler createHandlerIfCapable(final FactorySpec spec, final ProcessingEnvironment processingEnvironment)
	{
		if (spec.getCompositions().size() != 1)
		{
			return null;
		}
		TypeMirror implementationType = spec.getCompositions().get(0);
		if (processingEnvironment.getTypeUtils().isSubtype(implementationType, spec.getEntryPoint()))
		{
			return new Handler(spec, processingEnvironment);
		}
		else
		{
			return null;
		}
	}

	private static class Handler implements CompositionHandler
	{

		private final FactorySpec factorySpec;
		private final ProcessingEnvironment processingEnvironment;
		private final Map<String,TypeMirror> constructorParamMap;

		public Handler(final FactorySpec factorySpec, final ProcessingEnvironment processingEnvironment)
		{
			this.factorySpec = factorySpec;
			this.processingEnvironment = processingEnvironment;
			final ExecutableElement constructor = findApplicableConstructor();
			this.constructorParamMap = constructor == null ? null : getDepsFromConstructor(constructor);
		}

		@Override
		public Map<String, TypeMirror> getDependencies()
		{
			return constructorParamMap;
		}

		private ExecutableElement findApplicableConstructor()
		{
			final ExecutableElement constructor;
			final DeclaredType implementationType = (DeclaredType) this.factorySpec.getCompositions().get(0);
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
				constructor = null;
			}
			else if (availableConstructors.size() == 1)
			{
				constructor = availableConstructors.get(0);
			}
			else
			{
				List<ExecutableElement> injectableConstructors = new ArrayList<ExecutableElement>();
				for (ExecutableElement candidate : availableConstructors)
				{
					if (candidate.getAnnotation(Inject.class) != null)
					{
						injectableConstructors.add(candidate);
					}
				}
				if (injectableConstructors.size() == 0)
				{
					constructor = null;
				}
				else if (injectableConstructors.size() > 1)
				{
					constructor = null;
				}
				else
				{
					constructor = injectableConstructors.get(0);
				}
			}
			return constructor;
		}

		private Map<String, TypeMirror> getDepsFromConstructor(final ExecutableElement constructor)
		{
			final List<? extends VariableElement> parameters = constructor.getParameters();
			Map<String, TypeMirror> depsMaps = new LinkedHashMap<String, TypeMirror>();
			for (VariableElement parameter : parameters)
			{
				depsMaps.put(getParamName(parameter.asType()), parameter.asType());
			}
			return depsMaps;
		}

		private String getParamName(final TypeMirror typeMirror)
		{
			String name = typeMirror.toString();
			name = name.substring(name.lastIndexOf(".") + 1);
			name = name.substring(0, 1).toLowerCase() + name.substring(1);
			return name;
		}

		@Override
		public void writeFields(final JavaWriter javaWriter) throws IOException
		{
			for (Map.Entry<String, TypeMirror> parameter : constructorParamMap.entrySet())
			{
				javaWriter.emitField(parameter.getValue().toString(), parameter.getKey(), PRIVATE_FINAL);
			}
		}

		@Override
		public void writeConstructorBody(final JavaWriter javaWriter) throws IOException
		{
			for (String paramName: constructorParamMap.keySet())
			{
				javaWriter.emitStatement("this.%s = %s", paramName, paramName);
			}
		}

		@Override
		public void writeCreateMethodBody(final JavaWriter javaWriter, final TypeMirror returnType) throws IOException
		{
			javaWriter.emitStatement("return new %s(%s)", (DeclaredType) this.factorySpec.getCompositions().get(0), createParamList(this.constructorParamMap.keySet()));
		}

		private Object createParamList(final Set<String> parameterNames)
		{
			StringBuffer sb = new StringBuffer();
			for (String name : parameterNames)
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
}
