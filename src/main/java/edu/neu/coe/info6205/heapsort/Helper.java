package edu.neu.coe.info6205.heapsort;


import java.io.PrintStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Helper class for sorting methods.
 *
 * @param <X> the underlying type (must be Comparable).
 */
public class Helper<X extends Comparable<X>> {

		// TODO also keep track of copies
		// TODO also allow for multi-neighbor-swaps/copies (as in insertion sort)

		public Helper(String description, int n, long seed) {
        this.n = n;
        this.description = description;
        this.random = new Random(seed);
    }

    public Helper(String description, int n) {
        this(description, n, System.currentTimeMillis());
    }

    public Helper(String description) {
        this(description, 0);
    }

		public X[] initialize(X[] xs) {
				compares = 0;
				swaps = 0;
				return Arrays.copyOf(xs, xs.length);
		}

		public boolean cleanup(X[] xs, PrintStream ps) {
				if (ps != null) ps.println(toString());
				final boolean sorted = sorted(xs);
				if (!sorted) Objects.requireNonNull(ps).println(Arrays.toString(xs));
				return sorted;
		}
    /**
     * Method to determine if one X value is less than another.
     *
     * @param v   the candidate element.
     * @param w   the comparand element.
     * @return true only if v is less than w.
     */
    boolean less(X v, X w) {
        compares++;
        return v.compareTo(w) < 0;
    }

		/**
		 * Method to determine if one X value is less than another.
		 *
		 * @param v the candidate element.
		 * @param w the comparand element.
		 * @return the result of v.compareTo(w).
		 */
		int compare(X v, X w) {
				compares++;
				return v.compareTo(w);
		}

		/**
		 * Swap the adjacent elements of array a at indices i-1 and i.
		 * This type of swap is guaranteed to be stable.
		 *
		 * @param xs the array.
		 * @param lo the lowest index of interest (only used for checking).
		 * @param hi one more than the highest index of interest (only used for checking).
		 * @param i  the index of the higher element to swap.
		 */
		void swap(X[] xs, int lo, int hi, int i) {
				swap(xs,i - 1, i);
		}

		/**
		 * Swap the elements of array a at indices i and j.
		 *
		 * @param xs   the array.
		 * @param lo  the lowest index of interest (only used for checking).
		 * @param hi  one more than the highest index of interest (only used for checking).
		 * @param i   one of the indices.
		 * @param j   the other index.
		 */
		void swap(X[] xs,int i, int j) {
				swaps++;
				
				X temp = xs[i];
				xs[i] = xs[j];
				xs[j] = temp;
		}

		void moveUp(X[] xs, int j) {
				swaps++; // NOTE: really this is only a half-swap
				xs[j] = xs[j - 1];
		}

		public boolean sorted(X[] xs) {
				for (int i = 1; i < xs.length; i++) if (xs[i - 1].compareTo(xs[i]) > 0) return false;
				return true;
    }

    public X[] random(int n, Class<X> clazz, Function<Random, X> f) {
        setN(n);
        @SuppressWarnings("unchecked") X[] result = (X[]) Array.newInstance(clazz, n);
        for (int i = 0; i < n; i++) result[i] = f.apply(random);
        return result;
    }

    public X[] random(Class<X> clazz, Function<Random, X> f) {
        return random(n, clazz, f);
    }

    @Override
    public String toString() {
        return "Helper for "+description+" with "+n+" elements: compares="+compares+", swaps="+swaps;
    }

    void setN(int n) {
        if (this.n == 0 || this.n == n) this.n = n;
        else throw new RuntimeException("Helper: n is already set to a different value");
    }

    private int compares = 0;
    private int swaps = 0;

    private int n;
    private final String description;
    private final Random random;
}
