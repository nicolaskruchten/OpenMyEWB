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
import ca.myewb.model.ApplicationQuestionModel;
import ca.myewb.model.ApplicationSessionModel;


public class ApplicationQuestionTest extends TestCase
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
	
	public void testNewApplicationQuestion()
	{
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
		ApplicationQuestionModel q = s.addQuestion("Test question");
		assertNotNull("The Application question should exist", q);
		int id = q.getId();
			
		q = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, id);
		assertEquals("The question should be the one set", "Test question", q.getQuestion());
		assertSame("The application session  should be the one the question was added to", s, q.getSession());
	}
	
	public void testSave()
	{
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
		ApplicationQuestionModel q = s.addQuestion("Test question");
		q.save("New Test question");
		int id = q.getId();
		
		q = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, id);
		assertEquals("The question should be the one set", "New Test question", q.getQuestion());
		assertSame("The application session  should be the one the question was added to", s, q.getSession());
	}
	
	public void testDelete()
	{
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
		int nextOrder = s.getQuestions().size() + 1;
		ApplicationQuestionModel q = s.addQuestion("Test question");
		q.delete();
		int idQ = q.getId();
		int idS = s.getId();
		
		q = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, idQ);
		s = (ApplicationSessionModel)session.load(ApplicationSessionModel.class, idS);
		assertNull("The session should be null", q.getSession());
		assertFalse("The question should no longer be in the application session", s.getQuestions().contains(q));
		assertEquals("The application question should be that last in the question order", nextOrder, q.getQuestionOrder());
	}
	
	public void testUpOrder()
	{
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
		ApplicationQuestionModel q1 = s.addQuestion("Test question 1");
		ApplicationQuestionModel q2 = s.addQuestion("Test question 2");
		ApplicationQuestionModel q3 = s.addQuestion("Test question 3");
		q1.upOrder();
		q2.upOrder();
		q3.upOrder();
		int idQ1 = q1.getId();
		int idQ2 = q2.getId();
		int idQ3 = q3.getId();
		
		q1 = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, idQ1);
		q2 = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, idQ2);
		q3 = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, idQ3);
		
		assertEquals("The first question sould be question 2", 1, q2.getQuestionOrder());
		assertEquals("The second question sould be question 3", 2, q3.getQuestionOrder());
		assertEquals("The third question sould be question 1", 3, q1.getQuestionOrder());
	}
	
	public void testDownOrder()
	{
		ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
		ApplicationQuestionModel q1 = s.addQuestion("Test question 1");
		ApplicationQuestionModel q2 = s.addQuestion("Test question 2");
		ApplicationQuestionModel q3 = s.addQuestion("Test question 3");
		q3.downOrder();
		q2.downOrder();
		q1.downOrder();
		int idQ1 = q1.getId();
		int idQ2 = q2.getId();
		int idQ3 = q3.getId();
		
		q1 = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, idQ1);
		q2 = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, idQ2);
		q3 = (ApplicationQuestionModel)session.load(ApplicationQuestionModel.class, idQ3);
		
		assertEquals("The first question sould be question 3", 1, q3.getQuestionOrder());
		assertEquals("The second question sould be question 1", 2, q1.getQuestionOrder());
		assertEquals("The third question sould be question 2", 3, q2.getQuestionOrder());
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
