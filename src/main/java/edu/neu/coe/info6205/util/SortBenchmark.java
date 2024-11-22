/*
  (c) Copyright 2018, 2019 Phasmid Software
 */
package edu.neu.coe.info6205.util;

import edu.neu.coe.info6205.sort.*;
import edu.neu.coe.info6205.sort.classic.BucketSort;
import edu.neu.coe.info6205.sort.counting.LSDStringSort;
import edu.neu.coe.info6205.sort.counting.MSDStringSort;
import edu.neu.coe.info6205.sort.elementary.*;
import edu.neu.coe.info6205.sort.linearithmic.TimSort;
import edu.neu.coe.info6205.sort.linearithmic.*;
import edu.neu.coe.info6205.util.SorterBenchmark;
import edu.neu.coe.info6205.util.Stopwatch;
import edu.neu.coe.info6205.util.Config;
import edu.neu.coe.info6205.util.TimeLogger;
import edu.neu.coe.info6205.util.LazyLogger;
import edu.neu.coe.info6205.util.SortBenchmarkHelper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static edu.neu.coe.info6205.sort.InstrumentedComparableHelper.AT;
import static edu.neu.coe.info6205.sort.linearithmic.MergeSort.MERGESORT;
import static edu.neu.coe.info6205.util.SortBenchmarkHelper.generateRandomLocalDateTimeArray;
import static edu.neu.coe.info6205.util.SortBenchmarkHelper.getWords;
import static edu.neu.coe.info6205.util.Utilities.formatWhole;

import edu.neu.coe.info6205.util.SorterBenchmark;
import edu.neu.coe.info6205.util.Stopwatch;

/**
 * <p>This class runs a suite of sorting benchmarks.
 * </p>
 * In order to make it work you need to do two things:
 * <ol><li>Edit config.ini</li>
 * <li>Provide command line arguments to specify the problem sizes that you want</li></ol>
 * <p>Note that each benchmark smaller than 512,000 is designed to run in approximately 10 seconds.
 * When the size equals or exceeds 512,000, that period of time will be roughly proportional to the size.</p>
 */
public class SortBenchmark {
    public static final String BENCHMARKSTRINGSORTERS = "benchmarkstringsorters";

   // public static final TimeLogger TIME_LOGGER_RAW = new TimeLogger("Raw time per run {mSec}: ", null);
    public static final TimeLogger TIME_LOGGER_NLOGN = new TimeLogger("Normalized time per run {n log n}: ", SortBenchmark::minComparisons);
    public static final TimeLogger TIME_LOGGER_N = new TimeLogger("Normalized time per run {n}: ", n -> n * 1.0);
    public static void main(String[] args) throws IOException {
        Config config = Config.load(SortBenchmark.class);
        logger.info("!!!!!!!!!!!!!!!!!!!! SortBenchmark Start !!!!!!!!!!!!!!!!!!!!\n");
        logger.info("SortBenchmark.main: version " + config.get("sortbenchmark", "version") + " with word counts: " + Arrays.toString(args));
        if (args.length == 0) logger.warn("No word counts specified on the command line");
        new SortBenchmark(config).doMain(args);
    }

    void doMain(String[] args) {
        sortStrings(getWordCounts(args));
        sortIntegers(getWordCounts(args));
    }

    public SortBenchmark(Config config) {
        this.config = config;
    }

    /**
     * Estimate an appropriate amount of total work for the given problem size.
     *
     * @param n      problem size.
     * @param config the configuration.
     * @return same as configured totalComparisons, unless n >= 32,000.
     */
    private static double getTotalWork(int n, Config config) {
        int z = config.getInt(BENCHMARKSTRINGSORTERS, "totalcomparisons", 100_000_000);
        int x = n / 512_000 + 1; // NOTE this is integer division
        return (double) z * x;
    }

    private void sortIntegers(Stream<Long> wordCounts) {
        wordCounts.forEach(this::runIntegerSorts);
    }

    private void runIntegerSorts(long N) {
        if (N > Integer.MAX_VALUE) throw new SortException("number of elements is too large");
        int n = (int) N;
        if (isConfigBenchmarkIntegerSorter("shellsort"))
            sortIntegersByShellSort(n);
        if (isConfigBenchmarkIntegerSorter("bucketsort"))
            runIntegerBucketSort(n);
        if (isConfigBenchmarkIntegerSorter("quicksort"))
            runIntegerQuickSort(n);
    }

