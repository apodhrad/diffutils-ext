package org.apodhrad.diffutils_ext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

/**
 * 
 * @author apodhrad
 *
 */
public class DiffTest {

	protected File test1;
	protected File test2;
	protected File testdiff;

	public DiffTest() {
		test1 = new File(getClass().getResource("/test1.txt").getFile());
		test2 = new File(getClass().getResource("/test2.txt").getFile());
		testdiff = new File(getClass().getResource("/test.diff").getFile());
	}

	@Test
	public void constructorTest() throws Exception {
		List<String> original = FileUtils.readLines(test1);
		List<String> revised = FileUtils.readLines(test2);

		Patch patch = DiffUtils.diff(original, revised);
		Diff diff = new Diff(patch);

		Iterator<Delta> itPatch = patch.getDeltas().iterator();
		Iterator<Delta> itDiff = diff.getDeltas().iterator();

		while (itPatch.hasNext()) {
			assertTrue(itDiff.hasNext());
			assertTrue(itPatch.next().equals(itDiff.next()));
		}
	}

	@Test
	public void toStringTest() throws Exception {
		List<String> original = FileUtils.readLines(test1);
		List<String> revised = FileUtils.readLines(test2);

		Patch patch = DiffUtils.diff(original, revised);
		Diff diff = new Diff(patch);

		assertEquals(FileUtils.readFileToString(testdiff), diff.toString());
	}
}
