package org.apodhrad.diffutils_ext;

import java.io.File;
import java.io.IOException;

public class Demo {

	public static void main(String[] args) throws IOException {
		File target = new File("/home/apodhrad/target");
		File file = new File(target, "reports/diff/test.diff");
		
		System.out.println(target.toPath().relativize(file.toPath()));
	}
}
