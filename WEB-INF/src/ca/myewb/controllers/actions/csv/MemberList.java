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

package ca.myewb.controllers.actions.csv;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.hibernate.Hibernate;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.GroupChapterModel;


public class MemberList extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupChapterModel chapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class);

		if (!Permissions.canAdministerGroupMembership(currentUser, chapter))
		{
			throw getSecurityException("You don't have the right permissions to view a member list!",
			                           path + "/chapter/ChapterInfo/"
			                           + chapter.getId());
		}

		Vector<String[]> csvData = new Vector<String[]>();
		csvData.add(new String[] {
					"Join Date",
					"Status",
					"First name",
					"Last name",
					"Email",
					"Phone",
					"Address1",
					"Suite",
					"Address2",
					"City",
					"Province",
					"Postal Code",
					"Country",
					"Student Number",
					"Educational Institution",
					"Field of Study",
					"Sector of Employment",
					"Employer",
					"Position",
				});

		String query = "SELECT " 
			+ "r.start, " 
			+ "u.firstname, " 
			+ "u.lastname, " 
			+ "u.email, " 
			+ "u.phone, " 
			+ "(CASE WHEN u.expiry IS NOT NULL THEN 'regular' WHEN u.username!='' " +
					"THEN 'associate' ELSE 'mailing list' END) as status, " 
			+ "u.address1, " 
			+ "u.suite, " 
			+ "u.address2, " 
			+ "u.city, " 
			+ "u.province, " 
			+ "u.postalcode, " 
			+ "u.country, "
			+ "u.studentnumber, u.studentinstitution, u.studentfield, "
			+ "u.prosector, u.proemployer, u.proposition "
			+ "FROM users u, roles r " 
			+ "WHERE r.userid=u.id AND r.groupid=? AND r.level = 'm' AND r.end IS NULL " 
			+ "GROUP BY u.email " 
			+ "ORDER BY (CASE WHEN u.expiry IS NOT NULL THEN 1 WHEN u.username!='' " +
					"THEN 2 ELSE 3 END) asc, r.start desc, firstname asc";
		List memberList = hibernateSession.createSQLQuery(query)
							.addScalar("start", Hibernate.STRING)
							.addScalar("status", Hibernate.STRING)
							.addScalar("firstname")
							.addScalar("lastname")
							.addScalar("email")
							.addScalar("phone")
							.addScalar("address1")
							.addScalar("suite")
							.addScalar("address2")
							.addScalar("city")
							.addScalar("province")
							.addScalar("postalcode")
							.addScalar("country")
							.addScalar("studentnumber")
							.addScalar("studentinstitution")
							.addScalar("studentfield")
							.addScalar("prosector")
							.addScalar("proemployer")
							.addScalar("proposition")
							.setInteger(0,chapter.getId()).list();


		Iterator members = memberList.iterator();

		while (members.hasNext())
		{
			csvData.add(toStringArray((Object[])members.next()));
		}

		try
		{
			this.setInterpageVar("csvData", csvData);
			this.setInterpageVar("csvFileName", "memberlist.csv");
		}
		catch(IllegalStateException e)
		{
			//session timeout!
			throw new IllegalStateException("Session timeout on CSV!", e);
		}

		throw new RedirectionException(path + "/csvfile/memberlist.csv");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}
	
	public String oldName()
	{
		return "MemberList";
	}
}
