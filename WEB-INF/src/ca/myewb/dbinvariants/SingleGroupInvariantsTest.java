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

public class SingleGroupInvariantsTest extends TestCase
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

	public void testNoRecipientMemberInvariant()
	{
		int result = getNumDuplicateRolesForUserInGroup('r', 'm');
		assertEquals("No user should be a recepient and a member of the same group", 0, result);
	}
	
	public void testNoSenderLeaderInvariant()
	{
		int result = getNumDuplicateRolesForUserInGroup('s', 'l');
		assertEquals("No user should be a sender and a leader of the same group", 0, result);
	}
	
	public void testNoDuplicateRecipientInvariant()
	{
		int result = getNumDuplicateRolesForUserInGroup('r', 'r');
		assertEquals("No user should have two recipient roles in a group", 0, result);
	}
	
	public void testNoDuplicateMemberInvariant()
	{
		int result = getNumDuplicateRolesForUserInGroup('m', 'm');
		assertEquals("No user should have two member roles in a group", 0, result);
	}
	
	public void testNoDuplicateSenderInvariant()
	{
		int result = getNumDuplicateRolesForUserInGroup('s', 's');
		assertEquals("No user should have two sender roles in a group", 0, result);
	}
	
	public void testNoDuplicateLeaderInvariant()
	{
		int result = getNumDuplicateRolesForUserInGroup('l', 'l');
		assertEquals("No user should have two leader roles in a group", 0, result);
	}
	
	public void testNoSenderOrLeaderWithoutMemberOrRecipientInvariant()
	{
		String sql = "SELECT count(*) " +
				"FROM roles r1 " +
				"WHERE (r1.level = 'l' OR r1.level = 's') " +
				"AND r1.end IS NULL " +
				"AND NOT EXISTS ( " +
					"SELECT * " +
					"FROM roles r2 " +
					"WHERE r1.userid = r2.userid " +
					"AND (r2.level = 'm' OR r2.level = 'r') " +
					"AND r2.end IS NULL " +
					"AND r1.userid = r2.userid " +
					"AND r1.groupid = r2.groupid " +
					"AND r2.id != r1.id " +
				") ";
		
		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No user should be a sender or a recipient of a group without being a member or recipient of that group", 0, result);
	}
	
	public void testExpiry()
	{
		String sql = "select count(*) " +
				"from users u, roles r " +
				"where r.userid=u.id and r.end is null and r.groupid=6 " +
				"and u.expiry is null";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No regular member should have a null expiry date", 0, result);

	}
	
	public void testNoRecipientRolesInAdminGroupInvariant()
	{
		int result = numRoleInAdminGroup('r');
		assertEquals("There should be no recipient roles in admin groups", 0, result);
	}
	
	public void testNoSenderRolesInAdminGroupInvariant()
	{
		int result = numRoleInAdminGroup('s');
		assertEquals("There should be no sender roles in admin groups", 0, result);
	}
	
	public void testNoLeaderRolesInAdminGroupInvariant()
	{
		int result = numRoleInAdminGroup('l');
		assertEquals("There should be no leader roles in admin groups", 0, result);
	}

	private int numRoleInAdminGroup(char role) {
		String sql = "SELECT count(*) " +
				"FROM roles r, groups g " +
				"WHERE r.groupid = g.id " +
				"AND ((g.admin = 1 AND g.visible = 0) OR g.id=1) " +
				"AND r.level = ?";
		int result = ((BigInteger)session.createSQLQuery(sql).setCharacter(0, role).uniqueResult()).intValue();
		return result;
	}
	
	public void testNoMemberRolesInGeneralMailGroupInvariant()
	{
		String sql = "SELECT count(*) " +
			"FROM roles r, groups g " +
			"WHERE r.groupid = g.id " +
			"AND g.parent IS NULL " +
			"AND g.id NOT IN (SELECT id FROM groupchapter) " +
			"AND g.admin = 0 " +
			"AND r.level = 'm'";
		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("There should be no member roles in general mail groups", 0, result);
	}
	
	public void testNoMemberRolesInChapterSublistGroup() {
		String sql = "SELECT count(*) " +
				"FROM roles r, groups g " +
				"WHERE r.groupid = g.id " +
				"AND g.parent IS NOT NULL " +
				"AND g.shortname != 'exec' " +
				"AND r.level = 'm'";
		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("There should be no member roles in chapter sublist groups", 0, result);
	}

	private int getNumDuplicateRolesForUserInGroup(char role1, char role2) {
		String sql = "SELECT count( * ) " +
				"FROM roles r1, roles r2 " +
				"WHERE r1.userid = r2.userid " +
				"AND r1.groupid = r2.groupid " +
				"AND r1.level = ? " +
				"AND r2.level = ? " +
				"AND r1.end IS NULL " +
				"AND r2.end IS NULL " +
				"AND r1.id != r2.id";
		
		int result = ((BigInteger)session.createSQLQuery(sql).setCharacter(0, role1).setCharacter(1, role2).uniqueResult()).intValue();
		return result;
	}
}
