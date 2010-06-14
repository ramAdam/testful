/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

package testful.coverage;

import testful.coverage.fault.FaultsCoverage;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.utils.ElementManager;

/**
 * Test for the fault detection functionality
 * @author matteo
 */
public class FaultTestCase extends testful.testCut.TestCoverageFaultTestCase {

	public void testA() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testANull() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA1() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a1, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());
	}

	public void testA1Null() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a1, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA2() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a2, new Reference[] { objects[0] } )
		}));


		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA2Null() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a2, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testB() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], b , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());
	}

	public void testB1() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], b1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testC() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());
	}

	public void testC1() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testC2() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c2, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testD() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], d , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testE() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], e , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());
	}

	public void testE1() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], e1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}
}