    public void sortLocalDateTimes(final int n, Config config) throws IOException {
        logger.info("Beginning LocalDateTime sorts");
        // CONSIDER why do we have localDateTimeSupplier IN ADDITION TO localDateTimes?
        Supplier<LocalDateTime[]> localDateTimeSupplier = () -> generateRandomLocalDateTimeArray(n);
        Helper<ChronoLocalDateTime<?>> helper = new NonInstrumentingComparableHelper<>("DateTimeHelper", config);
        final LocalDateTime[] localDateTimes = generateRandomLocalDateTimeArray(n);

        // CONSIDER finding the common ground amongst these sorts and get them all working together.

        // NOTE Test on date using pure tim sort.
        if (isConfigBenchmarkDateSorter("timsort"))
            logger.info(benchmarkFactory("ProcessingSort LocalDateTimes using Arrays::sort (TimSort)", Arrays::sort, null).runFromSupplier(localDateTimeSupplier, 100) + "ms");

        // NOTE this is supposed to match the previous benchmark run exactly. I don't understand why it takes rather less time.
        if (isConfigBenchmarkDateSorter("timsort")) {
            logger.info(benchmarkFactory("Repeat ProcessingSort LocalDateTimes using timSort::mutatingSort", new TimSort<>(helper)::mutatingSort, null).runFromSupplier(localDateTimeSupplier, 100) + "ms");
            // NOTE this is intended to replace the run two lines previous. It should take the exact same amount of time.
            runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, n, 100);
        }
    }

    /**
     * Method to run string sorter benchmarks.
     * <p>
     * NOTE: this is package-private because it is used by unit tests.
     *  @param words     the word source.
     *
     * @param nWords the number of words to be sorted.
     */
    void benchmarkStringSorters(String[] words, int nWords) {
        double totalWork = getTotalWork(nWords, config);
        logger.info("benchmarkStringSorters: sorting " + formatWhole(nWords) + " words" + (config.isInstrumented() ? " and instrumented" : "") + " with total work (for estimating runs): " + totalWork);
        Random random = new Random();
        int nRunsLinearithmic = estimateRuns(minComparisons(nWords), totalWork);
        int nRunsLinear = estimateRuns(15.0 * nWords, totalWork);
        int nRunsBucket = estimateRuns(2.0 * nWords + 0.5 * nWords * nWords / BucketSort.DIGRAPHS_SIZE, totalWork);

        // System sort
        if (isConfigBenchmarkStringSorter("puresystemsort") && nRunsLinearithmic > 0)
            runPureSystemSortBenchmark(words, nWords, nRunsLinearithmic, random);

        // Linear sorts
        if (isConfigBenchmarkStringSorter("bucketsort") && nRunsBucket > 0)
            try (SortWithHelper<String> sorter = BucketSort.CaseIndependentBucketSort(BucketSort::classifyStringDigraph, BucketSort.DIGRAPHS_SIZE, nWords, config)) {
                runStringSortBenchmark(words, nWords, nRunsBucket, sorter, timeLoggersLinear);
            }

        if (isConfigBenchmarkStringSorter("LSD") && nRunsLinear > 0) {
            int nRuns = nRunsLinear * 5;
            try (SortWithHelper<String> sorter = new LSDStringSort(nWords, 20, String::compareTo, nRuns, config)) {
                runStringSortBenchmark(words, nWords, nRuns, sorter, timeLoggersLinear);
            }
        }

        if (isConfigBenchmarkStringSorter("MSD") && nRunsLinear > 0) {
            int nRuns = nRunsLinear * 5;
            try (SortWithHelper<String> sorter = new MSDStringSort(CodePointMapper.ASCIIExt, nWords, nRuns, config)) {
                runStringSortBenchmark(words, nWords, nRuns, sorter, timeLoggersLinear);
            }
//            try (SortWithHelper<String> sorter = new MSDStringSort(CodePointMapper.English, nWords, config)) {
//                runStringSortBenchmark(words, nWords, nRunsLinear * 10, sorter, timeLoggersLinear);
//            }
        }

        // Linearithmic sorts
        if (isConfigBenchmarkStringSorter("timsort") && nRunsLinearithmic > 0)
            try (SortWithHelper<String> sorter = TimSort.CaseInsensitiveSort(nWords, config)) {
                runStringSortBenchmark(words, nWords, nRunsLinearithmic * 2, sorter, timeLoggersLinearithmic);
            }

        if (isConfigBenchmarkStringSorter(MERGESORT))
            runMergeSortBenchmark(words, nWords, nRunsLinearithmic * 4, config);

        if (isConfigBenchmarkStringSorter("quicksort3way") && nRunsLinearithmic > 0)
            try (SortWithHelper<String> sorter = new QuickSort_3way<>(nWords, config)) {
                runStringSortBenchmark(words, nWords, nRunsLinearithmic * 3, sorter, timeLoggersLinearithmic);
            }

        if (isConfigBenchmarkStringSorter("quicksortDualPivot") && nRunsLinearithmic > 0)
            try (SortWithHelper<String> sorter = new QuickSort_DualPivot<>(nWords, config)) {
                runStringSortBenchmark(words, nWords, nRunsLinearithmic * 4, sorter, timeLoggersLinearithmic);
            }

        if (isConfigBenchmarkStringSorter("quicksort") && nRunsLinearithmic > 0)
            try (SortWithHelper<String> sorter = new QuickSort_Basic<>(nWords, config)) {
                runStringSortBenchmark(words, nWords, nRunsLinearithmic * 3, sorter, timeLoggersLinearithmic);
            }

        if (isConfigBenchmarkStringSorter("heapsort") && nRunsLinearithmic > 0) {
            Helper<String> helper = HelperFactory.create("Heapsort", nWords, config);
            try (SortWithHelper<String> sorter = new HeapSort<>(helper)) {
                runStringSortBenchmark(words, nWords, nRunsLinearithmic * 3, sorter, timeLoggersLinearithmic);
            }
        }

        if (isConfigBenchmarkStringSorter("introsort") && nRunsLinearithmic > 0)
            try (SortWithHelper<String> sorter = new IntroSort<>(nWords, config)) {
                runStringSortBenchmark(words, nWords, nRunsLinearithmic * 3, sorter, timeLoggersLinearithmic);
            }

        if (isConfigBenchmarkStringSorter("randomsort") && nRunsLinearithmic > 0)
            try (SortWithHelper<String> sorter = new RandomSort<>(nWords, config)) {
                runStringSortBenchmark(words, nWords, nRunsLinearithmic, sorter, timeLoggersLinearithmic);
            }
        

        if (isConfigBenchmarkStringSorter("shellsort")) {
            int nRunsSubQuadratic = estimateRuns(Math.pow(nWords, 4.0 / 3) / 2, totalWork);
            if (nRunsSubQuadratic > 0)
                try (SortWithHelper<String> sorter = new ShellSort<>(4, nWords, nRunsSubQuadratic, config)) {
                    runStringSortBenchmark(words, nWords, nRunsSubQuadratic, sorter, timeLoggersSubQuadratic);
                }
        }

        // Quadratic sorts.
        if (isConfigBenchmarkStringSorter("insertionsort") || isConfigBenchmarkStringSorter("bubblesort")) {
            int nRunsQuadratic = estimateRuns(1.25 * meanInversions(nWords), totalWork);

            if (isConfigBenchmarkStringSorter("insertionsort") && nRunsQuadratic > 0)
                try (SortWithHelper<String> sorter = new InsertionSort<>(InsertionSort.DESCRIPTION, nWords, nRunsQuadratic * 13, config)) {
                    runStringSortBenchmark(words, nWords, nRunsQuadratic * 13, sorter, timeLoggersQuadratic);
                }

            if (isConfigBenchmarkStringSorter("bubblesort") && nRunsQuadratic > 0)
                try (SortWithHelper<String> sorter = new BubbleSort<>(nWords, nRunsQuadratic * 4, config)) {
                    runStringSortBenchmark(words, nWords, nRunsQuadratic * 4, sorter, timeLoggersQuadratic);
                }
        }
    }

    private int estimateRuns(double workPerRun, double totalWork) {
        long result = Utilities.round(totalWork / workPerRun);
        if (result >= 0 && result < Integer.MAX_VALUE)
            if (result < 10_000_000)
                return (int) result;
            else
                throw new SortException("estimated number of runs is too large (max is 10 million): " + result + ". Reduce the value of totalcomparisons accordingly");
        else
            throw new RuntimeException("estimated number of runs is not a positive Integer: " + result);
    }

    private void runIntegerBucketSort(int N) {
        int bucketSize = config.getInt(BENCHMARKINTEGERSORTERS, "bucketsize", 16);
        int nRuns = config.getInt(BENCHMARKINTEGERSORTERS, "runs", 1000);
        int buckets = (N + bucketSize - 1) / bucketSize;
        BucketSort<Integer> sorter = new BucketSort<>(null, buckets, N, config);
        Helper<Integer> helper = sorter.getHelper();
        helper.init(N);
        Integer[] xs = helper.random(N, Integer.class, r -> r.nextInt(1000));
        runIntegerSortBenchmark(xs, N, nRuns, sorter, null, timeLoggersLinearithmic);
        helper.close();
    }

    private static void runPureSystemSortBenchmark(String[] words, int nWords, int nRuns, Random random) {
        Benchmark<String[]> benchmark = new Benchmark_Timer<>("SystemSort", null, Arrays::sort, null);
        doPureBenchmark(words, nWords, nRuns, random, benchmark);
    }

    private void sortIntegersByShellSort(int N) {
        int m = config.getInt(BENCHMARKINTEGERSORTERS, "mode", 5);
        int runs = config.getInt(BENCHMARKINTEGERSORTERS, "runs", 1000);
        SortWithHelper<Integer> sorter = new ShellSort<>(m, N, runs, config);
        Integer[] numbers = sorter.getHelper().random(Integer.class, Random::nextInt);
        runIntegerSortBenchmark(numbers, N, runs, sorter, sorter::preProcess, timeLoggersSubQuadratic);
    }

    private void runIntegerQuickSort(int N) {
        SortWithHelper<Integer> sorter = new QuickSort_DualPivot<>(N, config);
        Integer[] numbers = sorter.getHelper().random(Integer.class, Random::nextInt);
        int runs = config.getInt(BENCHMARKINTEGERSORTERS, "runs", 1000);
        runIntegerSortBenchmark(numbers, N, runs, sorter, sorter::preProcess, timeLoggersLinearithmic);
    }


    private void sortStrings(Stream<Long> wordCounts) {
        logger.info("Beginning String sorts");

        // NOTE: common words benchmark
//        benchmarkStringSorters(getWords("3000-common-words.txt", SortBenchmark::lineAsList), config.getInt("benchmarkstringsorters", "words", 1000), config.getInt("benchmarkstringsorters", "runs", 1000));

        // NOTE: Leipzig English words benchmarks (according to command-line arguments)
        wordCounts.forEach(this::doLeipzigBenchmarkEnglish);

        // NOTE: Leipzig Chinese words benchmarks (according to command-line arguments)
//        doLeipzigBenchmark("zho-simp-tw_web_2014_10K-sentences.txt", 5000, 1000);
    }

    private String randomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }

    private void doLeipzigBenchmarkEnglish(long N) {
        if (N > Integer.MAX_VALUE) throw new SortException("number of elements is too large");
        int x = (int) N;
        logger.info("############################### " + x + " words ###############################");
    
        // Generate random strings
        String[] words = new String[x];
        for (int i = 0; i < x; i++) {
            words[i] = randomString(10);
        }
    
        try {
            benchmarkStringSorters(words, x);
        } catch (Exception e) {
            logger.warn("Unable to run benchmark with N: " + N + " because:", e);
        }
    }
