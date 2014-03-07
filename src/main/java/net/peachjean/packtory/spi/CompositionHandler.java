package net.peachjean.packtory.spi;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import com.squareup.javawriter.JavaWriter;

/**
 * TODO: Document this class
 */
public interface CompositionHandler
{
	boolean canComposeFactory(FactorySpec spec, ProcessingEnvironment processingEnvironment);

	List<TypeMirror> determineDependencies(FactorySpec spec, ProcessingEnvironment processingEnvironment);

	/**
	 *
	 * @param javaWriter
	 * @param parameterNameMap a map of types to parameter names, guaranteed to iterate in the same order as the
	 *                         list returned by {@link #determineDependencies}.
	 */
	void writeConstructorBody(JavaWriter javaWriter, Map<TypeMirror, String> parameterNameMap) throws IOException;

	void writeFields(JavaWriter javaWriter, Map<TypeMirror, String> parameterNameMap) throws IOException;

	void writeCreateMethodBody(JavaWriter javaWriter, TypeMirror returnType, FactorySpec spec, Map<TypeMirror, String> parameterNameMap) throws IOException;
}
