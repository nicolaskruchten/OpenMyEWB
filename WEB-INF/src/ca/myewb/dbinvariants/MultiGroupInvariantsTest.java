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
import java.util.List;

import junit.framework.TestCase;

import org.hibernate.Session;
import org.hibernate.Transaction;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Helpers;
import ca.myewb.model.GroupModel;


public class MultiGroupInvariantsTest extends TestCase
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

	////////////// top-level memberships
	
	public void testAllUsersOrgOrDeleted()
	{
		String sql = "SELECT count(*) " +
		"FROM users u " +
		"WHERE NOT EXISTS (" +
			"SELECT * " +
			"FROM roles r, groups g " +
			"WHERE r.userid = u.id " +
			"AND r.end IS NULL " +
			"AND r.groupid = g.id " +
			"AND (g.name = 'Org' OR g.name = 'Deleted') " +
		") ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("Every known user should be a member of Org or Deleted", 0, result);
	}
	
	public void testUserMemberOrg() 
	{
		int result = numViolationsOfMutuallyInclusiveGroups("Users", "Org");
		assertEquals("Every member of the User group must be a member of the Org", 0, result);
	}
	
	public void testDeletionInvariant() 
	{
		String sql = "SELECT count(*) " +
			"FROM roles r1, roles r2, groups g1 " +
			"WHERE r1.userid = r2.userid " +
			"AND r1.groupid = g1.id " +
			"AND g1.name = 'Deleted' " +
			"AND r1.end IS NULL " +
			"AND r2.end IS NULL " +
			"AND r1.id != r2.id ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No deleted user should be a member of any group", 0, result);
	}

	////////////// associate/regular
	
	public void testAssociateOrRegularInvariant()
	{
		int result = numViolationsOfMutuallyExclusiveGroups("Associate", "Regular");
		assertEquals("No user should be a member of the Associate group and Regular group", 0, result);
	}
	
	public void testUserAssociateOrRegular() 
	{
		int result = numViolationsOfInGroupXMustBeInYOrZ("Users", "Associate", "Regular");
		assertEquals("Every user should be a member of Associate or Regular", 0, result);
	}
	
	public void testAssociateUser() 
	{
		int result = numViolationsOfMutuallyInclusiveGroups("Associate", "Users");
		assertEquals("Every member of associate must be a registered User", 0, result);
	}
	
	public void testRegularUser() 
	{
		int result = numViolationsOfMutuallyInclusiveGroups("Regular", "Users");
		assertEquals("Every member of regular must be a registered User", 0, result);
	}
	
	////////////// chapter/no chapter
	
	public void testOrgChapterOrNoChapter() 
	{
		int result = numViolationsOfInGroupXMustBeInYOrZ("Org", "Chapter", "NoChapter");
		assertEquals("Every user should be a member of Chapter or NoChapter", 0, result);
	}
	
	public void testChapterOrg() 
	{
		int result = numViolationsOfMutuallyInclusiveGroups("Chapter", "Org");
		assertEquals("Every member of Chapter must be a registered User", 0, result);
	}
	
	public void testNoChapterOrg() 
	{
		int result = numViolationsOfMutuallyInclusiveGroups("NoChapter", "Org");
		assertEquals("Every member of NoChapter must be a registered User", 0, result);
	}
	
	public void testChapterOrNochapterInvariant()
	{
		int result = numViolationsOfMutuallyExclusiveGroups("Chapter", "NoChapter");
		assertEquals("No user should be a member of the Chapter group and NoChapter group", 0, result);
	}
	
	////////////// other chapter stuff
	
	public void testChapterMemberOfChapter() 
	{
		String sql = "SELECT count(*) " +
				"FROM roles r1, groups g " +
				"WHERE r1.end IS NULL " +
				"AND r1.groupid = g.id " +
				"AND g.name = 'Chapter' " +
				"AND NOT EXISTS ( " +
					"SELECT * " +
					"FROM roles r2, groupchapter gc " +
					"WHERE r2.userid = r1.userid " +
					"AND r2.end IS NULL " +
					"AND r2.groupid = gc.id " +
				") ";
		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("Every member of the Chapter group must be a member of a chapter", 0, result);
	}
	
	public void testNoChapterNotMemberOfChapter() 
	{
		String sql = "SELECT count(*) " +
				"FROM roles r1, groups g " +
				"WHERE r1.end IS NULL " +
				"AND r1.groupid = g.id " +
				"AND g.name = 'NoChapter' " +
				"AND EXISTS ( " +
					"SELECT * " +
					"FROM roles r2, groupchapter gc " +
					"WHERE r2.userid = r1.userid " +
					"AND r2.end IS NULL " +
					"AND r2.groupid = gc.id " +
					"AND r2.level = 'm' " +
				") ";
		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("Every member of the NoChapter group must not be a member of a chapter", 0, result);
	}
	
	public void testSingleChapterInvariant()
	{
		String sql = "SELECT count(*) " +
			"FROM roles r1, roles r2, groups g1, groups g2, groupchapter gc1, groupchapter gc2 " +
			"WHERE r1.userid = r2.userid " +
			"AND r1.groupid = g1.id " +
			"AND r2.groupid = g2.id " +
			"AND g1.id != g2.id " +
			"AND g1.id = gc1.id " +
			"AND g2.id = gc2.id " +
			"AND r1.level = 'm' " +
			"AND r2.level = 'm' " +
			"AND r1.end IS NULL " +
			"AND r2.end IS NULL ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No user should be a member of more than one chapter", 0, result);
	}
	
	public void testChapterLeaderExecMemberInvariant() {
		String sql = "SELECT count(*) " +
			"FROM roles r, groups g, groupchapter gc " +
			"WHERE r.groupid = g.id " +
			"AND g.id = gc.id " +
			"AND r.level = 'l' " +
			"AND r.end IS NULL " +
			"AND r.userid NOT IN ( " +
				"SELECT rs.userid " +
				"FROM roles rs, groups gs " +
				"WHERE rs.groupid = gs.id " +
				"AND gs.name = 'Chapter Executive Members (all chapters)' " +
				"AND rs.end IS NULL " +
			") ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("Every leader of a chapter should be a member of the Exec group", 0, result);
	}
	
	public void testExecMemberChapterLeaderInvariant() {
		//inverse of above
		String sql = "SELECT count(*) " +
			"FROM roles rs, groups gs " +
			"WHERE rs.groupid = gs.id " +
			"AND gs.name = 'exec' " +
			"AND rs.end IS NULL " +
			"AND rs.userid NOT IN ( " +
				"SELECT r.userid " +
				"FROM roles r, groups g, groupchapter gc " +
				"WHERE r.groupid = g.id " +
				"AND g.id = gc.id " +
				"AND r.level = 'l' " +
				"AND r.end IS NULL " +
			") ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("Every member of the exec group should be a leader of a chapter", 0, result);
	}
	
	public void testChapterLeaderChapterExecListLeaderInvariant() {
	
		String sql = "SELECT count(*) " +
			"FROM roles r, groups g, groupchapter gc " +
			"WHERE r.groupid = g.id " +
			"AND g.id = gc.id " +
			"AND r.level = 'l' " +
			"AND r.end IS NULL " +
			"AND r.userid NOT IN ( " +
				"SELECT rs.userid " +
				"FROM roles rs, groups gs, groupchapter gcs " +
				"WHERE rs.groupid = gs.id " +
				"AND gs.parent = gcs.id " +
				"AND gs.shortname = 'exec' " +
				"AND rs.level = 'l' " +
				"AND gcs.id = g.id " +
				"AND rs.end IS NULL " +
			") ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("Every leader of a chapter should be a leader of their chapter exec list", 0, result);
	}
	
	public void testChapterExecListLeaderChapterLeaderInvariant() {
		
		//Inverse of above
		String sql = "SELECT count(*) " +
			"FROM roles rs, groups gs, groupchapter gcs, groups g " +
			"WHERE rs.groupid = gs.id " +
			"AND gs.parent = gcs.id " +
			"AND gs.shortname = 'exec' " +
			"AND rs.level = 'l' " +
			"AND gcs.id = g.id " +
			"AND rs.end IS NULL " +
			"AND rs.userid NOT IN ( " +
				"SELECT r.userid " +
				"FROM roles r, groupchapter gc " +
				"WHERE r.groupid = g.id " +
				"AND g.id = gc.id " +
				"AND r.level = 'l' " +
				"AND r.end IS NULL " +
			") ";

		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("Every leader of a chapter exec list should be a leader of their chapter list", 0, result);
	}
	
	public void testNatlRepsAreExecOrNatlRepsInvariant() {
		List<GroupModel> nrLists = Helpers.getNationalRepLists(true, true);
		for(GroupModel nrList: nrLists)
		{
			int result = numViolationsOfInGroupXMustBeInYOrZ(nrList.getName(), "Chapter Executive Members (all chapters)", "National Reps");
			assertEquals("Every member of the " + nrList.getName() + " group should be in the Exec group or in the National Reps group", 0, result);
		}
	}

	public void testNatlRepOrExecInvariant()
	{
		int result = numViolationsOfMutuallyExclusiveGroups("Chapter Executive Members (all chapters)", "National Reps");
		assertEquals("No user should be a member of the Exec group and NatlRep group", 0, result);
	}

	public void testNatlRepChapterMemberInvariant()
	{
		int result = numViolationsOfMutuallyInclusiveGroups("National Reps", "Chapter");
		assertEquals("Every user who is a member of the NatlRep group should be a Chapter member", 0, result);
	}
	
	// Pro vs. Student Chapter tests
	public void testStudentChapterRepsNotInProRepOrExecGroups()
	{
		String sql = "SELECT count(*) " +
					"FROM groups g, users u, roles r1, roles r2, groupchapter c " +
					"WHERE ( " +
							"r1.end IS NULL " +
							"AND r2.end IS NULL) " +
						"AND " +
							"(r1.groupid = c.id " +
							"AND r1.userid = u.id " +
							"AND r1.level = 'm' " +
							"AND c.professional = false) " +
						"AND " +
							"(r2.groupid = g.id " +
							"AND r2.userid = u.id " +
							"AND ( g.nationalRepType IN ('p') " +
								"OR g.shortname LIKE 'ProChaptersExec' )" +
						")";
		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No student chapter members should be in professional rep groups", 0, result);
	}

	public void testProChapterRepsNotInStudentRepOrExecGroups()
	{
		String sql = "SELECT count(*) " +
					"FROM groups g, users u, roles r1, roles r2, groupchapter c " +
					"WHERE ( " +
							"r1.end IS NULL " +
							"AND r2.end IS NULL) " +
						"AND " +
							"(r1.groupid = c.id " +
							"AND r1.userid = u.id " +
							"AND r1.level = 'm' " +
							"AND c.professional = true) " +
						"AND " +
							"(r2.groupid = g.id " +
							"AND r2.userid = u.id " +
							"AND ( g.nationalRepType IN ('s') " +
							"OR g.shortname LIKE 'UniChaptersExec' )" +
						")";
		int result = ((BigInteger)session.createSQLQuery(sql).uniqueResult()).intValue();
		assertEquals("No professional chapter members should be in student rep groups", 0, result);
	}
	
	public void testUniExecMembersNotInProExecGroup(){
		int result = numViolationsOfMutuallyExclusiveGroups("Student Chapter Exec", "Professional Chapter Exec");
		assertEquals("Nobody should be in both the University Chapters Exec Group and the Pro Chapters Exec Group"
				, 0, result);
	}

	public void testProAndStudentExecInExecGroup(){
		int result = numViolationsOfMutuallyInclusiveGroups("Student Chapter Exec", "Chapter Executive Members (all chapters)");
		assertEquals("Users in the UniChaptersExec group should also be in the Exec group"
				, 0, result);
		result = numViolationsOfMutuallyInclusiveGroups("Professional Chapter Exec", "Chapter Executive Members (all chapters)");
		assertEquals("Users in the ProChaptersExec group should also be in the Exec group"
				, 0, result);
	}

	public void testExecMembersInStudentExecOrProExecGroup()
	{
		int result =  numViolationsOfInGroupXMustBeInYOrZ("Chapter Executive Members (all chapters)","Professional Chapter Exec","Student Chapter Exec");
		assertEquals("Members of the Exec must be Pro or Uni Exec", 0, result);
	}
	////////////// generic helpers for some of the above tests
	
	private int numViolationsOfMutuallyExclusiveGroups(String g1, String g2) 
	{
		String sql = "SELECT count(*) " +
		"FROM roles r1, roles r2, groups g1, groups g2 " +
		"WHERE r1.userid = r2.userid " +
		"AND r1.groupid = g1.id " +
		"AND r2.groupid = g2.id " +
		"AND g1.name = ? " +
		"AND g2.name = ? " +
		"AND r1.end IS NULL " +
		"AND r2.end IS NULL ";

		return((BigInteger)session.createSQLQuery(sql).setString(0, g1).setString(1, g2).uniqueResult()).intValue();
	}

	private int numViolationsOfMutuallyInclusiveGroups(String g1, String g2) {
		String sql = "SELECT count(*) " +
			"FROM roles r, groups g " +
			"WHERE r.groupid = g.id " +
			"AND r.end IS NULL " +
			"AND g.name = ? " +
			"AND r.userid NOT IN ( " +
				"SELECT rs.userid " +
				"FROM roles rs, groups gs " +
				"WHERE rs.groupid = gs.id " +
				"AND gs.name = ? " +
				"AND rs.end IS NULL " +
			") ";

		int result = ((BigInteger)session.createSQLQuery(sql).setString(0, g1).setString(1, g2).uniqueResult()).intValue();
		return result;
	}

	private int numViolationsOfInGroupXMustBeInYOrZ(String x, String y, String z) {
		String sql = "SELECT count(*) " +
		"FROM roles r1, groups g1 " +
		"WHERE r1.groupid = g1.id " +
		"AND r1.end IS NULL " +
		"AND g1.name = ? " +
		"AND NOT EXISTS (" +
			"SELECT * " +
			"FROM roles r2, groups g2 " +
			"WHERE r1.userid = r2.userid " +
			"AND r2.end IS NULL " +
			"AND r2.groupid = g2.id " +
			"AND (g2.name = ? OR g2.name = ?) " +
		") ";

		int result = ((BigInteger)session.createSQLQuery(sql).setString(0, x).setString(1, y).setString(2, z).uniqueResult()).intValue();
		return result;
	}

}
