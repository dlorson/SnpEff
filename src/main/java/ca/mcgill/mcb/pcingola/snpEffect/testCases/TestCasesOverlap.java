package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.binseq.DnaSequence;
import ca.mcgill.mcb.pcingola.overlap.OverlapDnaSeq;
import ca.mcgill.mcb.pcingola.util.Gpr;

public class TestCasesOverlap extends TestCase {

	/**
	 * Overlap two sequences and check results
	 */
	void checkOverlap(String seq1, String seq2, int score, int offset) {
		DnaSequence bs1 = new DnaSequence(seq1), bs2 = new DnaSequence(seq2);
		OverlapDnaSeq obs = new OverlapDnaSeq();
		obs.setMinOverlap(1); // At least 1 base overlap!
		obs.overlap(bs1, bs2);
		assertEquals(score, obs.getBestScore());
		assertEquals(offset, obs.getBestOffset());
	}

	public void test_binSeq_01() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	public void test_binSeq_02() {
		Gpr.debug("Test");
		checkOverlap("acgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	public void test_binSeq_03() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgt", 0, 0);
	}

	public void test_binSeq_04() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	public void test_binSeq_05() {
		Gpr.debug("Test");
		checkOverlap("acgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 0);
	}

	public void test_binSeq_06() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgt", 0, 0);
	}

	public void test_binSeq_07() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "ttttacgt", 0, 4);
	}

	public void test_binSeq_08() {
		Gpr.debug("Test");
		checkOverlap("ttttacgt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, -4);
	}

	public void test_binSeq_09() {
		Gpr.debug("Test");
		checkOverlap("acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", "acgttttt", 0, -60);
	}

	public void test_binSeq_10() {
		Gpr.debug("Test");
		checkOverlap("acgttttt", "acgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgtacgt", 0, 60);
	}

	// Empty sequence
	public void test_binSeq_11() {
		Gpr.debug("Test");
		checkOverlap("", "", 0, 0);
	}

	// No possible overlap (using minOverlap=1)
	public void test_binSeq_12() {
		Gpr.debug("Test");
		checkOverlap("aaaaaaaaaaaaaaaa", "tttttttttttttttt", 1, 15);
	}

	public void test_binSeq_13() {
		Gpr.debug("Test");
		checkOverlap("ttttttttttttttttttttttttttttttttaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaccccccccccccccccccccccccccccccccggggggggggggggggggggggggggggggggtttttttttttttttttttttttttttttttt", 0, -32);
	}

}