//     private void doLeipzigBenchmarkEnglish(long N) {
//         if (N > Integer.MAX_VALUE) throw new SortException("number of elements is too large");
//         int x = (int) N;
//         logger.info("############################### " + x + " words ###############################");
// //        String resource = "eng-uk_web_2002_" + (x < 50000 ? "10K" : x < 200000 ? "100K" : "1M") + "-sentences.txt";
//         String resource = "eng-uk_web_2002_" + (x < 50000 ? "10K" : "100K") + "-sentences.txt";
//         try {
//             benchmarkStringSorters(getWords(resource, SortBenchmark::getLeipzigWords), x);
//         } catch (FileNotFoundException e) {
//             logger.warn("Unable to find resource: " + resource + "because:", e);
//         } catch (Exception e) {
//             logger.warn("Unable to run benchmark with N: " + N + "because:", e);
//         }
//     }

    /**
     * Method to run a sorting benchmark, using an explicit preProcessor.
     *
     * @param words        an array of available words (to be chosen randomly).
     * @param nWords       the number of words to be sorted.
     * @param nRuns        the number of runs of the sort to be performed.
     * @param sorter       the sorter to use--NOTE that this sorter will be closed at the end of this method.
     * @param preProcessor the pre-processor function, if any.
     * @param timeLoggers  a set of timeLoggers to be used.
     */
    static void runStringSortBenchmark(String[] words, int nWords, int nRuns, SortWithHelper<String> sorter, UnaryOperator<String[]> preProcessor, TimeLogger[] timeLoggers) {
        logger.info("****************************** String sort: " + nRuns + " runs of " + nWords + " " + sorter.getDescription() + " ******************************");
        new SorterBenchmark<>(String.class, preProcessor, sorter, words, nRuns, timeLoggers).run(getDescription(nWords, sorter), nWords);
        sorter.close();
    }

    /**
     * Method to run a sorting benchmark using the standard preProcess method of the sorter.
     *
     * @param words       an array of available words (to be chosen randomly).
     * @param nWords      the number of words to be sorted.
     * @param nRuns       the number of runs of the sort to be performed.
     * @param sorter      the sorter to use--NOTE that this sorter will be closed at the end of this method.
     * @param timeLoggers a set of timeLoggers to be used.
     *                    <p>
     *                                                          NOTE: this method is public because it is referenced in a unit test of a different package
     */
    public static void runStringSortBenchmark(String[] words, int nWords, int nRuns, SortWithHelper<String> sorter, TimeLogger[] timeLoggers) {
        sorter.init(nWords);
        try (Stopwatch stopwatch = new Stopwatch()) {
            runStringSortBenchmark(words, nWords, nRuns, sorter, sorter::preProcess, timeLoggers);
            logger.info("************************************************************ (" + stopwatch.lap() / 1000.0 + " sec.)");
        }
    }

    /**
     * Method to run a sorting benchmark, using an explicit preProcessor.
     *
     * @param numbers      an array of available integers (to be chosen randomly).
     * @param n            the number of integers to be sorted.
     * @param nRuns        the number of runs of the sort to be performed.
     * @param sorter       the sorter to use--NOTE that this sorter will be closed at the end of this method.
     * @param preProcessor the pre-processor function, if any.
     * @param timeLoggers  a set of timeLoggers to be used.
     */
    static void runIntegerSortBenchmark(Integer[] numbers, int n, int nRuns, SortWithHelper<Integer> sorter, UnaryOperator<Integer[]> preProcessor, TimeLogger[] timeLoggers) {
        logger.info("****************************** Integer sort: " + n + " " + sorter.getDescription() + " ******************************");
        new SorterBenchmark<>(Integer.class, preProcessor, sorter, numbers, nRuns, timeLoggers).run(getDescription(n, sorter), n);
        sorter.close();
        logger.info("************************************************************");
    }

    public static final TimeLogger TIME_LOGGER_RAW = new TimeLogger("Raw time per run {mSec}: ", null);

    /**
     * For mergesort, the number of array accesses is actually four times the number of comparisons (six when nocopy is false).
     * That's because, in addition to each comparison, there will be approximately two copy operations.
     * Thus, in the case where comparisons are based on primitives,
     * the normalized time per run should approximate the time for one array access.
     */
    public final static TimeLogger[] timeLoggersLinearithmic = {
            TIME_LOGGER_RAW,
            new TimeLogger("Normalized time per run {n log n}: ", SortBenchmark::minComparisons)
    };

    /**
     * Linear time loggers.
     */
    public final static TimeLogger[] timeLoggersLinear = {
            TIME_LOGGER_RAW,
            new TimeLogger("Normalized time per run {n}: ", n -> n * 1.0)
    };

    /**
     * Quadratic time loggers.
     */
    final static TimeLogger[] timeLoggersQuadratic = {
            TIME_LOGGER_RAW,
            new TimeLogger("Normalized time per run {n^2}: ", SortBenchmark::meanInversions)
    };

    /**
     * For shellsort.
     */
    final static TimeLogger[] timeLoggersSubQuadratic = {
            TIME_LOGGER_RAW,
            new TimeLogger("Normalized time per run {n^(4/3)}: ", n -> Math.pow(n, 5.0 / 4))
    };


    final static LazyLogger logger = new LazyLogger(SortBenchmark.class);

    final static Pattern regexLeipzig = Pattern.compile("[~\\t]*\\t(([\\s\\p{Punct}\\uFF0C]*\\p{L}+)*)");

    /**
     * This is based on log2(n!)
     *
     * @param n the number of elements.
     * @return the minimum number of comparisons possible to sort n randomly ordered elements.
     */
    static double minComparisons(int n) {
        double lgN = Utilities.lg(n);
        return n * (lgN - LgE) + lgN / 2 + 1.33;
    }

    /**
     * This is the mean number of inversions in a randomly ordered set of n elements.
     * For insertion sort, each (low-level) swap fixes one inversion, so on average, this number of swaps is required.
     * The minimum number of comparisons is slightly higher.
     *
     * @param n the number of elements
     * @return one quarter n-squared more or less.
     */
    static double meanInversions(int n) {
        return 0.25 * n * (n - 1);
    }

    private static Collection<String> lineAsList(String line) {
        List<String> words = new ArrayList<>();
        words.add(line);
        return words;
    }

    public static Collection<String> getLeipzigWords(String line) {
        return getWords(regexLeipzig, line);
    }

    // CONSIDER: to be eliminated soon.
    private static Benchmark<LocalDateTime[]> benchmarkFactory(String description, Consumer<LocalDateTime[]> sorter, Consumer<LocalDateTime[]> checker) {
        return new Benchmark_Timer<>(
                description,
                (xs) -> Arrays.copyOf(xs, xs.length),
                sorter,
                checker
        );
    }

    private static void doPureBenchmark(String[] words, int nWords, int nRuns, Random random, Benchmark<String[]> benchmark) {
        // CONSIDER we should manage the space returned by fillRandomArray and deallocate it after use.
        final double time = benchmark.runFromSupplier(() -> Utilities.fillRandomArray(String.class, random, nWords, r -> words[r.nextInt(words.length)]), nRuns);
        for (TimeLogger timeLogger : timeLoggersLinearithmic) timeLogger.log("pure benchmark", time, nWords);
    }

    // TODO arrange for this to be resurrected.
