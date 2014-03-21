package net.peachjean.packtory.internal.dagger;

import java.io.IOException;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import net.peachjean.packtory.spi.CompositionHandler;
import net.peachjean.packtory.spi.FactorySpec;

import com.squareup.javawriter.JavaWriter;

/**
 * TODO: Document this class
 */
class DaggerCompositionHandler implements CompositionHandler
{
	public DaggerCompositionHandler(final FactorySpec spec, final ProcessingEnvironment processingEnvironment)
	{

	}

	@Override
	public Map<String, TypeMirror> getDependencies()
	{
		return null;
	}

	@Override
	public void writeFields(final JavaWriter javaWriter) throws IOException
	{

	}

	@Override
	public void writeConstructorBody(final JavaWriter javaWriterparameterNameMap) throws IOException
	{

	}

	@Override
	public void writeCreateMethodBody(final JavaWriter javaWriter, final TypeMirror returnType) throws IOException
	{

	}
}
