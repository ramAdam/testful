/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.runner;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.utils.ElementWithKey;

/**
 * Remote class loader.
 * @author matteo
 */
public class RemoteClassLoader extends ClassLoader implements ElementWithKey<String> {

	private static Logger logger = Logger.getLogger("testful.executor");

	private static final ClassLoader superClassLoader = RemoteClassLoader.class.getClassLoader();

	/** for these classes, use always the system class loader */
	private static final String[] SYSTEM_CLASSES = {
		"testful.coverage.TrackerDatum",
		"testful.model.IClassRegistry",
	};

	/** for these classes force the use of the remote class loader */
	private static final String[] REMOTE_CLASSES = {
		// Trackers
		"testful.coverage.Tracker",
		"testful.coverage.behavior.BehaviorTracker",
		"testful.coverage.fault.FaultTracker",
		"testful.coverage.stopper.Stopper",
		"testful.coverage.stopper.Stopper$1",
		"testful.coverage.whiteBox.TrackerWhiteBox",

		// Execution Managers
		"testful.runner.Executor",
		"testful.mutation.MutationTestExecutor",
		"testful.model.executor.TestExecutor",
		"testful.coverage.CoverageTestExecutor",
		"testful.model.OperationResultTestExecutor",
		"testful.regression.TestfulTestCase$FaultTestExecutor",

		// Test Executor
		"testful.model.executor.ReflectionExecutor",

		// Utilities
		"testful.model.ClassRegistry",
		"testful.model.ClassRegistry$1", // Fix for the sun compiler
		"testful.runner.Executor",
		"testful.runner.ObjectRegistry",
	};

	/** for these packages, use always the system class loader */
	private static final String[] SYSTEM_PACKAGES = {
		"testful.model.OperationResult", // this class and its inner classes
	};

	/** for these packages force to use the remote class loader */
	private static final String[] REMOTE_PACKAGES = {
		"testful.coverage.behavior.Abstractor", // common prefix to all Abstractors
		"org.apache.commons.jexl",
	};

	public static boolean canUseSystemClassLoader(String name) {
		for(String allow : SYSTEM_CLASSES)
			if(name.equals(allow)) return true;

		for(String forbid : REMOTE_CLASSES)
			if(name.equals(forbid)) return false;

		for(String allow : SYSTEM_PACKAGES)
			if(name.startsWith(allow)) return true;

		for(String forbid : REMOTE_PACKAGES)
			if(name.startsWith(forbid)) return false;

		return true;
	}

	private static long idGenerator = 0;

	private final long id;
	private final DataFinder finder;
	private final Set<String> loaded;
	private final String key;

	public RemoteClassLoader(DataFinder finder) throws RemoteException {
		super(superClassLoader);

		id = idGenerator++;
		loaded = new HashSet<String>();

		this.finder = finder;
		key = finder.getKey();
	}

	public RemoteClassLoader getNew() throws RemoteException {
		return new RemoteClassLoader(finder);
	}

	public DataFinder getFinder() {
		return finder;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

		if(canUseSystemClassLoader(name) || loaded.contains(name)) return super.loadClass(name, resolve);

		return findClass(name);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {

		final byte[] b;
		try {

			synchronized (finder) {

				final long start = System.currentTimeMillis();
				b = finder.getData(ClassType.NAME, name);
				final long end = System.currentTimeMillis();

				time += (end - start);
			}

		} catch(RemoteException e) {
			final ClassNotFoundException exc = new ClassNotFoundException("Cannot retrieve the class " + name, e);
			logger.log(Level.WARNING, exc.getMessage(), exc);
			throw exc;
		}

		if(b == null) throw new ClassNotFoundException("Cannot find class " + name);

		Class<?> c = defineClass(name, b, 0, b.length);
		loaded.add(name);
		return c;

	}

	/** Total time used by method findClass */
	private long time;

	/**
	 * Returns the amount of time spent to load classes (and reset the counter)
	 * @return the amount of time (ms) spent to load classes
	 */
	public long getLoadingTime() {

		final long ret;
		synchronized (finder) {
			ret = time;
			time = 0;
		}

		return ret;
	}


	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + id;
	}

	@Override
	public RemoteClassLoader clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Clone not supported in RemoteClassLoader");
	}
}
