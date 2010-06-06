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


public class MultipleEmailInvariantTest extends TestCase
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
	
	public void testNoSameEmailAssociatedToMoreThanOneUser()
	{
		if(HibernateUtil.isFactoryInitialized("deploy"))
		{	
			String sql = "select count(*) from " +
					"(SELECT count( * ) AS num " +
					"FROM useremails " +
					"GROUP BY email " +
					"order by num desc) as x where x.num >1";
	
			int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
			assertEquals("No emails should be associated with more than one user", 0, result);
		}
	}
	
	public void testAllPrimaryEmailsInUserEmailTableForUser()
	{
		String sql = "SELECT count(*) " +
			"FROM users u " +
			"WHERE u.email IS NOT NULL " +
			"AND u.email != ''" +
			"AND u.email NOT IN( " +
				"SELECT e.email " +
				"FROM useremails e " +
				"WHERE e.userid = u.id " +
			")";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No primary email is not in the user email table", 0, result);
	}
	
	public void testUseremailActuallyAUser()
	{
		String sql = "SELECT count(*) " +
		"FROM useremails e " +
		"WHERE e.userid NOT IN( " +
			"SELECT u.id " +
			"FROM users u " +
		")";

	int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
	assertEquals("All useremail entires should correspond to a user", 0, result);
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
