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

import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;
import ca.myewb.model.WhiteboardModel;


public class WhiteboardTest extends TestCase
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
			EventModel e = EventModel.newEvent("Test Event", testDate, testDate, "Test Location", "Test Notes", GroupModel.newGroup(), true, "");
						
			int id = e.getWhiteboard().getId();
			Date lastEdit = e.getWhiteboard().getLastEditDate();
			
			WhiteboardModel w = (WhiteboardModel)session.load(WhiteboardModel.class, id);
			assertEquals("There should be no attached files", 0, w.getAttachedFiles().size() );
			assertEquals("The body should be empty", "", w.getBody() );
			assertEquals("The ID should be equal to the one that was set", id, w.getId() );
			assertEquals("The last edit date should be equal to the one that was set", lastEdit, w.getLastEditDate() );
			assertNull("Nobody should have edited the whiteboard", w.getLastEditor() );
			assertEquals("Nobody should have edited the whiteboard", 0, w.getNumEdits() );
			assertEquals("The parent should be equal to the one that was set", e, w.getParentEvent() );
			
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
			GroupModel newGroup = GroupModel.newGroup();
			EventModel e = EventModel.newEvent("Test Event", testDate, testDate, 
					"Test Location", "Test Notes", newGroup, true, "");
			UserModel u = UserModel.newAssociateSignUp("blackhole@system.com", "testy", "mctesterson", "test");
			int id = e.getWhiteboard().getId();
			WhiteboardModel w = (WhiteboardModel)session.load(WhiteboardModel.class, id);
			w.save("New body", u);
			w = (WhiteboardModel)session.load(WhiteboardModel.class, id);
			
			assertEquals("There should(n't) be files as set", 0, w.getAttachedFiles().size() );
			assertEquals("The body should be equal to the one that was set", "New body", w.getBody() );
			assertEquals("The ID should be equal to the one that was set", id, w.getId() );
			assertEquals("The last editor should be the user 'u'", u, w.getLastEditor() );
			assertEquals("One person should have edited the whiteboard", 1, w.getNumEdits() );
			assertEquals("The parent should be equal to the one that was set", e, w.getParentEvent() );
						
		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception");
		}
	}

}