package org.apodhrad.diffutils_ext;

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

	private List<String> diffs;

	public HtmlGenerator(String target) {
		this(new File(target));
	}

	public HtmlGenerator(File target) {
		this.diffReports = new File(target, DIFF_REPORTS);
		this.diffDir = new File(this.diffReports, DIFF_DIR);

		diffs = new ArrayList<String>();
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

	public HtmlGenerator generateHtmlDiff(Diff diff, String name) throws IOException {
		if (name == null) {
			throw new IllegalArgumentException("Name must be specified!");
		}
		if (name.toLowerCase().endsWith(".html")) {
			name = name.substring(0, name.length() - 5);
		}

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", name);
		data.put("code", diff.toString());

		Writer fileWriter = new FileWriter(new File(diffDir, name + ".html"));
		try {
			diffTemplate.process(data, fileWriter);
		} catch (TemplateException e) {
			throw new HtmlGeneratorException("Cannot create HTML diff with name '" + name + "'", e);
		} finally {
			fileWriter.close();
		}

		diffs.add(name);

		return this;
	}

	public HtmlGenerator generateIndex() throws IOException {
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

}
