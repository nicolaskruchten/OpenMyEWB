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

import java.util.Date;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;


public class EventTest extends TestCase
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
	
	public void testNewEvent()
	{
		try
		{
			Date testDate = new Date();
			GroupModel g = GroupModel.newGroup();
			EventModel e = EventModel.newEvent("Test Event", testDate, testDate, "Test Location", "Test Notes", g, true, "");
			int id = e.getId();
			
			e = (EventModel)session.load(EventModel.class, id);
			assertEquals("The name should be the one set", "Test Event", e.getName());
			assertEquals("The start date should be the one set", testDate, e.getStartDate());
			assertEquals("The end date should be the one set", testDate, e.getEndDate());
			assertEquals("The location should be the one set", "Test Location", e.getLocation());
			assertEquals("The notes should be the one set", "Test Notes", e.getNotes());
			assertEquals("The whiteboard should be (en/dis)abled as set", true, e.getWhiteboard().isEnabled());
			assertSame("The group should be the one set", g, e.getGroup());
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception");
		}
		
	}
	
	public void testSave()
	{
		try
		{
			Date testDate = new Date();
			GroupModel g = GroupModel.newGroup();
			GroupModel newG = GroupModel.newGroup();
			EventModel e = EventModel.newEvent("Test Event", testDate, testDate, "Test Location", "Test Notes", g, true, "");
			int id = e.getId();			
			e = (EventModel)session.load(EventModel.class, id);
			Date newDate = new Date();
			e.save("New Event", newDate, newDate, "New Location", "New Notes", newG, false, "");
			
			e = (EventModel)session.load(EventModel.class, id);
			assertEquals("The name should be the one set", "New Event", e.getName());
			assertEquals("The start date should be the one set", newDate, e.getStartDate());
			assertEquals("The end date should be the one set", newDate, e.getEndDate());
			assertEquals("The location should be the one set", "New Location", e.getLocation());
			assertEquals("The notes should be the one set", "New Notes", e.getNotes());
			assertEquals("The whiteboard should be (en/dis)abled as set", false, e.getWhiteboard().isEnabled());
			assertSame("The group should be the one set", newG, e.getGroup());
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception");
		}
	}
	
	public void testDelete()
	{
		try
		{
			Date testDate = new Date();
			GroupModel g = GroupModel.newGroup();
			EventModel e = EventModel.newEvent("Test Event", testDate, testDate, "Test Location", "Test Notes", g, true, "");
			int id = e.getId();			
			e.delete();
			
			e = (EventModel)session.load(EventModel.class, id);
			assertEquals("The name should be the deleted name", "*deleted* Test Event", e.getName());
			assertEquals("The start date should be the one set", testDate, e.getStartDate());
			assertEquals("The end date should be the one set", testDate, e.getEndDate());
			assertEquals("The location should be the one set", "Test Location", e.getLocation());
			assertEquals("The whiteboard should be (en/dis)abled as set", true, e.getWhiteboard().isEnabled());
			assertSame("The group should be the deleted group", Helpers.getGroup("DeletedPosts"), e.getGroup());
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception");
		}
	}
	
	public void testNewEventWithWhiteboard()
	{
		try
		{
			Date testDate = new Date();
			GroupModel g = GroupModel.newGroup();
			EventModel e = EventModel.newEvent("Test Event", testDate, testDate, "Test Location", "Test Notes", g, true, "");
			int id = e.getId();
			
			e = (EventModel)session.load(EventModel.class, id);
			assertEquals("The name should be the one set", "Test Event", e.getName());
			assertEquals("The start date should be the one set", testDate, e.getStartDate());
			assertEquals("The end date should be the one set", testDate, e.getEndDate());
			assertEquals("The location should be the one set", "Test Location", e.getLocation());
			assertEquals("The notes should be the one set", "Test Notes", e.getNotes());
			assertEquals("The whiteboard should be (en/dis)abled as set", true, e.getWhiteboard().isEnabled());
			assertSame("The group should be the one set", g, e.getGroup());
			
			e.save(e.getName(), e.getStartDate(), e.getEndDate(), e.getLocation(), e.getNotes(), g, false, "");
			
			assertTrue("The whiteboard should be disabled", !e.getWhiteboard().isEnabled());
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception");
		}
		
	}
	
	public void testNewEventWithoutWhiteboard()
	{
		try
		{
			Date testDate = new Date();
			GroupModel g = GroupModel.newGroup();
			EventModel e = EventModel.newEvent("Test Event", testDate, testDate, "Test Location", "Test Notes", g, false, "");
			int id = e.getId();
			
			e = (EventModel)session.load(EventModel.class, id);
			assertEquals("The name should be the one set", "Test Event", e.getName());
			assertEquals("The start date should be the one set", testDate, e.getStartDate());
			assertEquals("The end date should be the one set", testDate, e.getEndDate());
			assertEquals("The location should be the one set", "Test Location", e.getLocation());
			assertEquals("The notes should be the one set", "Test Notes", e.getNotes());
			assertNull("The whiteboard should be null", e.getWhiteboard());
			assertSame("The group should be the one set", g, e.getGroup());
			
			e.save(e.getName(), e.getStartDate(), e.getEndDate(), e.getLocation(), e.getNotes(), g, true, "");
			
			assertTrue("The whiteboard should be enabled", e.getWhiteboard().isEnabled());
			
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception");
		}
		
	}

}