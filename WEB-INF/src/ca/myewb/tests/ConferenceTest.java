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

import ca.myewb.frame.ConferenceCode;
import ca.myewb.frame.HibernateUtil;


public class ConferenceTest extends TestCase
{
	Session session;
	Transaction tx;
	
	public void testCodeGenerationAndValidity()
	{
		ConferenceCode code;
		
		code = new ConferenceCode("blah");
		assertFalse(code.isValid());
		
		String codeString = ConferenceCode.generateCode("a123");
		code = new ConferenceCode(codeString);
		assertTrue(code.isValid());
		
		assertEquals("test type", code.getType());
		assertEquals(123, code.getNumber());
		
	}
	
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

}
