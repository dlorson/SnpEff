package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.VariantFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.VariantTxtFileIterator;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGtf22;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 *
 * Test cases used:
 *
   Transcript: ENST00000250823

		Y   16167997    UPSTREAM
		Y   16168096    UPSTREAM
		Y   16168097    5PRIME_UTR
		Y   16168169    5PRIME_UTR
		Y   16168170    EXON
		Y   16168271    EXON
		Y   16168272    INTRON
		Y   16168463    INTRON
		Y   16168464    EXON
		Y   16168739    EXON
		Y   16168740    3PRIME_UTR
		Y   16168838    3PRIME_UTR
		Y   16168839    DOWNSTREAM
		Y   16168940    DOWNSTREAM

		Note: Coordinates in the following diagram are 16160000 + X

		7997       8096               8170       8271               8464       8739               8839       8938
		|-----UP------||-----5'------||-----EX------||-----IN------||-----EX------||-----3'------||-----DO------|
		7997           8097       8169               8272       8463               8740       8838

 *
 * @author pcingola
 */
public class TestCasesVariant extends TestCase {

	boolean verbose = false;
	long randSeed = 20100629;
	String genomeName = "testCase";

	public TestCasesVariant() {
		super();
	}

	/**
	 * CDS test (CDS = CoDing Sequences)
	 * Build CDS form exon sequences
	 */
	public void test_08() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.initSnpEffPredictor();

		// Read CDS (hg37, chromosome Y) from a file and store them indexed by transcript ID
		HashMap<String, String> cdsByTrId = new HashMap<String, String>();
		String cdsY = Gpr.readFile("./tests/cds_hg37_chrY.txt");
		String lines[] = cdsY.split("\n");
		for (String line : lines) {
			String recs[] = line.split("\t");
			cdsByTrId.put(recs[0], recs[1]);
		}

