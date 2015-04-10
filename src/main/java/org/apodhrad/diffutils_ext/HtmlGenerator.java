package org.apodhrad.diffutils_ext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
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

	private Configuration cfg;
	private File target;
	private File syntaxhighlighterFile;
	private Template template;
	
	public HtmlGenerator(String target) {
		this(new File(target));
	}
	
	public HtmlGenerator(File target) {
		this.target = new File(target, "diff-reports");
		this.target.mkdirs();
		
		cfg = new Configuration(new Version(2, 3, 22));
		cfg.setClassForTemplateLoading(HtmlGenerator.class, "/templates");
		
		try {
			template = cfg.getTemplate("diff.ftl");
		} catch (Exception e) {
			throw new RuntimeException("Cannot find template 'diff.ftl'", e);
		}
		
		URL url = getClass().getResource("/syntaxhighlighter_3.0.83");
		syntaxhighlighterFile = new File(url.getFile());
		if(!syntaxhighlighterFile.exists()) {
			throw new RuntimeException("Cannot find syntaxhighlighter_3.0.83");
		}
	}
	
	public void generateHtmlDiff(Diff diff, String name) throws TemplateException, IOException {
		if(name == null) {
			throw new IllegalArgumentException("Name must be specified!");
		}
		if (!name.toLowerCase().endsWith(".html")) {
			name = name + ".html";
		}
		
		FileUtils.copyDirectory(syntaxhighlighterFile, target);
		
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", name);
		data.put("code", diff.toString());

		Writer fileWriter = new FileWriter(new File(target, name));
		try {
			template.process(data, fileWriter);
		} finally {
			fileWriter.close();
		}
	}

	public static void main(String[] args) throws Exception {
		
	}
}
