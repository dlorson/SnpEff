package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.genotypes.GenotypeVector;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test cases for GenotypeVector class
 *
 * @author pcingola
 */
public class TestCasesGenotypeVector extends TestCase {

	boolean verbose = false;

	public void test_01() {
		Gpr.debug("Test");
		// Show masks (just to check they are OK)
		for (byte m : GenotypeVector.mask) {
			String line = "Mask          :" + m + "\t" + Integer.toBinaryString(m & 0xff);
			if (verbose) System.out.println(line);
		}

		for (byte m : GenotypeVector.reverseMask) {
			String line = "Reverse Mask  :" + m + "\t" + Integer.toBinaryString(m & 0xff);
			if (verbose) System.out.println(line);
		}

		for (int code = 0; code < 4; code++) {
			GenotypeVector gv = new GenotypeVector(2);

			for (int i = 0; i < 4; i++)
				gv.set(i, code);

			for (int i = 0; i < 4; i++)
				Assert.assertEquals(code, gv.get(i));
		}
	}

	public void test_02() {
		Gpr.debug("Test");
		Random rand = new Random(20121221);
		GenotypeVector gv = new GenotypeVector(1000);

		// Create random codes
		int codes[] = new int[gv.size()];
		for (int i = 0; i < gv.size(); i++) {
			int code = rand.nextInt(4);
			codes[i] = code;
			gv.set(i, code);
		}

		// Check that codes are stored OK
		for (int i = 0; i < gv.size(); i++) {
			Assert.assertEquals(codes[i], gv.get(i));
		}
	}
}
