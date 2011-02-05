package testful.mutation;

import java.lang.reflect.Field;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestfulException;
import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Test;
import testful.model.executor.ReflectionExecutor;
import testful.runner.Context;
import testful.runner.DataFinder;
import testful.runner.ExecutionManager;
import testful.runner.TestfulClassLoader;
import testful.utils.Cloner;

/**
 * This is a fake execution manager: it is able to manage "real" mutant execution managers,
 * giving to each of them a mutant to evaluate. Users must use this class to evaluate mutants.
 *
 * @author matteo
 *
 */
public class MutationExecutionManager extends ExecutionManager<MutationCoverage> {

	private static Logger logger = Logger.getLogger("testful.mutation");

	public static Context<MutationCoverage, MutationExecutionManager> getContext(DataFinder finder, Test test, TrackerDatum ... data) {
		return new Context<MutationCoverage, MutationExecutionManager>(MutationExecutionManager.class, finder, ReflectionExecutor.class, test, true, data);
	}

	private static ClassLoader classLoader = MutationExecutionManager.class.getClassLoader();

	/** tracker data. trackerData[0] contains information about the mutation to run */
	private TrackerDatum[] trackerData;

	/** classes to mutate */
	private final String[] classes;

	/** the result */
	private final MutationCoverage coverage;

	/** the configuration class */
	private Class<?> config;

	public MutationExecutionManager(byte[] executorSer, byte[] trackerDataSer, boolean reloadClasses) throws TestfulException {
		super(executorSer, trackerDataSer, reloadClasses);

		TrackerDatum[] trackerDataTmp = (TrackerDatum[]) Cloner.deserialize(trackerDataSer, true);

		int i = 1;
		trackerData = new TrackerDatum[trackerDataTmp.length+1];
		for(TrackerDatum datum : trackerDataTmp) trackerData[i++] = datum;

		coverage = new MutationCoverage();

		try {
			config = classLoader.loadClass(Utils.CONFIG_CLASS);
		} catch(ClassNotFoundException e) {
			throw new TestfulException(e);
		}

		Set<String> classes = new HashSet<String>();
		for(Field f : config.getFields()) {
			if(f.getName().startsWith(Utils.CUR_MUTATION_PREFIX)) {
				String m = f.getName();
				String n = Utils.decodeClassName(m.substring(Utils.CUR_MUTATION_PREFIX.length(), m.length()-Utils.CUR_MUTATION_SUFFIX.length()));
				classes.add(n);
			}
		}
		this.classes = classes.toArray(new String[classes.size()]);
	}

	@Override
	protected MutationCoverage getResult() {
		return coverage;
	}

	@Override
	protected void setup() throws ClassNotFoundException { }

	@Override
	protected void reallyExecute(boolean stopOnBug)  {

		for(String className : classes) {
			logger.fine("Applying mutation analysis on " + className);
			MutationCoverageSingle singleCov = executeMutantsOnSingleClass(stopOnBug, className);

			if(singleCov != null)
				coverage.add(className, singleCov);
		}
	}

	private MutationCoverageSingle executeMutantsOnSingleClass(boolean stopOnBug, String className) {
		try {
			Field mutationField = config.getField(Utils.getCurField(className));
			Integer maxMutations = (Integer) config.getMethod(Utils.getMaxField(className)).invoke(null);
			Class<?> clazz = classLoader.loadClass(className);
			Field executedMutantsField = clazz.getField(Utils.EXECUTED_MUTANTS);

			MutationCoverageSingle coverage = null;
			ReflectionExecutor reflectionExecutor = (ReflectionExecutor) executor;
			Operation[] test = reflectionExecutor.getTest();

			// run the original class (1st time) && save the execution time
			mutationField.set(null, 0);
			super.reallyExecute(stopOnBug);
			logger.fine("Run test (" + test.length + ") on the original class " + className + ": " + executionTime);

			if(faults == 0) {
				// re-run the test, tracking operation status
				OperationResult.insert(test);
				super.reallyExecute(stopOnBug);
				logger.fine("Tracked operation status on " + className);

				// re-run the test, verifying operation status
				OperationResult.Verifier.insertOperationResultVerifier(test);
				super.reallyExecute(stopOnBug);
				logger.fine("Verified operation status on " + className);
			}

			if(faults > 0) {
				// the class contains some errors, revealed by the test
				logger.warning("The test reveals some errors in the class " + className + ": skipping mutation analysis!");
				return null;
			}

			final long originalExecutionTime = executionTime;

			// get the live mutant set
			mutationField.set(null, -1);
			super.reallyExecute(stopOnBug);

			BitSet executedMutants;
			try {
				executedMutants = (BitSet) executedMutantsField.get(null);
				logger.fine("Got executed mutants on " + className + ": " + executedMutants.cardinality() + "/" + maxMutations);
			} catch (Throwable e1) {
				executedMutants = null;
				logger.log(Level.FINE, "Cannot retrieve the list of executed mutants: " + e1, e1);
			}

			BitSet notExecutedMutants = new BitSet();
			if(executedMutants == null) notExecutedMutants.set(1, maxMutations + 1);
			else {
				notExecutedMutants.or(executedMutants);
				notExecutedMutants.flip(1, maxMutations + 1);
			}

			coverage = new MutationCoverageSingle(notExecutedMutants);
			final long maxExecutionTime = 5 * (originalExecutionTime) + 500;
			logger.fine("Set maximum execution time to " + maxExecutionTime + " (" + originalExecutionTime + ")");

			for(int mutation = executedMutants.nextSetBit(0); mutation >= 0; mutation = executedMutants.nextSetBit(mutation + 1)) {
				try {
					TestfulClassLoader loader = (TestfulClassLoader) classLoader;
					if(reloadClasses) loader = loader.getNew();

					//TODO: use the "new" class loader
					trackerData[0] = new MutationExecutionData(className, mutation, maxExecutionTime);
					MutationExecutionManagerSingle em = new MutationExecutionManagerSingle(executor, trackerData, false);

					long executionTime = em.execute(stopOnBug);
					if(executionTime < 0) {
						if(executionTime != MutationExecutionManagerSingle.ERROR_EXECUTION) {
							coverage.setKilled(mutation);
							logger.fine("Killed mutant " + mutation);
						} else {
							logger.fine("Error while executing mutant " + mutation);
						}
					} else {
						coverage.setAlive(mutation, executionTime);
						logger.fine("Alive mutant " + mutation);
					}

				} catch(Exception e) {
					logger.log(Level.WARNING, "Error executing mutant " + mutation + ": " + e.getMessage(), e);
				}

			}

			return coverage;

		} catch(Exception e) {
			logger.log(Level.WARNING, "Error executing the mutant: " + e.getMessage(), e);
			return null;
		}
	}
}
