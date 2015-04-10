package org.apodhrad.diffutils_ext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class Demo {

	// Helper method for get the file content
	private static List<String> fileToLines(String filename) {
		List<String> lines = new LinkedList<String>();
		String line = "";
		try {
			BufferedReader in = new BufferedReader(new FileReader(filename));
			while ((line = in.readLine()) != null) {
				lines.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return lines;
	}

	public static void main(String[] args) throws Exception {
		File test1 = new File("/home/apodhrad/Temp/1.java");
		File test2 = new File("/home/apodhrad/Temp/2.java");

		List<String> original = FileUtils.readLines(test1);
		List<String> revised = FileUtils.readLines(test2);

		Patch patch = DiffUtils.diff(original, revised);

		makeStandardDiff(patch);

	}

	private static void makeStandardDiff(Patch diff) {
		for (Delta delta : diff.getDeltas()) {
			System.out.print(createStandardDiff(delta));
		}
	}
	
	private static String createStandardDiff(Delta delta) {
		StringBuilder diff = new StringBuilder();
		
		diff.append(getPositionInfo(delta.getOriginal()));
		diff.append(getType(delta.getType()));
		diff.append(getPositionInfo(delta.getRevised()));
		diff.append("\n");
		
		for (Object line: delta.getOriginal().getLines()) {
			diff.append("< " + line.toString());
			diff.append("\n");
		}
		
		if (delta.getType() == Delta.TYPE.CHANGE) {
			diff.append("---");
			diff.append("\n");
		}
		
		for (Object line: delta.getRevised().getLines()) {
			diff.append("> " + line.toString());
			diff.append("\n");
		}
		
		return diff.toString();
	}
	
	private static String getPositionInfo(Chunk chunk) {
		int numberOfLines = chunk.getLines().size();
		String positionInfo = String.valueOf(chunk.getPosition() + (numberOfLines > 0 ? 1: 0));
		if (numberOfLines > 1) {
			positionInfo += "," + String.valueOf(chunk.getPosition() + numberOfLines);
		}
		return positionInfo;
	}

	private static char getType(Delta.TYPE type) {
		if (type == Delta.TYPE.INSERT) {
			return 'a';
		}
		if (type == Delta.TYPE.CHANGE) {
			return 'c';
		}
		if (type == Delta.TYPE.DELETE) {
			return 'd';
		}
		throw new IllegalArgumentException("Cannot recognized type");
	}
}
