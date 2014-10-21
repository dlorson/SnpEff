package ca.mcgill.mcb.pcingola.stats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.VariantWithScore;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;

/**
 * Count for each 'type' and 'gene'.
 * Tries to avoid multiple counting by comparing to latest seqChanges.
 * WARNING: This strategy does not work if changeEffect are out of order.
 *
 * @author pcingola
 */
public class GeneCountByTypeTable implements Iterable<String>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public static int GENE_CPG_NUM_BINS = 30;
	public static boolean debug = false;

	HashSet<String> keys;
	HashSet<String> types;
	HashMap<String, CountByType> countersByType;
	HashMap<String, CountByType> counterSizeByType;
	HashMap<String, String> bioType;
	HashMap<String, Integer> sizeByType;

	public GeneCountByTypeTable() {
		keys = new HashSet<String>();
		types = new HashSet<String>();
		countersByType = new HashMap<String, CountByType>();
		counterSizeByType = new HashMap<String, CountByType>();
		bioType = new HashMap<String, String>();
		sizeByType = new HashMap<String, Integer>();
	}

	public String getBioType(String key) {
		String bio = bioType.get(key);
		return bio != null ? bio : "";
	}

	/**
	 * Get counter for this type
	 */
	public CountByType getCounter(String type) {
		CountByType counter = countersByType.get(type);

		// Lazy init counters
		if (counter == null) {
			counter = new CountByType();
			countersByType.put(type, counter);
		}

		return counter;
	}

	/**
	 * Get size counter for this type
	 */
	public CountByType getCounterSize(String type) {
		CountByType counter = counterSizeByType.get(type);

		// Lazy init counters
		if (counter == null) {
			counter = new CountByType();
			counterSizeByType.put(type, counter);
		}

		return counter;
	}

	/**
	 * Get a sorted list of keys
	 */
	public List<String> getKeyList() {
		ArrayList<String> keyList = new ArrayList<String>();
		keyList.addAll(keys);
		Collections.sort(keyList);
		return keyList;
	}

	public int getSizeByType(String key, String type) {
		Integer size = sizeByType.get(key + "\t" + type);
		return size == null ? 0 : size;
	}

	/**
	 * Get a sorted list of keys
	 */
	public List<String> getTypeList() {
		ArrayList<String> typeList = new ArrayList<String>();
		typeList.addAll(types);
		Collections.sort(typeList);
		return typeList;
	}

	@Override
	public Iterator<String> iterator() {
		return keys.iterator();
	}

	/**
	 * Sample this <gene, marker, type, seqChange> tuple to update statistics
	 */
	public void sample(Gene gene, Transcript tr, String type, VariantEffect variantEffect) {
		String key = gene.getGeneName() + "\t" + gene.getId() + "\t" + tr.getId();

		// Count
		CountByType counter = getCounter(type);
		counter.inc(key);

		// Add biotype
		if (tr.getBioType() != null) bioType.put(key, tr.getBioType());

		// Calculate the size of the intersection
		Marker marker = variantEffect.getMarker();
		Variant variant = variantEffect.getVariant();
		int start = Math.max(variant.getStart(), marker.getStart());
		int end = Math.min(variant.getEnd(), marker.getEnd());
		int size = end - start + 1;

		if (size > 0) {
			// Increment size counters (bases affected)
			getCounterSize(type).inc(key, size);

			// Set size by type
			sizeByType.put(key + "\t" + type, marker.size());

			// Add score (if any)
			if (variant instanceof VariantWithScore) {
				double score = ((VariantWithScore) variant).getScore();
				if (!Double.isNaN(score)) counter.addScore(key, score);
			}
		}

		types.add(type); // Add type
		keys.add(key); // Add key
	}
}
