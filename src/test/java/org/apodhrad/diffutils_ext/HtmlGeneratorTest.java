package org.apodhrad.diffutils_ext;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import difflib.DiffUtils;
import difflib.Patch;

public class HtmlGeneratorTest {

	private File target;
	protected File test1;
	protected File test2;

	public HtmlGeneratorTest() {
		test1 = new File(getClass().getResource("/test1.txt").getFile());
		test2 = new File(getClass().getResource("/test2.txt").getFile());
		target = new File(System.getProperty("project.build.directory", "target"));
	}
	
	@Before //@After
	public void deleteTestDir() {
		FileUtils.deleteQuietly(new File(target, "diff-reports"));
	}

	@Test
	public void test() throws Exception {
		List<String> original = FileUtils.readLines(test1);
		List<String> revised = FileUtils.readLines(test2);

		Patch patch = DiffUtils.diff(original, revised);
		Diff diff = new Diff(patch);
		
		new HtmlGenerator(target).generateHtmlDiff(diff, "test");
		
	}
}
