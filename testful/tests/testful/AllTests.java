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

package testful;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Runs all valuable tests (excluding long random tests).
 * @author matteo
 */
public class AllTests {

	/** If true, also run AutoTests (Random) */
	public static final boolean AUTO = false;

	public static Test suite() {
		TestFul.setupLogging(GenericTestCase.getConfig());

		TestSuite suite = new TestSuite("Test for Testful");

		suite.addTest(testful.model.AllTests.suite());
		suite.addTest(testful.coverage.AllTests.suite());
		suite.addTest(testful.regression.AllTests.suite());
		suite.addTest(testful.runner.AllTests.suite());

		return refine(suite);
	}

	public static void main(String[] args) {
		TestRunner runner = new TestRunner();
		TestResult result = runner.doRun(suite(), false);

		if (! result.wasSuccessful())
			System.exit(1);
	}

	@SuppressWarnings("unchecked")
	private static Test refine(Test test) {
		if(test instanceof TestSuite) {

			TestSuite newSuite = new TestSuite(((TestSuite) test).getName());

			Enumeration<Test> tests = ((TestSuite) test).tests();
			while(tests.hasMoreElements()) {
				Test t = refine(tests.nextElement());
				if(t != null) newSuite.addTest(t);
			}

			return newSuite;

		} else if(test instanceof TestCase) {

			if(test instanceof AutoTestCase) {
				if(AUTO) return test;
				else return null;

			} else {
				return test;

			}
		} else {

			System.err.println("Unknown class: " + test.getClass().getCanonicalName() + ": " + test);
			return null;
		}
	}
}
