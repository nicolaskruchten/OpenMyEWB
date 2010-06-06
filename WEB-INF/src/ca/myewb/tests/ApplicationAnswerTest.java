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


public class ApplicationAnswerTest extends TestCase
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
	
	public void testNewApplicationAnswer()
	{
		try
		{
			ApplicationSessionModel s = ApplicationSessionModel.newApplicationSession("Test Session", "Test Instructions", "Test Instructions", "Test completed app message", "Test close email text", new Date(), new Date(), new Date(), "Test Rejection Email");
			UserModel user = UserModel.newAssociateSignUp("Test Email", "Test", "User", "testuser");
			ApplicationModel app = ApplicationModel.newApplication(s, user);
			ApplicationQuestionModel q = s.addQuestion("Test Question");
			ApplicationAnswerModel ans = ApplicationAnswerModel.newApplicationAnswer(q, app, "Test answer");
			assertNotNull("The Application answer should exist", ans);
			int id = ans.getId();
			
			ans = (ApplicationAnswerModel)session.load(ApplicationAnswerModel.class, id);
			assertEquals("The answer should be the one set", "Test answer", ans.getAnswer());
			assertSame("The application should be the one set", app, ans.getApp());
			assertSame("The question should be the one set", q, ans.getQuestion());
		} catch (Exception e)
		{
			fail("Unexpected Excpetion thrown");
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
}
