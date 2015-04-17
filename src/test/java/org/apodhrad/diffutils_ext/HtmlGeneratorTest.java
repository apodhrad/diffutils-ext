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

	public HtmlGeneratorTest() {
		test1 = new File(getClass().getResource("/test1.txt").getFile());
		test1Copy = new File(getClass().getResource("/test1-copy.txt").getFile());
		test2 = new File(getClass().getResource("/test2.txt").getFile());
		dir1 = new File(getClass().getResource("/dir1").getFile());
		dir2 = new File(getClass().getResource("/dir2").getFile());
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
		assertTrue(getDiffDir().exists());
		assertTrue(new File(getDiffDir(), "test2.txt.html").exists());

		File index = new File(getDiffReportsDir(), "index.html");

		assertHtml("<a href=\"diff/test2.txt.html\">test2.txt</a>", index);

		htmlGenerator.generateHtmlDiff(test2, test1).generateIndex();
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(new File(getDiffDir(), "test1.txt.html").exists());
		assertTrue(new File(getDiffDir(), "test2.txt.html").exists());

		assertHtml("<a href=\"diff/test1.txt.html\">test1.txt</a>", index);
		assertHtml("<a href=\"diff/test2.txt.html\">test2.txt</a>", index);
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

		assertHtml("<a href=\"diff/test2.txt.html\">test2.txt</a>", index);
	}

	@Test
	public void generateHtmlDiffWithoutRevisedTest() throws Exception {
		HtmlGenerator htmlGenerator = new HtmlGenerator(target).create();
		htmlGenerator.generateHtmlDiff(test1, null).generateIndex();

		assertTrue(getDiffReportsDir().exists());
		assertTrue(new File(getDiffReportsDir(), "index.html").exists());
		assertTrue(getLibDir().exists());
		assertTrue(getDiffDir().exists());
		assertTrue(new File(getDiffDir(), "test1.txt.html").exists());

		File index = new File(getDiffReportsDir(), "index.html");

		assertHtml("<a href=\"diff/test1.txt.html\">test1.txt</a>", index);
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

		assertNotHtml("<a href=\"diff/null.html\">null</a>", index);
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

		assertHtml("<a href=\"diff/group/com/example/test2.txt.html\">group/com/example/test2.txt</a>", index);
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

		assertNotHtml("<a href=\"diff/null.html\">null</a>", index);
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

		assertHtml("<a href=\"diff/dir/com/example/Hello.java.html\">dir/com/example/Hello.java</a>", index);
		assertHtml("<a href=\"diff/dir/com/example/NewClass.java.html\">dir/com/example/NewClass.java</a>", index);
		assertHtml("<a href=\"diff/dir/com/example/OldClass.java.html\">dir/com/example/OldClass.java</a>", index);
	}

	private void assertHtml(String expected, File htmlFile) throws IOException {
		String html = FileUtils.readFileToString(htmlFile);
		assertThat(html, containsString(expected));
	}

	private void assertNotHtml(String expected, File htmlFile) throws IOException {
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
