package org.apodhrad.diffutils_ext;

import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author apodhrad
 *
 */
public class HtmlGeneratorTest {

	private File target;
	protected File test1;
	protected File test1Copy;
	protected File test2;
	private File dir1;
	private File dir2;
	private File binary1;
	private File binary2;

	public HtmlGeneratorTest() {
		test1 = new File(getClass().getResource("/test1.txt").getFile());
		test1Copy = new File(getClass().getResource("/test1-copy.txt").getFile());
		test2 = new File(getClass().getResource("/test2.txt").getFile());
		dir1 = new File(getClass().getResource("/dir1").getFile());
		dir2 = new File(getClass().getResource("/dir2").getFile());
		binary1 = new File(getClass().getResource("/Hello1.class").getFile());
		binary2 = new File(getClass().getResource("/Hello2.class").getFile());

		target = new File(System.getProperty("project.build.directory", "target"));
	}

	@Before
	@After
	public void deleteTestDir() {
		FileUtils.deleteQuietly(getDiffReportsDir());
	}

	@Test
	public void generateHtmlDiffTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(test1, test2).generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(getLibDir().exists());
		assertTrue(new File(getLibDir(), "scripts").exists());
		assertTrue(new File(getLibDir(), "styles").exists());
		assertTrue(getDiffDir().exists());
		assertTrue(new File(getDiffDir(), "test2.txt.html").exists());

		File index = new File(getDiffReportsDir(), "index.html");

		assertContains("<a href=\"diff/test2.txt.html\">test2.txt</a>", index);

		htmlGenerator.generateHtmlDiff(test2, test1).generateIndex();
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(new File(getDiffDir(), "test1.txt.html").exists());
		assertTrue(new File(getDiffDir(), "test2.txt.html").exists());

		assertContains("<a href=\"diff/test1.txt.html\">test1.txt</a>", index);
		assertContains("<a href=\"diff/test2.txt.html\">test2.txt</a>", index);
	}

	@Test
	public void generateHtmlDiffWithoutOriginalTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(null, test2).generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(new File(getDiffDir(), "test2.txt.html").exists());

		File index = new File(getDiffReportsDir(), "index.html");

		assertContains("<a href=\"diff/test2.txt.html\">test2.txt</a>", index);
	}

	@Test
	public void generateHtmlDiffWithoutRevisedTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(test1, null).generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(getDiffDir().listFiles().length == 1);
		assertTrue(new File(getDiffDir(), "test1.txt.html").exists());

		File index = new File(getDiffReportsDir(), "index.html");

		assertContains("<a href=\"diff/test1.txt.html\">test1.txt</a>", index);
	}

	@Test
	public void generateHtmlDiffForSameFilesTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(test1, test1Copy).generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(getDiffDir().listFiles().length == 0);

		File index = new File(getDiffReportsDir(), "index.html");

		assertNotContains("<a href=\"diff/null.html\">null</a>", index);
	}

	@Test
	public void generateHtmlDiffWithSameBinaryFilesTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(binary1, binary1).generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(getDiffDir().listFiles().length == 0);

		File index = new File(getDiffReportsDir(), "index.html");

		assertNotContains("<a href=\"diff/Hello2.class.html\">Hello2.class</a>", index);
	}

	@Test
	public void generateHtmlDiffWithBinaryFilesTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(binary1, binary2).generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(getDiffDir().listFiles().length == 1);
		assertTrue(new File(getDiffDir(), "Hello2.class.html").exists());
		assertContains("Binary files Hello1.class and Hello2.class differ", new File(getDiffDir(), "Hello2.class.html"));

		File index = new File(getDiffReportsDir(), "index.html");

		assertContains("<a href=\"diff/Hello2.class.html\">Hello2.class</a>", index);
	}

	@Test
	public void generateHtmlDiffWithPathTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(test1, test2, "group", "com", "example").generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(new File(getDiffDir(), "/group/com/example/test2.txt.html").exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());

		File index = new File(getDiffReportsDir(), "index.html");

		assertContains("<a href=\"diff/group/com/example/test2.txt.html\">group/com/example/test2.txt</a>", index);
	}

	@Test(expected = HtmlGeneratorException.class)
	public void generateHtmlDiffWithDirTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(dir1, dir2).generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(getDiffDir().listFiles().length == 0);
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());

		File index = new File(getDiffReportsDir(), "index.html");

		assertNotContains("<a href=\"diff/null.html\">null</a>", index);
	}

	@Test
	public void generateHtmlDiffDirTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiffDir(dir1, dir2, "dir").generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(new File(getDiffDir(), "/dir/com/example/Hello.java.html").exists());
		assertTrue(new File(getDiffDir(), "/dir/com/example/NewClass.java.html").exists());
		assertTrue(new File(getDiffDir(), "/dir/com/example/OldClass.java.html").exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());

		File index = new File(getDiffReportsDir(), "index.html");

		assertContains("<a href=\"diff/dir/com/example/Hello.java.html\">dir/com/example/Hello.java</a>", index);
		assertContains("<a href=\"diff/dir/com/example/NewClass.java.html\">dir/com/example/NewClass.java</a>", index);
		assertContains("<a href=\"diff/dir/com/example/OldClass.java.html\">dir/com/example/OldClass.java</a>", index);
	}
	
	@Test
	public void generateHtmlDiffDirWithSubfolderTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiffDir(dir1, dir2, "dir").generateIndex("dir", "abc");

		assertTrue(getDiffReportsDir().exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(new File(getDiffDir(), "/dir/com/example/Hello.java.html").exists());
		assertTrue(new File(getDiffDir(), "/dir/com/example/NewClass.java.html").exists());
		assertTrue(new File(getDiffDir(), "/dir/com/example/OldClass.java.html").exists());
		assertTrue(new File(getDiffReportsDir(), "abc.html").exists());

		File index = new File(getDiffReportsDir(), "abc.html");

		assertContains("<a href=\"diff/dir/com/example/Hello.java.html\">dir/com/example/Hello.java</a>", index);
		assertContains("<a href=\"diff/dir/com/example/NewClass.java.html\">dir/com/example/NewClass.java</a>", index);
		assertContains("<a href=\"diff/dir/com/example/OldClass.java.html\">dir/com/example/OldClass.java</a>", index);
	}

	private void assertContains(String expected, File htmlFile) throws IOException {
		String html = FileUtils.readFileToString(htmlFile);
		assertThat(html, containsString(expected));
	}

	private void assertNotContains(String expected, File htmlFile) throws IOException {
		String html = FileUtils.readFileToString(htmlFile);
		assertThat(html, not(containsString(expected)));
	}

	private File getDiffReportsDir() {
		return new File(target, "diff-reports");
	}

	private File getLibDir() {
		return new File(getDiffReportsDir(), "lib");
	}

	private File getDiffDir() {
		return new File(getDiffReportsDir(), "diff");
	}

}
