package net.peachjean.packtory.spi;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;

/**
 * TODO: Document this class
 */
public interface CompositionHandlerSource
{
	/**
	 * If this source is capable of handling the provided {@code factorySpec}, then this method should return a handler.
	 * Otherwise it should return {@code null}.
	 *
	 * The handler is specific to the given factory spec and will not be reused.
	 */
	@Nullable
	CompositionHandler createHandlerIfCapable(FactorySpec factorySpec, ProcessingEnvironment processingEnvironment);
}
