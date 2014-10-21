package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.interval.Cds;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRand;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Test random DEL changes
 *
 * @author pcingola
 */
public class TestCasesDel extends TestCase {

	public static int N = 1000;

	boolean debug = false;
	boolean forcePositive = false || debug; // Force positive strand (used for debugging)
	boolean verbose = false || debug;

	Random rand;
	Config config;
	Genome genome;
	Chromosome chromosome;
	Gene gene;
	Transcript transcript;
	SnpEffectPredictor snpEffectPredictor;
	String chromoSequence = "";
	char chromoBases[];

	public TestCasesDel() {
		super();
		init();
	}

	/**
	 * Calculate codonsNew using a naive algorithm
	 */
	String codonsNew(Variant seqChange) {
		int cdsBaseNum = 0;
		String codonsNew = "";
		char currCodon[] = new char[3];

		boolean useCodon = false;
		currCodon[0] = currCodon[1] = currCodon[2] = ' ';
		for (Exon exon : transcript.sortedStrand()) {
			int step = exon.isStrandPlus() ? 1 : -1;
			int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

			for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEnd()); pos += step, cdsBaseNum++) {
				int cdsCodonPos = cdsBaseNum % 3;

				// Should we use this codon?
				if (seqChange.intersects(pos)) useCodon = true;
				else {
					// Should we use this base? We don't use the ones that intersect with 'seqChage' (because they are deleted)
					char base = chromoBases[pos];
					currCodon[cdsCodonPos] = exon.isStrandPlus() ? base : GprSeq.wc(base); // Update current codon
				}

				// Add it?
				if (cdsCodonPos == 2) {
					if (useCodon) codonsNew += new String(currCodon);
					useCodon = false;
					currCodon[0] = currCodon[1] = currCodon[2] = ' ';
				}
			}
		}

		// Last 'codon' might be incomplete (only one or two bases)
		if (useCodon) codonsNew += new String(currCodon);

		// Remove white spaces
		return removeWhiteSpaces(codonsNew);
	}

	/**
	 * Calculate codonsOld using a naive algorithm
	 */
	String codonsOld(Variant variant) {
		int cdsBaseNum = 0;
		String codonsOld = "";
		char currCodon[] = new char[3];

		boolean useCodon = false;
		currCodon[0] = currCodon[1] = currCodon[2] = ' ';
		for (Exon exon : transcript.sortedStrand()) {
			int step = exon.isStrandPlus() ? 1 : -1;
			int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

			for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEnd()); pos += step, cdsBaseNum++) {
				int cdsCodonPos = cdsBaseNum % 3;

				useCodon |= variant.intersects(pos); // Should we use this codon?
				char base = chromoBases[pos];
				currCodon[cdsCodonPos] = exon.isStrandPlus() ? base : GprSeq.wc(base); // Update current codon

				// Finished codon?
				if (cdsCodonPos == 2) {
					if (useCodon) codonsOld += new String(currCodon);
					useCodon = false;
					currCodon[0] = currCodon[1] = currCodon[2] = ' ';
				}
			}
		}

		// Last 'codon' might be incomplete (only one or two bases)
		if (useCodon) codonsOld += new String(currCodon);

		// Remove white spaces
		return removeWhiteSpaces(codonsOld);
	}

	void init() {
		initRand();
		initSnpEffPredictor();
	}

	void initRand() {
		rand = new Random(20100629);
	}

	void initSnpEffPredictor() {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);

		// Create factory
		int maxGeneLen = 1000;
		int maxTranscripts = 1;
		int maxExons = 5;
		SnpEffPredictorFactoryRand sepf = new SnpEffPredictorFactoryRand(config, rand, maxGeneLen, maxTranscripts, maxExons);
		sepf.setForcePositive(forcePositive);
		if (forcePositive) Gpr.debug("WARNING: Positive strand only tests!");

		// Create predictor
		snpEffectPredictor = sepf.create();
		config.setSnpEffectPredictor(snpEffectPredictor);

		// No upstream or downstream
		config.getSnpEffectPredictor().setUpDownStreamLength(0);

		// Build forest
		config.getSnpEffectPredictor().buildForest();

		// Data
		chromoSequence = sepf.getChromoSequence();
		chromoBases = chromoSequence.toCharArray();
		chromosome = sepf.getChromo();
		genome = config.getGenome();
		gene = genome.getGenes().iterator().next();
		transcript = gene.iterator().next();
	}

	/**
	 * Remove white spaces from a string.
	 * @param str
	 * @return A string without any white spaces. '-' resulting string is empty
	 */
	String removeWhiteSpaces(String str) {
		String strNoWs = "";
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != ' ') strNoWs += str.charAt(i);
		}

		return strNoWs;
	}

	public void test_01() {
		Gpr.debug("Test");
		CodonTable codonTable = genome.codonTable();

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Create a random Insert at each position
		//	- Calculate effect
		for (int i = 0; i < N; i++) {

			initSnpEffPredictor();
			if (debug) System.out.println("DEL Test iteration: " + i + "\n" + transcript);
			else if (verbose) System.out.println("DEL Test iteration: " + i + "\t" + transcript.cds());
			else Gpr.showMark(i + 1, 1);
			int cdsBaseNum = 0;

			String transcriptStr = transcript.toString() + "\n" + transcript.toStringAsciiArt();

			// For each exon...
			for (Exon exon : transcript.sortedStrand()) {
				int step = exon.isStrandPlus() ? 1 : -1;
				int beg = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

				// For each base in this exon...
				for (int pos = beg; (pos >= exon.getStart()) && (pos <= exon.getEnd()); pos += step, cdsBaseNum++) {
					//---
					// Create variant
					//---

					// Get a random base different from 'refBase'
					int delLen = rand.nextInt(10) + 1;

					int start = pos;
					int end = pos + delLen;
					if (transcript.isStrandMinus()) {
						start = pos - delLen;
						end = pos;
					}

					if (start < 0) start = 0;
					if (end > chromosome.getEnd()) end = chromosome.getEnd();
					delLen = end - start + 1;

					String delPlus = chromoSequence.substring(start, end + 1); // Deletion (plus strand)
					String del = delPlus;

					// Codon number
					int cdsCodonNum = cdsBaseNum / 3;
					int cdsCodonPos = cdsBaseNum % 3;

					// Create a SeqChange
					Variant variant = new Variant(chromosome, start, "", "-" + del, "");

					// Sanity checks
					Assert.assertEquals(true, variant.isDel()); // Is it a deletion?
					Assert.assertEquals(del.length(), variant.size()); // Does seqChange have the correct size?

					//---
					// Expected Effect
					//---
					String effectExpected = "", aaExpected = "";
					String codonsOld = codonsOld(variant);
					codonsOld = codonsOld.toUpperCase();
					String aaOld = codonTable.aa(codonsOld);
					String codonsNew = codonsNew(variant);
					String aaNew = codonTable.aa(codonsNew);

					// Net change
					String netChange = "";
					for (Exon ex : transcript.sortedStrand())
						netChange += variant.netChange(ex);

					// Replace empty by '-'
					if (codonsOld.isEmpty()) codonsOld = "-";
					if (codonsNew.isEmpty()) codonsNew = "-";
					if (aaOld.isEmpty()) aaOld = "-";
					if (aaNew.isEmpty()) aaNew = "-";

					if (variant.includes(exon)) effectExpected = "EXON_DELETED";
					else if (netChange.length() % 3 != 0) {
						effectExpected = "FRAME_SHIFT";
						aaExpected = "(" + aaOld + "/" + "-" + ")";
					} else {
						if (cdsCodonPos == 0) {
							effectExpected = "CODON_DELETION";
							aaExpected = "(" + aaOld + "/-)";
						} else {
							if (codonsOld.startsWith(codonsNew) || (codonsNew.equals("-"))) {
								effectExpected = "CODON_DELETION";
								aaExpected = "(" + aaOld + "/" + aaNew + ")";
							} else {
								effectExpected = "CODON_CHANGE_PLUS_CODON_DELETION";
								aaExpected = "(" + aaOld + "/" + aaNew + ")";
							}
						}

						if ((cdsCodonNum == 0) && codonTable.isStartFirst(codonsOld) && !codonTable.isStartFirst(codonsNew)) {
							effectExpected = "START_LOST";
							aaExpected = "(" + aaOld + "/" + aaNew + ")";
						} else if ((aaOld.indexOf('*') >= 0) && (aaNew.indexOf('*') < 0)) {
							effectExpected = "STOP_LOST";
							aaExpected = "(" + aaOld + "/" + aaNew + ")";
						} else if ((aaNew.indexOf('*') >= 0) && (aaOld.indexOf('*') < 0)) {
							effectExpected = "STOP_GAINED";
							aaExpected = "(" + aaOld + "/" + aaNew + ")";
						}
					}

					//---
					// Filter out some effects
					//---
					VariantEffects effectsAll = snpEffectPredictor.variantEffect(variant);
					VariantEffects effects = new VariantEffects(variant);
					for (VariantEffect eff : effectsAll) {
						boolean copy = true;

						if (eff.getEffectType() == EffectType.SPLICE_SITE_ACCEPTOR) copy = false;
						if (eff.getEffectType() == EffectType.SPLICE_SITE_DONOR) copy = false;
						if (eff.getEffectType() == EffectType.INTRON) copy = false;

						if (copy) effects.addEffect(eff);
					}

					// There should be only one effect in most cases
					Assert.assertEquals(false, effects.isEmpty()); // There should be at least one effect
					if (debug && (effects.size() > 1)) {
						System.out.println("Found more than one effect: " + effects.size() + "\n" + transcript);
						System.out.println("\tEffects: ");
						for (VariantEffect eff : effects)
							System.out.println("\t" + eff);
					}

					//---
					// Check effects
					//---
					boolean ok = false;
					for (VariantEffect effect : effects) {
						String effStr = effect.effect(true, false, true, false);
						String effFullStr = effect.effect(true, true, true, false);

						String line = "\tIteration: " + i + "\tPos: " + pos + "\tExpected: '" + effectExpected + "'\tEffect: '" + effStr + "'";
						StringBuilder msg = new StringBuilder();
						msg.append(line + "\n");
						msg.append("\tIteration: " + i);
						msg.append("\tPos: " + pos);
						msg.append("\n\t\tCDS base [codon] : " + cdsBaseNum + " [" + cdsCodonNum + ":" + cdsCodonPos + "]");
						msg.append("\n\t\tVariant          : " + variant + "\tsize: " + variant.size() + "\tdelPlus: " + delPlus);
						msg.append("\n\t\tNetCdsChange     : " + netChange);
						msg.append("\n\t\tEffect expected  : " + effectExpected);
						msg.append("\n\t\tEffect           : " + effStr + "\t" + effFullStr);
						msg.append("\n\t\tAA expected      : '" + aaOld + "' / '" + aaNew + "'\t" + aaExpected);
						msg.append("\n\t\tAA               : '" + effect.getAaOld() + "' / '" + effect.getAaNew() + "'");
						msg.append("\n\t\tCodon expected   : '" + codonsOld + "' / '" + codonsNew + "'");
						msg.append("\n\t\tCodons           : '" + effect.getCodonsOld().toUpperCase() + "' / '" + effect.getCodonsNew().toUpperCase() + "'");
						msg.append("\n\nTranscript:\n" + transcriptStr + "\n");
						msg.append(line + "\n");

						if (debug) System.out.println(msg);
						else if (verbose) System.out.println(line);

						for (String e : effStr.split("\\+")) {
							if (effectExpected.equals(e)) {
								ok = true;
								// Check codons
								if ((effect.getEffectType() != EffectType.FRAME_SHIFT) // No codons in 'FRAME_SHIFT'
										&& (effect.getEffectType() != EffectType.EXON_DELETED) // No codons in 'EXON_DELETED'
										&& (effect.getEffectType() != EffectType.SPLICE_SITE_REGION) // No codons in 'SPLICE_SITE_REGION'
										&& (effect.getEffectType() != EffectType.INTERGENIC) // No codons in 'INTERGENIC'
								) {
									if (codonsNew.equals("-")) codonsNew = "";

									String codonsNewEff = effect.getCodonsNew().toUpperCase();
									if (codonsNewEff.equals("-")) codonsNewEff = "";

									Assert.assertTrue(msg.toString(), codonsOld.equals(effect.getCodonsOld().toUpperCase())); // Check codons old
									Assert.assertTrue(msg.toString(), codonsNew.equals(codonsNewEff)); // Check codons new
								}
							}
						}
					}
					Assert.assertEquals(true, ok);
				}
			}
		}
	}

	public void test_02() {
		//---
		// Create a transcript
		//---
		String chrSeq = "CGTGTTACCAAGCATTTGGGAACCGGAATTCTACGCTGGAGTTCGCCTAACAGCTAAAGCTCAAAACAGGAGTGGTTTAGTTCACAGGCAGACCTTTAACGGTACCTACCTTAATCCGGTCAGATTTAAATTCATGATAGATGGTCTAAGTCATGCACTACTCCTACCAGTTTTGGACGTGGCGCATTGGGCCCCGACACCAAATGCGTTGGAGCGACAGCACAAACGATATCTCAGATTAAGTGCATTGAAGGCCTTCTCACGGATAGATCTATAGGTCGCTGTGTGTCGGGAAGTCTTTCATCCGCTCGGGAGTCGGGTAAATTCTTTCTTCCAGTTGTGTGTTCTACGACTGCAGAGGACAGCTCTCGCTTAGTTTCCTTCCGTTCTTTGTAAGGCCCGATAGGAATACCCGCATAATTATCCCCACAGTCTAACTTGATCCACGTCATATGGGGCGATGGCTGGAATGAGGGGGGTTCAGTGGCAGACCACGATATCTCTGTTTATCTGATGAAGAATTTTGGATACCCCAATAACTAATGTGAGAAGAGTGTGGGGTATCCAACGTGATTGGAGGGAAGTATGGAAACGAATGAAGACAAGTTCGACAAGCCGTGGTAACGATCATATGCCACCGTATTCTAACGGGGGGGGGCCGCTGTTTGAATGCCAGATAGATATTTAAAGAGGATGGCCGCCCCCCAATGGGCACGAGGAACTGACCAGAGGTAGGTATGATGGTGTGAGAGAGAGATGTAGTGTAGTTACTATAAAGATTTGAAGATCGAAAGGCTTAGTGATACGAAACCTCCCGCTACTGCGGGCCGGTATAGCAGGTCTAATACTTGGTGATAGGGCTACATCGAGCCTCGTTCGACAAGAATACCCCAGTTGCAATGGGGACCGATTCAGTCTGGGGCTCTGTGGTCCGACATTATGCCGGAATGCCTAGTATGGGAATTAAAACGATTAATATTACCTCTGGGACTATCTTTGTGCCAAACTCTACTGAGAGAGGAACGTAATCGTATTAGGGGCCAAGATAGTTTACTCCGATCCACCGTAACACTTATTTTGGGCCGTGCGTACCCACCCACGGTCATGAACGCAAACTGGATAGGGCGCGTCATACGAGCTAAAGTGTTCTCTTGGGGCAAATAAGAAGGGACTAGCTGTATTCCCCTCCCATTGTGTGGGTACAATTCGGGGGAAAGGTGTCGAGCCCAAATGCTACGGCTCGTAGATATCAGGTCCTTTAGCCGCCCGTATGTAAGCACCTCCAACGTTTTGGAGGAAGATCCCTCTCCCACGGGTTATACCATTAATGACTCCGCACTAACAATATCTTACCAGGGCCCGCAAATACCACACCGGGATACTGGAATGAATTATTCTCCGACCCATTTGATGAAACGTATGGGAACTTTCCTGTTGTATTAAACCCGCGGATACACTCTCTTGCCCACGAGTAGCCTCTACTTACTAGATTGAGAGGTACACTACGCCATTATGAAGCATTTCGACTTTCACTCCAGTATATAGAAGGTATGTGTGGATCCTTCAAAATAGTGATCCGAACCTGGTTGTAGGGGCCACCGAAATTCCGATTACTGAAGATTAATGTTTTCAAATGCCCTATTTCACTGGTGATAGCGATGGAGCCGGGCTATCATCATGGCGAAGCACGTGGGAAAGCATTCCGTCAAGGCTAGTGGGAACCTCTGCCTTGCCATGTACGCGTTCTATATACACGAACATCGATACCGGTCTCGTCCTGGGGTAGAGCCATCCTCATCACGAGTGATGTAGGACGGCTAGCTCATTAATTCACTGCGTGTCAGATAGAATGTCTTCGTGCGCAAAAATTGTTTAGGAGACCGTCGGCGCTCCCTTGGAGAGATGCCCAACGTAGAGAGGCAATCCCCGGCGTATTGGTGGTTTCTGGAACGGAACGAGACTCTTTGTTGCGTCGACTCCCGGCAACACCGGCCCCCGCCG";
		Genome genome = new Genome("test");

		Chromosome chr = new Chromosome(genome, 0, chrSeq.length() - 1, "1");
		chr.setSequence(chrSeq);
		genome.add(chr);

		Gene gene = new Gene(chr, 333, 408, true, "gene1", "gene1", "protein_coding");

		Transcript tr = new Transcript(gene, 333, 408, true, "tr1");
		gene.add(tr);
		tr.setProteinCoding(true);

		Exon e1 = new Exon(tr, 333, 334, true, "exon1", 0);
		Exon e2 = new Exon(tr, 353, 364, true, "exon2", 0);
		Exon e3 = new Exon(tr, 388, 398, true, "exon3", 0);
		Exon e4 = new Exon(tr, 404, 408, true, "exon4", 0);
		Exon exons[] = { e1, e2, e3, e4 };

		for (Exon e : exons) {
			String seq = chrSeq.substring(e.getStart(), e.getEnd() + 1);
			seq = GprSeq.reverseWc(seq);
			e.setSequence(seq);
			tr.add(e);

			Cds cds = new Cds(tr, e.getStart(), e.getEnd(), e.isStrandMinus(), "");
			tr.add(cds);
		}
		tr.rankExons();

		if (verbose) System.out.println("Transcript:\n" + tr);
		Assert.assertEquals("FLPYKAVLCR", tr.protein());

		//---
		// Create variant
		//---
		Variant var = new Variant(chr, 397, "GCCCGATAGGA", "", "");
		if (verbose) System.out.println("Variant: " + var);
		Assert.assertEquals("chr1:397_GCCCGATAGGA/", var.toString());

		//---
		// Calculate effects
		//---
		snpEffectPredictor = new SnpEffectPredictor(genome);
		snpEffectPredictor.setUpDownStreamLength(0);
		snpEffectPredictor.add(gene);
		snpEffectPredictor.buildForest();

		VariantEffects effectsAll = snpEffectPredictor.variantEffect(var);
		for (VariantEffect eff : effectsAll) {
			if (eff.getEffectType() == EffectType.CODON_CHANGE_PLUS_CODON_DELETION) {
				if (verbose) System.out.println("\t" + eff.getEffectTypeString(false) + "\t" + eff.getCodonsOld() + "\t" + eff.getCodonsNew());
				Assert.assertEquals("TCT", eff.getCodonsNew().toUpperCase());
			}
		}
	}
}
