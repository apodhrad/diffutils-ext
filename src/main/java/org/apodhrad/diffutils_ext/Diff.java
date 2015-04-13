package org.apodhrad.diffutils_ext;

import difflib.Chunk;
import difflib.Delta;
import difflib.Patch;

/**
 * 
 * @author apodhrad
 *
 */
public class Diff extends Patch {

	public Diff(Patch patch) {
		super();
		for (Delta delta : patch.getDeltas()) {
			addDelta(delta);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Delta delta : getDeltas()) {
			result.append(getStandardDiff(delta));
		}
		return result.toString();
	}

	private static String getStandardDiff(Delta delta) {
		StringBuilder diff = new StringBuilder();

		diff.append(getPositionInfo(delta.getOriginal()));
		diff.append(getType(delta.getType()));
		diff.append(getPositionInfo(delta.getRevised()));
		diff.append("\n");

		for (Object line : delta.getOriginal().getLines()) {
			diff.append("< " + line.toString());
			diff.append("\n");
		}

		if (delta.getType() == Delta.TYPE.CHANGE) {
			diff.append("---");
			diff.append("\n");
		}

		for (Object line : delta.getRevised().getLines()) {
			diff.append("> " + line.toString());
			diff.append("\n");
		}

		return diff.toString();
	}

	private static String getPositionInfo(Chunk chunk) {
		int numberOfLines = chunk.getLines().size();
		String positionInfo = String.valueOf(chunk.getPosition() + (numberOfLines > 0 ? 1 : 0));
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
		return 'x';
	}
}