		// Calculate CDS from chromosome Y and compare
		int totalOk = 0;
		for (Gene gint : comp.config.getGenome().getGenes()) {
			for (Transcript tint : gint) {
				String seqOri = cdsByTrId.get(tint.getId());

				if (seqOri != null) {
					String seq = tint.cds();
					// Compare CDS sequences
					if (!seqOri.equalsIgnoreCase(seq)) throw new RuntimeException("CDS do not match:\nTranscipt:" + tint.getId() + " " + tint.isStrandMinus() + "\n\t" + seq + "\n\t" + seqOri + "\n");
					else {
						if (verbose) System.out.println("CDS compare:\n\t" + seqOri + "\n\t" + seq);
						totalOk++;
					}
				}
			}
		}
		if (totalOk == 0) throw new RuntimeException("No sequences compared!");
	}

	/**
	 * Test SNP effect predictor for a transcript
	 */
	public void test_09() {
		String trId = "ENST00000250823";
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.setUseAaNoNum(true);
		comp.snpEffect("tests/" + trId + ".out", trId, true);
	}

	/**
	 * Test SNP effect predictor: Test UTR distances, Up/Downstream distances
	 */
	public void test_11() {
		String trId = "ENST00000250823";
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.setUseAaNoNum(true);
		comp.snpEffect("tests/" + trId + "_all.out", trId, false);
	}

	/**
	 * Test SNP effect predictor: Test Splice sites
	 */
	public void test_12() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/splice_site.out", null, true);
	}

	/**
	 * Test SNP effect predictor: Test Splice sites (make sure they are only 2 bases long)
	 */
	public void test_12_2() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/splice_site_2.out", null, true);
	}

	/**
	 * Test SNP effect predictor: Test start codon gained
	 */
	public void test_19() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		String trId = "ENST00000439108";
		comp.snpEffect("tests/" + trId + ".snps", trId, true);
	}

	/**
	 * Test SNP effect predictor: Test start codon gained (reverse strand)
	 */
	public void test_20() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		String trId = "ENST00000382673";
		comp.snpEffect("tests/" + trId + ".snps", trId, true);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_21() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		String trId = "ENST00000250823";
		comp.snpEffect("tests/" + trId + "_InDels.out", trId, true);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_21_2() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		String trId = "ENST00000250823";
		comp.snpEffect("tests/" + trId + "_InDels_2.out", trId, true);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_21_3() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		String trId = "ENST00000250823";
		comp.setUseAaNoNum(true);
		comp.snpEffect("tests/" + trId + "_InDels_3.out", trId, true);
	}

	/**
	 * Read file test: Should throw an exception (chromosome not found)
	 */
	public void test_22() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);

		VariantFileIterator snpFileIterator;
		snpFileIterator = new VariantTxtFileIterator("tests/chr_not_found.out", comp.config.getGenome());
		snpFileIterator.setIgnoreChromosomeErrors(false);

		boolean trown = false;
		try {
			// Read all SNPs from file. Note: This should throw an exception "Chromosome not found"
			for (Variant variant : snpFileIterator) {
				Gpr.debug(variant);
			}
		} catch (RuntimeException e) {
			trown = true;
			String expectedMessage = "ERROR: Chromosome 'chrZ' not found! File 'tests/chr_not_found.out', line 1";
			if (e.getMessage().equals(expectedMessage)) ; // OK
			else throw new RuntimeException("This is not the exception I was expecting!\n\tExpected message: '" + expectedMessage + "'\n\tMessage: '" + e.getMessage() + "'", e);
		}

		// If no exception => error
		if (!trown) throw new RuntimeException("This should have thown an exception 'Chromosome not found!' but it didn't");
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_23_MNP_on_exon_edge() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		String trId = "ENST00000250823";
		comp.setUseAaNoNum(true);
		comp.snpEffect("tests/" + trId + "_mnp_out_of_exon.txt", trId, true);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_24_delete_exon_utr() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/delete_exon_utr.txt", null, true);
	}

	public void test_25_exon_bases() {
		Gpr.debug("Test");
		System.out.println("Loading config file");
		Config config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		System.out.println("Loading fasta file");
		String fastaFile = "tests/testCase.fa";
		String seq = GprSeq.fastaSimpleRead(fastaFile);

		// Test all bases in all exons
		int countOk = 0, countErr = 0;
		for (Gene gint : config.getGenome().getGenes()) {
			for (Transcript tr : gint) {
				if (verbose) System.out.println("Transcript: " + tr.getId());
				List<Exon> exons = tr.sortedStrand();
				for (Exon exon : exons) {
					for (int i = exon.getStart(); i <= exon.getEnd(); i++) {
						String base = seq.substring(i, i + 1);
						String exonBase = exon.basesAt(i - exon.getStart(), 1);

						if (base.equalsIgnoreCase(exonBase)) {
							countOk++;
						} else {
							countErr++;
							String msg = "ERROR:\tPosition: " + i + "\tExpected: " + base + "\tGot: " + exonBase;
							if (verbose) Gpr.debug(msg);
							throw new RuntimeException(msg);
						}
					}
				}
			}
		}

		System.out.println("Count OK: " + countOk + "\tCount Err: " + countErr);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_26_chr15_78909452() {
		Gpr.debug("Test");
		String genomeName = "testHg3761Chr15";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/chr15_78909452.txt", null, true);
	}

	/**
	 * Splice site: Bug reported by Wang, Xusheng
	 */
	public void test_28_Splice_mm37_ENSMUSG00000005763() {
		Gpr.debug("Test");
		//---
		// Build snpEffect
		//---
		String gtfFile = "tests/ENSMUSG00000005763.gtf";
		String genome = "testMm37.61";

		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGtf22 fgtf22 = new SnpEffPredictorFactoryGtf22(config);
		fgtf22.setFileName(gtfFile);
		fgtf22.setReadSequences(false); // Don't read sequences
		SnpEffectPredictor snpEffectPredictor = fgtf22.create();
		config.setSnpEffectPredictor(snpEffectPredictor);

		// Set chromosome size (so that we don't get an exception)
		for (Chromosome chr : config.getGenome())
			chr.setEnd(1000000000);

		//---
		// Calculate effect
		//---
		CompareEffects comp = new CompareEffects(snpEffectPredictor, randSeed, verbose);
		comp.snpEffect("tests/ENSMUSG00000005763.out", null, true);
	}

	/**
	 * Test effect when hits a gene, but not any transcript within a gene.
	 * This is an extremely weird case, might be an annotation problem.
	 */
	public void test_29_Intergenic_in_Gene() {
		Gpr.debug("Test");
		String genomeName = "testHg3763Chr20";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/warren.eff.missing.chr20.txt", null, true);
	}

	/**
	 * Rare Amino acid
	 */
	public void test_30_RareAa() {
		Gpr.debug("Test");
		String genomeName = "testHg3765Chr22";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/rareAa.txt", null, true);
	}

	/**
	 * MT chromo
	 */
	public void test_31_CodonTable() {
		Gpr.debug("Test");
		String genomeName = "testHg3767Chr21Mt";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/mt.txt", null, true);
	}

	/**
	 * Start gained
	 */
	public void test_32_StartGained() {
		Gpr.debug("Test");
		String genomeName = "testHg3769Chr12";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/start_gained_test.txt", null, true);
	}

	/**
	 * Not start gained
	 */
	public void test_33_StartGained_NOT() {
		Gpr.debug("Test");
		String genomeName = "testHg3769Chr12";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffectNegate("tests/start_gained_NOT_test.txt", null, true);
	}

	/**
	 * Start gained
	 */
	public void test_34_StartGained() {
		Gpr.debug("Test");
		String genomeName = "testHg3766Chr1";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/start_gained_test_2.txt", null, true);
	}

	/**
	 * Not start gained
	 */
	public void test_35_StartGained_NOT() {
		Gpr.debug("Test");
		String genomeName = "testHg3766Chr1";
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffectNegate("tests/start_gained_NOT_test_2.txt", null, true);
	}

	/**
	 * Make sure all variant effects have appropriate impacts
	 */
	public void test_36_EffectImpact() {
		Gpr.debug("Test");
		Chromosome chr = new Chromosome(null, 0, 1, "1");
		Variant var = new Variant(chr, 1, "A", "C");
		var.setVariantType(VariantType.SNP);

		System.out.println(var);
		for (EffectType eff : EffectType.values()) {
			VariantEffect varEff = new VariantEffect(var);
			varEff.setEffectType(eff);
			if (verbose) System.out.println(var.isVariant() + "\t" + eff + "\t" + varEff.getEffectImpact());
		}
	}

	/**
	 * Make sure all effect_tpyes have appropriate impacts, regions, etc.
	 */
	public void test_37_EffectType() {
		for (EffectType eff : EffectType.values()) {
			if (verbose) System.out.println("\t" + eff);

			// None of these should throw an exception
			eff.effectImpact();
			eff.getGeneRegion();
			eff.toSequenceOntology();
		}
	}

	/**
	 * Test
	 */
	public void test_zzz() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
		comp.snpEffect("tests/z.out", "ENST00000250823", true);
	}

}
