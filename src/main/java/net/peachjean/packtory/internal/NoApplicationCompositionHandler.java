package net.peachjean.packtory.internal;

import net.peachjean.packtory.spi.FactorySpec;

/**
 * TODO: Document this class
 */
class NoApplicationCompositionHandler extends Exception
{
	public NoApplicationCompositionHandler(final FactorySpec factorySpec)
	{
		super(String.format("No composition handler could be located to handle package '%s'.", factorySpec.getPackageName()));
	}
}
