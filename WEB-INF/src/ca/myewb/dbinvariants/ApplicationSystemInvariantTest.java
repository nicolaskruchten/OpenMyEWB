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

import java.math.BigInteger;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.frame.HibernateUtil;


public class ApplicationSystemInvariantTest extends TestCase
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
			if(!HibernateUtil.isFactoryInitialized())
			{
				HibernateUtil.createFactory("deploy");
			}
			session = HibernateUtil.currentSession();
			tx = session.beginTransaction();
		}
		catch (Exception e)
		{
			System.err.println("Exception " + e);
			e.printStackTrace();
		}
	}
	
	public void testNoMultipleAnswersToSameQuestion()
	{
		String sql = "SELECT count(*) " +
			"FROM appanswers a1, appanswers a2 " +
			"WHERE a1.appid = a2.appid " +
			"AND a1.id != a2.id " +
			"AND a1.questionid = a2.questionid ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No application should have two answers to the same question", 0, result);
	}
	
	public void testNoAppAnswersToQuestionsNotInSession()
	{
		String sql = "SELECT count(*) " +
			"FROM appanswers ans, applications app " +
			"WHERE ans.appid = app.id " +
			"AND ans.questionid NOT IN( " +
				"SELECT q.id " +
				"FROM appquestions q " +
				"WHERE q.sessionid = app.sessionid " +
			")";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No application should answer a question outside of the applications", 0, result);
	}
	
	public void testNoMultipleApplicationsToSameSession()
	{
		String sql = "SELECT count(*) " +
			"FROM applications a1, applications a2 " +
			"WHERE a1.userid = a2.userid " +
			"AND a1.id != a2.id " +
			"AND a1.sessionid = a2.sessionid ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No user should have two applications to the same session", 0, result);
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

	public void testInvariantSystem()
	{
		assertTrue(true);
	}
}
