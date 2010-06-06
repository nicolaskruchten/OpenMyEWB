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
import ca.myewb.model.PlacementModel;
import ca.myewb.model.UserModel;


public class PlacementTest extends TestCase
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
			PlacementModel p = PlacementModel.newPlacementModel();
			Integer i = p.getId();
			
			UserModel u = UserModel.newAssociateSignUp("a@b.com", "Test", "User", "123456");

			u.assignPlacement(p);

			// New object to make sure info was written to database
			PlacementModel p2 = (PlacementModel)session.load(PlacementModel.class, i);
			assertTrue(p2.getOv().equals(u));
			assertTrue(u.getPlacements().contains(p2));
		}
		catch (Exception e)
		{
			System.err.println("Exception caught: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void testNewPlacement()
	{
		PlacementModel p = PlacementModel.newPlacementModel();
		assertNotNull("Placement should exist", p);
		int id = p.getId();
		
		PlacementModel p2 = (PlacementModel)session.load(PlacementModel.class, id);
		assertFalse("The placement should be initialized to be inactive", p2.isActive());
		assertFalse("The placement should not be deleted", p2.isDeleted());
	}
	
	public void testSave()
	{
		PlacementModel p = PlacementModel.newPlacementModel();
		p.save("Test Placement", "0001", "17-07-2007", "17-08-2007", "Somewhere", "Someplace", "It'll be fun!", true);
		int id = p.getId();
		
		PlacementModel p2 = (PlacementModel)session.load(PlacementModel.class, id);
		assertEquals("The placement name should be what was set", "Test Placement", p2.getName());
		assertEquals("The placement accounting id should be what was set", "0001", p2.getAccountingid());
		assertEquals("The placement start date should be what was set", "17-07-2007", p2.getStartdate());
		assertEquals("The placement end date should be what was set", "17-08-2007", p2.getEnddate());
		assertEquals("The placement country should be what was set", "Somewhere", p2.getCountry());
		assertEquals("The placement town should be what was set", "Someplace", p2.getTown());
		assertEquals("The placement description should be what was set", "It'll be fun!", p2.getDescription());
	}
	
	public void testDeactivate()
	{
		PlacementModel p = PlacementModel.newPlacementModel();
		int id = p.getId();
		p.setActive(true);
		p.deactivate();
		
		PlacementModel p2 = (PlacementModel)session.load(PlacementModel.class, id);
		assertFalse("The placement should be decativated", p2.isActive());
	}
	
	public void testDelete()
	{
		PlacementModel p = PlacementModel.newPlacementModel();
		int id = p.getId();
		
		p.delete();
		
		PlacementModel p2 = (PlacementModel)session.load(PlacementModel.class, id);
		assertTrue("The placement should be deleted", p2.isDeleted());
	}

}
