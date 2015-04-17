package org.apodhrad.diffutils_ext;

import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.StringUtils;

import difflib.DiffUtils;
import difflib.Patch;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

/**
 * 
 * @author apodhrad
 *
 */
public class HtmlGenerator {

	public static final String DIFF_REPORTS = "diff-reports";
	public static final String DIFF_DIR = "diff";
	public static final String JS_LIB = "syntaxhighlighter_3.0.83";

	private Configuration cfg;
	private File diffReports;
	private File diffDir;
	private Template diffTemplate;
	private Template indexTemplate;

	public HtmlGenerator(String target) {
		this(new File(target));
	}

	public HtmlGenerator(File target) {
		this.diffReports = new File(target, DIFF_REPORTS);
		this.diffDir = new File(this.diffReports, DIFF_DIR);
	}

	public HtmlGenerator create() throws IOException {
		FileUtils.deleteQuietly(diffReports);
		diffReports.mkdirs();
		diffDir.mkdirs();

		cfg = new Configuration(new Version(2, 3, 22));
		cfg.setClassForTemplateLoading(HtmlGenerator.class, "/templates");

		try {
			diffTemplate = cfg.getTemplate("diff.ftl");
			indexTemplate = cfg.getTemplate("index.ftl");
		} catch (Exception e) {
			throw new HtmlGeneratorException("Cannot find a template", e);
		}

		copyFromResourceToDir(JS_LIB, new File(diffReports, "lib"));

		return this;
	}

	public HtmlGenerator generateHtmlDiff(File originalFile, File revisedFile, String... path) throws IOException {
		if (originalFile == null && revisedFile == null) {
			throw new IllegalArgumentException("At least one file must be specified!");
		}

		List<String> originalLines = originalFile == null ? emptyList() : FileUtils.readLines(originalFile);
		List<String> revisedLines = revisedFile == null ? emptyList() : FileUtils.readLines(revisedFile);

		String originalName = originalFile == null ? "null" : originalFile.getName();
		String revisedName = revisedFile == null ? "null" : revisedFile.getName();

		Patch diff = DiffUtils.diff(originalLines, revisedLines);
		List<String> lines = DiffUtils.generateUnifiedDiff(originalName, revisedName, originalLines, diff, 1);

		if (lines.isEmpty()) {
			return this;
		}

		File diffPathDir = new File(diffDir, StringUtils.join(path, "/"));
		diffPathDir.mkdirs();

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", revisedName);
		data.put("code", StringUtils.join(lines, "\n"));
		data.put("lib", diffPathDir.toPath().relativize(new File(diffReports, "lib").toPath()).toString());

		Writer fileWriter = new FileWriter(new File(diffPathDir, revisedName + ".html"));
		try {
			diffTemplate.process(data, fileWriter);
		} catch (TemplateException e) {
			throw new HtmlGeneratorException("Cannot create HTML diff with name '" + revisedName + "'", e);
		} finally {
			fileWriter.close();
		}

		return this;
	}

	public HtmlGenerator generateIndex() throws IOException {
		List<String> diffs = new ArrayList<String>();

		for (File file : FileUtils.listFiles(diffDir, suffixFileFilter(".html"), trueFileFilter())) {
			diffs.add(diffDir.toPath().relativize(file.toPath()).toString().replace(".html", ""));
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("diffs", diffs);

		Writer fileWriter = new FileWriter(new File(diffReports, "index.html"));
		try {
			indexTemplate.process(data, fileWriter);
		} catch (TemplateException e) {
			throw new HtmlGeneratorException("Cannot generate index.html", e);
		} finally {
			fileWriter.close();
		}

		return this;
	}

	private void copyFromResourceToDir(String resource, File dir) throws IOException {
		URL url = getClass().getResource("/" + resource);
		if (url == null) {
			throw new HtmlGeneratorException("Cannot find resource '" + resource + "'");
		}
		File resourceFile = new File(url.getFile());
		FileUtils.copyDirectory(resourceFile, dir);
	}

	private List<String> emptyList() {
		return new ArrayList<String>();
	}

}
