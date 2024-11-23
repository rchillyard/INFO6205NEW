package edu.neu.coe.info6205.sort.classic;

import edu.neu.coe.info6205.sort.Classifier;
import edu.neu.coe.info6205.sort.Helper;
import edu.neu.coe.info6205.sort.SortException;
import edu.neu.coe.info6205.sort.SortWithHelper;

import java.util.function.BiFunction;

public abstract class ClassificationSorter<X extends Comparable<X>, Y> extends SortWithHelper<X> implements Classifier<X, Y> {

    public ClassificationSorter(Helper<X> helper, BiFunction<X, Y, Integer> classifier) {
        super(helper);
        this.classifier = classifier;
    }

    @Override
    public int classify(X x, Y y) {
        helper.incrementLookups();
        if (classifier != null)
            return classifier.apply(x, y);
        throw new SortException("Classifier is not set");
    }

    @Override
    public int classify(X[] xs, int i, Y y) {
        return classify(helper.get(xs, i), y);
    }

    public BiFunction<X, Y, Integer> getClassifier() {
        return classifier;
    }

    public void setClassifier(BiFunction<X, Y, Integer> classifier) {
        this.classifier = classifier;
    }

    private BiFunction<X, Y, Integer> classifier;

}