//    private void dateSortBenchmark(Supplier<LocalDateTime[]> localDateTimeSupplier, LocalDateTime[] localDateTimes, Sort<ChronoLocalDateTime<?>> dateHuskySortSystemSort, String s, int i) {
//        logger.info(benchmarkFactory(s, dateHuskySortSystemSort::sort, dateHuskySortSystemSort::postProcess).runFromSupplier(localDateTimeSupplier, 100) + "ms");
//        // NOTE: this is intended to replace the run in the previous line. It should take the exact same amount of time.
//        runDateTimeSortBenchmark(LocalDateTime.class, localDateTimes, 100000, 100, i);
//    }

    private static Stream<Long> getWordCounts(String[] args) {
        return Arrays.stream(args).map(SortBenchmark::parseInt);
    }

    static long parseInt(String w) {
        long result = 1L;
        String expression = w.replaceAll("[gG]", "mk").replaceAll("[mM]", "kk").replaceAll("[kK]", "*1024");
        for (String split : expression.split("\\*")) result *= Integer.parseInt(split);
        return result;
    }

    private void runMergeSortBenchmark(String[] words, int nWords, int nRuns, Config config) {
        try (MergeSort<String> sorter = new MergeSort<>(nWords, nRuns, config)) {
            runStringSortBenchmark(words, nWords, nRuns, sorter, timeLoggersLinearithmic);
        }
    }

    private static <X> String getDescription(int n, SortWithHelper<X> sorter) {
        return n + AT + sorter.getDescription();
    }

    @SuppressWarnings("SameParameterValue")
    private void runDateTimeSortBenchmark(Class<?> tClass, ChronoLocalDateTime<?>[] dateTimes, int N, int m) throws IOException {
        final SortWithHelper<ChronoLocalDateTime<?>> sorter = new TimSort<>();
        logger.info("****************************** DateTime sort: " + N + " " + sorter.getDescription() + " ******************************");
        @SuppressWarnings("unchecked") final SorterBenchmark<ChronoLocalDateTime<?>> sorterBenchmark = new SorterBenchmark<>((Class<ChronoLocalDateTime<?>>) tClass, (xs) -> Arrays.copyOf(xs, xs.length), sorter, dateTimes, m, timeLoggersLinearithmic);
        sorterBenchmark.run(getDescription(N, sorter), N);
        sorter.close();
        logger.info("************************************************************");
    }

    private static final double LgE = Utilities.lg(Math.E);

    private boolean isConfigBenchmarkStringSorter(String option) {
        return isConfigBoolean(BENCHMARKSTRINGSORTERS, option);
    }

    private boolean isConfigBenchmarkMergeSort(String option) {
        return isConfigBoolean(MERGESORT, option);
    }

    private boolean isConfigBenchmarkDateSorter(String option) {
        return isConfigBoolean("benchmarkdatesorters", option);
    }

    private boolean isConfigBenchmarkIntegerSorter(String option) {
        return isConfigBoolean(BENCHMARKINTEGERSORTERS, option);
    }

    private boolean isConfigBoolean(String section, String option) {
        return config.getBoolean(section, option);
    }

    public static final String BENCHMARKINTEGERSORTERS = "benchmarkintegersorters";

    private final Config config;
}