package net.peachjean.packtory.spi;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import com.squareup.javawriter.JavaWriter;

/**
 * A stateful handler for the composition of a factory.
 */
public interface CompositionHandler
{
	/**
	 * A map of this factory's dependencies. The keys are guaranteed to be used as the names of the constructor
	 * parameters, in the order of iteration.
	 * @return
	 */
	Map<String, TypeMirror> getDependencies();

	void writeFields(JavaWriter javaWriter) throws IOException;

	void writeConstructorBody(JavaWriter javaWriterparameterNameMap) throws IOException;

	void writeCreateMethodBody(JavaWriter javaWriter, TypeMirror returnType) throws IOException;
}
