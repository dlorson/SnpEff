package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by a Interval
 *
 * Note: An interval does not produce any effect.
 *
 * @author pcingola
 */
public class CodonChangeInterval extends CodonChange {

	public CodonChangeInterval(Variant seqChange, Transcript transcript, VariantEffects changeEffects) {
		super(seqChange, transcript, changeEffects);
		returnNow = false; // An interval may affect more than one exon
	}

	/**
	 * Analyze
	 */
	@Override
	protected boolean codonChangeSingle(Exon exon) {
		return false;
	}

}
