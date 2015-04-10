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
import org.hamcrest.core.SubstringMatcher;

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

	private Configuration cfg;
	private File target;
	private File diffDir;
	private File syntaxhighlighterFile;
	private Template diffTemplate;
	private Template indexTemplate;
	
	private List<String> diffs;
	
	public HtmlGenerator(String target) {
		this(new File(target));
	}
	
	public HtmlGenerator(File target) {
		this.target = new File(target, "diff-reports");
		this.target.mkdirs();
		this.diffDir = new File(this.target, "diff");
		this.diffDir.mkdirs();
		
		cfg = new Configuration(new Version(2, 3, 22));
		cfg.setClassForTemplateLoading(HtmlGenerator.class, "/templates");
		
		try {
			diffTemplate = cfg.getTemplate("diff.ftl");
		} catch (Exception e) {
			throw new RuntimeException("Cannot find template 'diff.ftl'", e);
		}
		try {
			indexTemplate = cfg.getTemplate("index.ftl");
		} catch (Exception e) {
			throw new RuntimeException("Cannot find template 'index.ftl'", e);
		}
		
		URL url = getClass().getResource("/syntaxhighlighter_3.0.83");
		syntaxhighlighterFile = new File(url.getFile());
		if(!syntaxhighlighterFile.exists()) {
			throw new RuntimeException("Cannot find syntaxhighlighter_3.0.83");
		}
		
		diffs = new ArrayList<String>();
	}
	
	public HtmlGenerator generateHtmlDiff(Diff diff, String name) throws TemplateException, IOException {
		if(name == null) {
			throw new IllegalArgumentException("Name must be specified!");
		}
		if (name.toLowerCase().endsWith(".html")) {
			name = name.substring(0, name.length() - 5);
		}
		
		FileUtils.copyDirectory(syntaxhighlighterFile, new File(target, "lib"));
		diffDir.mkdirs();
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", name);
		data.put("code", diff.toString());

		Writer fileWriter = new FileWriter(new File(diffDir, name + ".html"));
		try {
			diffTemplate.process(data, fileWriter);
		} finally {
			fileWriter.close();
		}
		
		diffs.add(name);
		
		return this;
	}
	
	public HtmlGenerator generateIndex() throws TemplateException, IOException {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("diffs", diffs);

		Writer fileWriter = new FileWriter(new File(target, "index.html"));
		try {
			indexTemplate.process(data, fileWriter);
		} finally {
			fileWriter.close();
		}
		
		return this;
	}

	public static void main(String[] args) throws Exception {
		
	}
}
