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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.ApplicationQuestionModel;
import ca.myewb.model.ApplicationSessionModel;
import ca.myewb.model.UserModel;


public class ApplicationSessionTest extends TestCase
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
	
	public void testNewApplicationSession()
	{
		//Proper session creation
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2000);
		Date open = cal.getTime();
		cal.set(Calendar.MONTH, 2);
		Date due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		Date close = cal.getTime();
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		assertNotNull("The application session should exist", s);
		int id = s.getId();
		
		s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, id);
		assertEquals("The name should be the one that was set", "Test session", s.getName());
		assertEquals("The instructions should be the one that was set", "Test instructions", s.getInstructions());
		assertEquals("The completed applcation message should be the one set", "Test completedAppMessage", s.getCompletedApplicationMessage());
		assertEquals("The close email message should be the one set", "Test closeEmailText", s.getCloseEmailText());
		assertEquals("The open date should be the  one set", open, s.getOpenDate());
		assertEquals("The due date should be the one set", due, s.getDueDate());
		assertEquals("The close date should be the one set", close, s.getCloseDate());
		assertFalse("The session should not have sent an email", s.isEmailSent());
		
		//Bad sessions
		
		//due before open
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2000);
		open = cal.getTime();
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.YEAR, 1999);
		due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		close = cal.getTime();
		try 
		{
			s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
			fail("Exception should have been thrown since open date was greater than due date");
		} 
		catch (IllegalArgumentException e)
		{
		}
		
		//close before open
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2000);
		open = cal.getTime();
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.YEAR, 1999);
		due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		cal.set(Calendar.YEAR, 1998);
		close = cal.getTime();
		try 
		{
			s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
			fail("Exception should have been thrown since due date was greater than close date");
		} 
		catch (IllegalArgumentException e)
		{
		}

	}
	
	public void testSave()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2001);
		Date open = cal.getTime();
		cal.set(Calendar.MONTH, 2);
		Date due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		Date close = cal.getTime();
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", new Date(), new Date(), new Date(), "Test Rejection Email");
		s.save("New Test session", "New Test instructions", "Test Instructions", "New Test completedAppMessage", "New Test closeEmailText", open, due, close, "Test Rejection Email");
		int id = s.getId();
		
		s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, id);
		assertEquals("The name should be the one that was set", "New Test session", s.getName());
		assertEquals("The instructions should be the one that was set", "New Test instructions", s.getInstructions());
		assertEquals("The completed applcation message should be the one set", "New Test completedAppMessage", s.getCompletedApplicationMessage());
		assertEquals("The close email message should be the one set", "New Test closeEmailText", s.getCloseEmailText());
		assertEquals("The open date should be the  one set", open, s.getOpenDate());
		assertEquals("The due date should be the one set", due, s.getDueDate());
		assertEquals("The close date should be the one set", close, s.getCloseDate());
		
		//Bad sessions
		
		//due before open
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2000);
		open = cal.getTime();
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.YEAR, 1999);
		due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		close = cal.getTime();
		try 
		{
			s.save("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
			fail("Exception should have been thrown since open date was greater than due date");
		} 
		catch (IllegalArgumentException e)
		{
		}
		
		//close before open
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2000);
		open = cal.getTime();
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.YEAR, 1999);
		due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		cal.set(Calendar.YEAR, 1998);
		close = cal.getTime();
		try 
		{
			s.save("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
			fail("Exception should have been thrown since due date was greater than close date");
		} 
		catch (IllegalArgumentException e)
		{
		}

	}
	
	public void testClose() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 1);
		cal.set(Calendar.YEAR, 2001);
		Date open = cal.getTime();
		cal.set(Calendar.MONTH, 2);
		Date due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		Date close = cal.getTime();
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		Date now = new Date();
		s.close();
		int id = s.getId();
		
		s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, id);
		assertEquals("The session should be closed at the time close() was called", now, s.getCloseDate());
	}
	
	public void testAddQuestion()
	{
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
		ApplicationQuestionModel q = s.addQuestion("Test question");
		assertNotNull("The question should exist", q);
		int idQ = q.getId();
		int idS = s.getId();
		
		q = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, idQ);
		s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, idS);
		assertEquals("The application question should read as set", "Test question", q.getQuestion());
		assertSame("The session of the question should be this application session", s, q.getSession());
		assertTrue("The question should be in this application sessions list of questions", s.getQuestions().contains(q));
	}
	
	public void testGetOpenApplicationSessions()
	{
		//In order to make this work all test sessions created in this method must be closed, or be closed at the end of the method
		
		//open, due, closed
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2000);
		Date open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 2);
		Date close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
		Date due = cal.getTime();
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		List<ApplicationSessionModel> col = ApplicationSessionModel.getOpenApplicationSessions();
		assertEquals("There should be no open sessions", 0, col.size());
		
		//open, not due, not closed
		cal = Calendar.getInstance();
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 2);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getOpenApplicationSessions();
		assertEquals("There should be 1 open sessions", 1, col.size());
		assertSame("This session should be the one that is open", s, col.get(0));
		
		s.close();
		
		//open, due, not closed
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 2);
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 4);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 3);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getOpenApplicationSessions();
		assertEquals("There should be 1 open sessions", 1, col.size());
		assertSame("This session should be the one that is open", s, col.get(0));
		
		s.close();
		
		//not open, not due, not closed
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 3000);
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 2);
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 4);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 3);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getOpenApplicationSessions();
		assertEquals("There should be no open sessions", 0, col.size());
		
		s.close();
	}
	
	public void testGetClosedApplicationSessions()
	{	
		//In order to make this work all test sessions created in this method must be closed, or be closed at the end of the method
		
		//open, due, closed
		int curNum = ApplicationSessionModel.getClosedApplicationSessions().size();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2000);
		Date open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 2);
		Date close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
		Date due = cal.getTime();
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		List<ApplicationSessionModel> col = ApplicationSessionModel.getClosedApplicationSessions();
		assertEquals("There should be one more closed session", curNum + 1, col.size());
		assertTrue("The sesison we just closed should be in the list of closed applications", ApplicationSessionModel.getClosedApplicationSessions().contains(s));
		
		//open, not due, not closed
		curNum = ApplicationSessionModel.getClosedApplicationSessions().size();
		cal = Calendar.getInstance();
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 2);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getClosedApplicationSessions();
		assertEquals("There should be the same number of closed sessions", curNum, col.size());
		
		s.close();
		
		//open, due, not closed
		curNum = ApplicationSessionModel.getClosedApplicationSessions().size();
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 2);
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 4);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 3);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getClosedApplicationSessions();
		assertEquals("There should be the same number of closed sessions", curNum, col.size());
		
		s.close();
		
		//not open, not due, not closed
		curNum = ApplicationSessionModel.getClosedApplicationSessions().size();
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 3000);
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 2);
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 4);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 3);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getClosedApplicationSessions();
		assertEquals("There should be the same number of closed sessions", curNum, col.size());
		
		s.close();
	}
	
	public void testGetFutureApplicationSessions()
	{
		//In order to make this work all test sessions created in this method must be closed, or be closed at the end of the method
		
		//open, due, closed
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2000);
		Date open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 2);
		Date close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
		Date due = cal.getTime();
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		List<ApplicationSessionModel> col = ApplicationSessionModel.getFutureApplicationSessions();
		assertEquals("There should be no future sessions", 0, col.size());
		
		//open, not due, not closed
		cal = Calendar.getInstance();
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 2);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 1);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getFutureApplicationSessions();
		assertEquals("There should be no future sessions", 0, col.size());
		
		s.close();
		
		//open, due, not closed
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 2);
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 4);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 3);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getFutureApplicationSessions();
		assertEquals("There should be no future sessions", 0, col.size());
		
		s.close();
		
		//not open, not due, not closed
		cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 3000);
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 2);
		open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 4);
		close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 3);
		due = cal.getTime();
		s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		
		col = ApplicationSessionModel.getFutureApplicationSessions();
		assertEquals("There should be 1 future sessions", 1, col.size());
		assertSame("This session should be the only future", s, col.get(0));
		
		s.close();
	}
	
	public void testOpen()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 3000);
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 2);
		Date open = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) + 4);
		Date close = cal.getTime();
		cal.set(Calendar.DATE, cal.get(Calendar.DATE) - 3);
		Date due = cal.getTime();
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", open, due, close, "Test Rejection Email");
		Date now = new Date();
		s.open();
		int id = s.getId();
		
		s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, id);
		assertEquals("The open date should be now", now, s.getOpenDate());
		
		s.close();
	}
	
	public void testReopen()
	{
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.YEAR, 2001);
		cal.set(Calendar.MONTH, 2);
		Date due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		Date close = cal.getTime();
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", new Date(), new Date(), new Date(), "Test Rejection Email");
		cal.set(Calendar.YEAR, 3002);
		Date newClose = cal.getTime();
		cal.set(Calendar.DATE, 1);
		Date newDue = cal.getTime();
		Date now = new Date();
		s.reopen(newDue, newClose);
		int id = s.getId();
		
		s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, id);
		assertEquals("The open date should be now", now, s.getOpenDate());
		assertEquals("The due date should be the one that was set", newDue, s.getDueDate());
		assertEquals("The close date should be the one set", newClose, s.getCloseDate());
		
		s.close();
		
		//Bad sessions
		
		//due before open
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.YEAR, 1999);
		due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		close = cal.getTime();
		try 
		{
			s.reopen(due, close);
			fail("Exception should have been thrown since open date was greater than due date");
		} 
		catch (IllegalArgumentException e)
		{
		}
		
		//close before due
		cal = Calendar.getInstance();
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.YEAR, 3001);
		due = cal.getTime();
		cal.set(Calendar.DATE, 5);
		cal.set(Calendar.YEAR, 3000);
		close = cal.getTime();
		try 
		{
			s.reopen(due, close);
			fail("Exception should have been thrown since due date was greater than close date");
		} 
		catch (IllegalArgumentException e)
		{
		}
	}
	
	public void testGetNextQuestion()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			ApplicationQuestionModel q1 = s.addQuestion("Test Question 1");
			ApplicationQuestionModel q2 = s.addQuestion("Test Question 2");

			assertSame("The first question should be question 1", q1, s.getNextQuestion(null));
			assertSame("The question after question 1 should be question 2", q2, s.getNextQuestion(q1));
			assertNull("Question 2 should be the last question", s.getNextQuestion(q2));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testGetApplicantEmails()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			UserModel user1 = UserModel.newAssociateSignUp("Test Email 1", "Test", "User", "testuser");
			UserModel user2 = UserModel.newAssociateSignUp("Test Email 2", "Test", "User", "testuser");
			user1.applyToSession(s);
			user2.applyToSession(s);
			int id = s.getId();
			
			s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, id);
			List<String> emails = s.getApplicantEmails(true);
			assertTrue("There should only be two e-mails", emails.size() == 2);
			assertTrue("the first users email is in the list", emails.contains("Test Email 1"));
			assertTrue("the second users email is in the list", emails.contains("Test Email 2"));

		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testCroned()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			s.croned();
			int id = s.getId();
			
			s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, id);
			assertTrue("The session email should now have been sent", s.isEmailSent());
			
			s.close();

		} catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testRecentlyClosedSessions()
	{
		//In order to make this work all test sessions created in this method must be closed, or be closed at the end of the method
		
		//open, due, closed
		int curNum = ApplicationSessionModel.getRecentlyClosedSessions(3).size();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -10);
		Date oneWeek = cal.getTime();
		cal.add(Calendar.DATE, -7);
		Date twoWeeks = cal.getTime();
		cal.add(Calendar.DATE, -14);
		Date fourWeeks = cal.getTime();
		cal.set(Calendar.YEAR, 3006);
		Date future = cal.getTime();
		
		ApplicationSessionModel sOneWeek = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", oneWeek, oneWeek, oneWeek, "Test Rejection Email");
		ApplicationSessionModel sTwoWeek = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", twoWeeks, twoWeeks, twoWeeks, "Test Rejection Email");
		ApplicationSessionModel sFourWeek = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", fourWeeks, fourWeeks, fourWeeks, "Test Rejection Email");
		ApplicationSessionModel sFuture = ApplicationSessionModel.newApplicationSession("Test session", "Test instructions", "Test Instructions", "Test completedAppMessage", "Test closeEmailText", new Date(), new Date(), future, "Test Rejection Email");
		
		List<ApplicationSessionModel> col = ApplicationSessionModel.getRecentlyClosedSessions(3);
		assertEquals("There should be two more recently closed session", curNum + 2, col.size());
		assertTrue("The one week session should be in the list", col.contains(sOneWeek));
		assertTrue("The two week session should be in the list", col.contains(sTwoWeek));
		assertFalse("The four week session should not be in the list", col.contains(sFourWeek));
		assertFalse("The future session should not be in the list", col.contains(sFuture));
		
		sOneWeek.close();
		sTwoWeek.close();
		sFourWeek.close();
		sFuture.close();
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
}
