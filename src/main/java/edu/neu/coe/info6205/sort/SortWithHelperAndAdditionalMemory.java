package edu.neu.coe.info6205.sort;

import edu.neu.coe.info6205.sort.classic.ClassificationSorter;
import edu.neu.coe.info6205.util.Config;
import edu.neu.coe.info6205.util.LazyLogger;

import java.util.Comparator;
import java.util.function.BiFunction;

/**
 * Base class for Sort with a non-comparable Helper.
 *
 * @param <X> underlying type which extends Comparable.
 */
public abstract class SortWithHelperAndAdditionalMemory<X extends Comparable<X>> extends ClassificationSorter<X, Integer> implements HasAdditionalMemory {

    private int arrayMemory = -1;
    private int additionalMemory;
    private int maxMemory;

    @Override
    public void setArrayMemory(int n) {
        if (arrayMemory == -1) {
            arrayMemory = n;
            additionalMemory(n);
        }
    }

    @Override
    public void additionalMemory(int n) {
        additionalMemory += n;
        if (maxMemory < additionalMemory) maxMemory = additionalMemory;
    }

    @Override
    public Double getMemoryFactor() {
        if (arrayMemory == -1)
            throw new SortException("Array memory has not been set");
        return 1.0 * maxMemory / arrayMemory;
    }

    public SortWithHelperAndAdditionalMemory(Helper<X> helper, BiFunction<X, Integer, Integer> classifier) {
        super(helper, classifier);
    }

    public SortWithHelperAndAdditionalMemory(String description, BiFunction<X, Integer, Integer> classifier, Comparator<X> comparator, int N, int nRuns, Config config) {
        super(HelperFactory.createGeneric(description, comparator, N, nRuns, config), classifier);
    }

    /**
     * Perform initializing step for this Sort.
     *
     * @param n the number of elements to be sorted.
     */
    public void init(int n) {
        setArrayMemory(n);
        super.init(n);
    }

    public LazyLogger getLogger() {
        return SortWithHelperAndAdditionalMemory.logger;
    }

    public void close() {
        super.close();
        double memoryFactor = getMemoryFactor();
        logger.info(this + ": memory factor: " + memoryFactor);
    }

    protected boolean closeHelper = false;

    final static LazyLogger logger = new LazyLogger(SortWithHelperAndAdditionalMemory.class);

}