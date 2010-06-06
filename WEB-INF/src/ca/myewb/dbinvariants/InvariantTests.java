/*

    This file is part of OpenMyEWB.

    OpenMyEWB is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    OpenMyEWB is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with OpenMyEWB.  If not, see <http://www.gnu.org/licenses/>.

    OpenMyEWB is Copyright 2005-2009 Nicolas Kruchten (nicolas@kruchten.com), Francis Kung, Engineers Without Borders Canada, Michael Trauttmansdorff, Jon Fishbein, David Kadish

*/

package ca.myewb.dbinvariants;

import junit.framework.Test;
import junit.framework.TestSuite;


public class InvariantTests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite();

		//$JUnit-BEGIN$
		suite.addTestSuite(SingleGroupInvariantsTest.class);
		suite.addTestSuite(MultiGroupInvariantsTest.class);
		suite.addTestSuite(MultipleEmailInvariantTest.class);
		suite.addTestSuite(ApplicationSystemInvariantTest.class);

		//$JUnit-END$
		return suite;
	}
}
