package org.apodhrad.diffutils_ext;

import static org.apache.commons.io.filefilter.FileFilterUtils.suffixFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
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

	public HtmlGenerator generateHtmlDiffDir(File originalDir, File revisedDir, String... path) throws IOException {
		if (originalDir == null || revisedDir == null) {
			throw new IllegalArgumentException("At least one file must be specified!");
		}

		if (!originalDir.isDirectory() || !revisedDir.isDirectory()) {
			throw new HtmlGeneratorException("You have to specify directories!");
		}

		Map<String, Long> originalFiles = new HashMap<String, Long>();
		for (File originalFile : FileUtils.listFiles(originalDir, trueFileFilter(), trueFileFilter())) {
			String filePath = originalDir.toPath().relativize(originalFile.toPath()).toString();
			originalFiles.put(filePath, FileUtils.checksumCRC32(originalFile));
		}

		Map<String, Long> revisedFiles = new HashMap<String, Long>();
		for (File revisedFile : FileUtils.listFiles(revisedDir, trueFileFilter(), trueFileFilter())) {
			String filePath = revisedDir.toPath().relativize(revisedFile.toPath()).toString();
			revisedFiles.put(filePath, FileUtils.checksumCRC32(revisedFile));
		}

		Set<String> diffFiles = new HashSet<String>();
		for (String filePath : originalFiles.keySet()) {
			Long originalHash = originalFiles.get(filePath);
			Long revisedHash = revisedFiles.get(filePath);
			if (originalHash == null || revisedHash == null || !originalHash.equals(revisedHash)) {
				diffFiles.add(filePath);
				revisedFiles.remove(filePath);
			}
		}
		for (String filePath : revisedFiles.keySet()) {
			Long originalHash = originalFiles.get(filePath);
			Long revisedHash = revisedFiles.get(filePath);
			if (originalHash == null || revisedHash == null || !originalHash.equals(revisedHash)) {
				diffFiles.add(filePath);
			}
		}

		for (String filePath : diffFiles) {
			File originalFile = getFile(originalDir, filePath);
			File revisedFile = getFile(revisedDir, filePath);
			String[] diffFilePath = ArrayUtils.addAll(path, filePath.split("/"));
			generateHtmlDiff(originalFile, revisedFile, ArrayUtils.remove(diffFilePath, diffFilePath.length - 1));
		}

		return this;
	}

	public HtmlGenerator generateHtmlDiff(File originalFile, File revisedFile, String... path) throws IOException {
		if (originalFile == null && revisedFile == null) {
			throw new IllegalArgumentException("At least one file must be specified!");
		}

		if ((originalFile != null && originalFile.isDirectory()) || (revisedFile != null && revisedFile.isDirectory())) {
			throw new HtmlGeneratorException("Directories are not supported!");
		}

		String originalName = originalFile == null ? "null" : originalFile.getName();
		String revisedName = revisedFile == null ? "null" : revisedFile.getName();

		List<String> lines = new ArrayList<String>();

		if (isBinaryFile(originalFile) || isBinaryFile(revisedFile)) {
			if (FileUtils.checksumCRC32(originalFile) == FileUtils.checksumCRC32(revisedFile)) {
				return this;
			}
			lines.add("Binary files " + originalName + " and " + revisedName + " differ");
		} else {
			List<String> originalLines = originalFile == null ? emptyList() : FileUtils.readLines(originalFile);
			List<String> revisedLines = revisedFile == null ? emptyList() : FileUtils.readLines(revisedFile);
			Patch diff = DiffUtils.diff(originalLines, revisedLines);
			lines = DiffUtils.generateUnifiedDiff(originalName, revisedName, originalLines, diff, 1);
		}

		if (lines.isEmpty()) {
			return this;
		}

		File diffPathDir = new File(diffDir, StringUtils.join(path, "/"));
		diffPathDir.mkdirs();

		String name = revisedName.equals("null") ? originalName : revisedName;

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("name", name);
		data.put("code", StringUtils.join(lines, "\n"));
		data.put("lib", diffPathDir.toPath().relativize(new File(diffReports, "lib").toPath()).toString());

		Writer fileWriter = new FileWriter(new File(diffPathDir, name + ".html"));
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
		URLConnection urlConnection = url.openConnection();
		if (urlConnection instanceof JarURLConnection) {
			copyJarResourceToFolder((JarURLConnection) urlConnection, dir);
		} else {
			FileUtils.copyURLToFile(url, dir);
		}
	}

	private List<String> emptyList() {
		return new ArrayList<String>();
	}

	private static File getFile(File dir, String filePath) {
		File file = new File(dir, filePath);
		if (file.exists()) {
			return file;
		}
		return null;
	}

	private static boolean isBinaryFile(File f) throws FileNotFoundException, IOException {
		if (f == null) {
			return false;
		}
		FileInputStream in = new FileInputStream(f);
		int size = in.available();
		if (size > 1024)
			size = 1024;
		byte[] data = new byte[size];
		in.read(data);
		in.close();

		int ascii = 0;
		int other = 0;

		for (int i = 0; i < data.length; i++) {
			byte b = data[i];
			if (b < 0x09)
				return true;

			if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D)
				ascii++;
			else if (b >= 0x20 && b <= 0x7E)
				ascii++;
			else
				other++;
		}

		if (other == 0)
			return false;

		return 100 * other / (ascii + other) > 95;
	}

	public void copyJarResourceToFolder(JarURLConnection jarConnection, File destDir) throws IOException {
		JarFile jarFile = jarConnection.getJarFile();

		/**
		 * Iterate all entries in the jar file.
		 */
		for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements();) {

			JarEntry jarEntry = e.nextElement();
			String jarEntryName = jarEntry.getName();
			String jarConnectionEntryName = jarConnection.getEntryName();

			/**
			 * Extract files only if they match the path.
			 */
			if (jarEntryName.startsWith(jarConnectionEntryName)) {

				String filename = jarEntryName.startsWith(jarConnectionEntryName) ? jarEntryName
						.substring(jarConnectionEntryName.length()) : jarEntryName;
				File currentFile = new File(destDir, filename);

				if (jarEntry.isDirectory()) {
					currentFile.mkdirs();
				} else {
					InputStream is = jarFile.getInputStream(jarEntry);
					OutputStream out = FileUtils.openOutputStream(currentFile);
					IOUtils.copy(is, out);
					is.close();
					out.close();
				}
			}
		}

	}

}
