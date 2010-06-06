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

package ca.myewb.tests;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.OVInfoModel;
import ca.myewb.model.UserModel;


public class OVInfoTest extends TestCase
{
	Session session;
	Transaction tx;

	public void setUp()
	{
		try
		{
			// Register DB
			Class.forName("com.mysql.jdbc.Driver");

			// Start a Hibernate session
			HibernateUtil.createFactory("test");
			session = HibernateUtil.currentSession();
			tx = session.beginTransaction();
		}
		catch (Exception e)
		{
			System.err.println("Exception " + e);
			e.printStackTrace();
		}
	}

	public void tearDown()
	{
		try
		{
			tx.commit();
			HibernateUtil.closeSession();
		}
		catch (Exception e)
		{
			System.err.println("Excpetion: " + e);
			e.printStackTrace();
		}
	}

	// Test general getters/setters
	public void testGeneral()
	{
		try
		{
			OVInfoModel p = OVInfoModel.newOVInfo();
			Integer i = p.getId();
			
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");

			u.setOVInfo(p);

			// New object to make sure info was written to database
			OVInfoModel p2 = (OVInfoModel)session.load(OVInfoModel.class, i);
			assertEquals(p2.getUserid(), u.getId());
			assertTrue(u.getOVInfo().equals(p2));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void testNewOVInfo()
	{
		OVInfoModel i = OVInfoModel.newOVInfo();
		assertNotNull("The OvInfo should exist", i);
	}

}
