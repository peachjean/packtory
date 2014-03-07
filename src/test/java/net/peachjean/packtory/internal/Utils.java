package net.peachjean.packtory.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

/**
 * TODO: Document this class
 */
class Utils
{
	private static final File EXAMPLES_BASEDIR = new File("src/test/examples");

	static JavaFileObject[] loadExampleSource(final String packageName)
	{
		File dir = new File(EXAMPLES_BASEDIR, packageName);

		List<JavaFileObject> javaObjects = new ArrayList<JavaFileObject>();
		for (File source : dir.listFiles(new FilenameFilter()
		{
			@Override
			public boolean accept(final File dir, final String name)
			{
				return name.endsWith(".java");
			}
		}))
		{
			javaObjects.add(new FileJavaObject(source));
		}
		return javaObjects.toArray(new JavaFileObject[javaObjects.size()]);
	}

	private static class FileJavaObject extends SimpleJavaFileObject
	{
		private final File source;

		public FileJavaObject(final File source)
		{
			super(source.toURI(), Kind.SOURCE);
			this.source = source;
		}

		@Override
		public Reader openReader(final boolean ignoreEncodingErrors) throws IOException
		{
			return new InputStreamReader(new FileInputStream(this.source), Charsets.UTF_8);
		}

		@Override
		public CharSequence getCharContent(final boolean ignoreEncodingErrors) throws IOException
		{
			return IOUtils.toString(openReader(ignoreEncodingErrors));
		}
	}
}
