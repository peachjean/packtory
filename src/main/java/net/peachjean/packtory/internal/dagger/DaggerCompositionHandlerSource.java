package net.peachjean.packtory.internal.dagger;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import dagger.Module;
import net.peachjean.packtory.spi.CompositionHandler;
import net.peachjean.packtory.spi.CompositionHandlerSource;
import net.peachjean.packtory.spi.FactorySpec;

import org.kohsuke.MetaInfServices;

/**
 * TODO: Document this class
 */
@MetaInfServices(CompositionHandlerSource.class)
public class DaggerCompositionHandlerSource implements CompositionHandlerSource
{
	@Nullable
	@Override
	public CompositionHandler createHandlerIfCapable(final FactorySpec spec, final ProcessingEnvironment processingEnvironment)
	{
		if (spec.getCompositions().size() != 1)
		{
			return null;
		}
		if (areAllDaggerModules(spec.getCompositions(), processingEnvironment))
		{
			return new DaggerCompositionHandler(spec, processingEnvironment);
		}
		else
		{
			return null;
		}
	}

	public boolean areAllDaggerModules(final List<TypeMirror> compositions, final ProcessingEnvironment processingEnvironment)
	{
		// we require at least one module otherwise why use the dagger?
		if (compositions.isEmpty())
		{
			return false;
		}
		for (TypeMirror composition: compositions)
		{
			final Element element = processingEnvironment.getTypeUtils().asElement(composition);
			if (!(element instanceof TypeElement) || getAnnotationMirror((TypeElement) element, Module.class) == null)
			{
				return false;
			}
		}
		return true;
	}

	private static AnnotationMirror getAnnotationMirror(TypeElement typeElement, Class<?> clazz) {
		String clazzName = clazz.getName();
		for(AnnotationMirror m : typeElement.getAnnotationMirrors()) {
			if(m.getAnnotationType().toString().equals(clazzName)) {
				return m;
			}
		}
		return null;
	}
}
