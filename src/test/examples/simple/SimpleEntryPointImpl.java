package simple;

class SimpleEntryPointImpl implements SimpleEntryPoint
{
	private final String baseString;

	SimpleEntryPointImpl(final String baseString)
	{
		this.baseString = baseString;
	}

	public String getUpperCased()
	{
		return this.baseString.toUpperCase();
	}

	public String getLowerCased()
	{
		return this.baseString.toLowerCase();
	}
}
