package net.peachjean.packtory.internal

import net.peachjean.commons.test.junit.TmpDir
import net.peachjean.tater.test.CompilerHarness
import org.junit.Rule
import spock.lang.Specification

import static net.peachjean.packtory.internal.Utils.*

/**
 * TODO: Document this class
 */
class PacktoryAnnotationProcessorTest extends Specification
{
	@Rule TmpDir tmpDir = new TmpDir();

	def "simple package factory"() {
		given: "some source files"
			def exampleSource = loadExampleSource("simple")
		when: "source is compiled"
			def compilerResults = new CompilerHarness(tmpDir.getDir(), exampleSource).addProcessor(new PacktoryAnnotationProcessor()).invoke()
			print compilerResults.getCompilerOutput()
			print compilerResults.getDiagnostics()
		then: "the compile succeeded"
			compilerResults.isSuccessful()
		then: "a factory is instantiable"
		    def factory = compilerResults.createClassLoader().loadClass("simple.Factory").newInstance("myText")
		and: "an object can be retrieved from it"
			def entryPoint = factory.create()
		and: "entry point returns the right values"
			"mytext" == entryPoint.getLowerCased()
			"MYTEXT" == entryPoint.getUpperCased()
	}
}
