package net.peachjean.packtory.spi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.type.TypeMirror;

/**
 * TODO: Document this class
 */
public class FactorySpec
{
	private final String packageName;
	private final String factoryName;
	private final TypeMirror entryPoint;
	private final List<TypeMirror> compositions;

	public FactorySpec(final String packageName, final String factoryName, final TypeMirror entryPoint, final List<TypeMirror> compositions)
	{
		this.packageName = packageName;
		this.factoryName = factoryName;
		this.entryPoint = entryPoint;
		this.compositions = Collections.unmodifiableList(compositions);
	}

	public TypeMirror getEntryPoint()
	{
		return entryPoint;
	}

	public List<TypeMirror> getCompositions()
	{
		return compositions;
	}

	public String getPackageName()
	{
		return packageName;
	}

	public String getFactoryName()
	{
		return factoryName;
	}

	public String getFullyQualifiedFactoryName()
	{
		return String.format("%s.%s", packageName, factoryName);
	}
}
