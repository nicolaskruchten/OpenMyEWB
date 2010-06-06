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
import ca.myewb.model.ApplicationAnswerModel;
import ca.myewb.model.ApplicationModel;
import ca.myewb.model.ApplicationQuestionModel;
import ca.myewb.model.ApplicationSessionModel;
import ca.myewb.model.UserModel;


public class ApplicationTest extends TestCase
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
	
	public void testNewApplication()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			UserModel user = UserModel.newAssociateSignUp("Test Email", "Test", "User", "testuser");
			ApplicationModel app = ApplicationModel.newApplication(s, user);
			assertNotNull("The Application should exist", app);
			int id = app.getId();
			
			app = (ApplicationModel)session.load(ApplicationModel.class, id);
			assertSame("The application session should be the one set", s, app.getSession());
			assertSame("The user should be the one set", user, app.getUser());
			assertEquals("English reading ability should be 0", 0, app.getEnglishReading());
			assertEquals("English writing ability should be 0", 0, app.getEnglishWriting());
			assertEquals("English speaking ability should be 0", 0, app.getEnglishSpeaking());
			assertEquals("French reading ability should be 0", 0, app.getFrenchReading());
			assertEquals("French writing ability should be 0", 0, app.getFrenchWriting());
			assertEquals("French speaking ability should be 0", 0, app.getFrenchSpeaking());
			assertEquals("Schooling should be the null string", "", app.getSchooling());
			assertEquals("Resume should be the null string", "", app.getResume());
			assertEquals("References should be the null string", "", app.getRefs());
		} catch (Exception e)
		{
			fail("Unexpected Excpetion thrown");
			e.printStackTrace();
		}
	}
	
	public void testAnswerQuestion()
	{
		try
		{
			//New Question
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			UserModel user = UserModel.newAssociateSignUp("Test Email", "Test", "User", "testuser");
			ApplicationQuestionModel q = s.addQuestion("Test question");
			ApplicationModel app = ApplicationModel.newApplication(s, user);
			ApplicationAnswerModel ans = app.answerQuestion(q, "Test Answer");
			assertNotNull("The Answer should exist", app);
			int id = app.getId();
			
			app = (ApplicationModel)session.load(ApplicationModel.class, id);
			assertTrue("The application has the answer saved", app.getAnswers().contains(ans));
			
			//Previously Answeres Question
			s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			user = UserModel.newAssociateSignUp("Test Email", "Test", "User", "testuser");
			q = s.addQuestion("Test question");
			app = ApplicationModel.newApplication(s, user);
			ans = app.answerQuestion(q, "Test Answer");
			ans = app.answerQuestion(q, "New Test Answer");
			assertNotNull("The Answer should exist", app);
			id = app.getId();
			int idA = ans.getId();
			
			app = (ApplicationModel)session.load(ApplicationModel.class, id);
			assertTrue("The application has the answer saved", app.getAnswers().contains(ans));
			assertEquals("The application has only 1 answer saved", 1, app.getAnswers().size());
			ans = (ApplicationAnswerModel)session.load(ApplicationAnswerModel.class, idA);
			assertEquals("The new answer should be the one set", "New Test Answer", ans.getAnswer());
		} catch (Exception e)
		{
			fail("Unexpected Excpetion thrown");
			e.printStackTrace();
		}
	}
	
	public void testSave()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			UserModel user = UserModel.newAssociateSignUp("Test Email", "Test", "User", "testuser");
			ApplicationModel app = ApplicationModel.newApplication(s, user);
			app.save(5, 4, 3, 2, 1, 5, "Test Schooling", "Test Resume", "Test References", 0);
			int id = app.getId();
			
			app = (ApplicationModel)session.load(ApplicationModel.class, id);
			assertEquals("English writing ability should be the one set", 5, app.getEnglishWriting());
			assertEquals("English reading ability should be the one set", 4, app.getEnglishReading());
			assertEquals("English speaking ability should be the one set", 3, app.getEnglishSpeaking());
			assertEquals("French writing ability should be the one set", 2, app.getFrenchWriting());
			assertEquals("French reading ability should be the one set", 1, app.getFrenchReading());
			assertEquals("French speaking ability should be the one set", 5, app.getFrenchSpeaking());
			assertEquals("Schooling should be the one set", "Test Schooling", app.getSchooling());
			assertEquals("Resume should be the one set", "Test Resume", app.getResume());
			assertEquals("References should be the one set", "Test References", app.getRefs());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testGetAnswerForQuestion()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			UserModel user = UserModel.newAssociateSignUp("Test Email", "Test", "User", "testuser");
			ApplicationModel app = ApplicationModel.newApplication(s, user);
			ApplicationQuestionModel q = s.addQuestion("Test Question");
			ApplicationAnswerModel a = app.answerQuestion(q, "Test Answer");
			ApplicationAnswerModel aReturned = app.getAnswerForQuestion(q); 

			assertEquals("The answer should be the one set", a.getAnswer(), aReturned.getAnswer());
			assertSame("The application should be the one set", a.getApp(), aReturned.getApp());
			assertSame("The question should be the one set", a.getQuestion(), aReturned.getQuestion());
			assertEquals("The object should be the one we just answered", a.getId(), aReturned.getId());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
		}
	}
	
	public void testGetIncompleteQuestions()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			UserModel user = UserModel.newAssociateSignUp("Test Email", "Test", "User", "testuser");
			ApplicationModel app = ApplicationModel.newApplication(s, user);
			s.addQuestion("Test Question 1");
			ApplicationQuestionModel q = s.addQuestion("Test Question 2");
			app.save(5, 5, 5, 5, 5, 5, "Test Schooling", "", "Test References", 0);
			app.answerQuestion(q, "Test Answer");
			String[] incomplete = app.getIncompleteQuestions();

			assertTrue("There should only be 3 incomplete questions", incomplete.length == 3);
			assertEquals("Resume should be incomplete", "Resum&eacute;", incomplete[1]);
			assertEquals("The first question should be incomplete", "Application Question 1", incomplete[2]);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail("Unexpected Exception thrown");
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
}
