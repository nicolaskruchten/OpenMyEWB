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
import org.hibernate.SQLQuery;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.GroupModel;


public class RenewalList extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		if(urlParams.getParam() == null && !currentUser.isAdmin())
		{
			throw getSecurityException("You don't have the right permissions to view a full upgrades list!",
                    path + "/chapter/ChapterList");
		}
		
		GroupModel chapter = null;
		
		if(urlParams.getParam() != null)
		{
			chapter = (GroupModel)getAndCheckFromUrl(GroupModel.class);
			
			if (!Permissions.canAdministerGroupMembership(currentUser, chapter))
			{
				throw getSecurityException("You don't have the right permissions to view an upgrades list!",
				                           path + "/chapter/ChapterInfo/" + chapter.getId());
			}
		}

		Vector<String[]> csvData = new Vector<String[]>();
		csvData.add(new String[] {
					"Upgrade Date",
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
					"Renewal?",
					"Student?",
					"Upgraded by",
					"Language"
				});
		String query = "select " +
		"r.start, " +
		"u.firstname, " +
		"u.lastname, " +
		"u.email, " +
		"u.phone, " +
		"u.address1, " +
		"u.suite, " +
		"u.address2, " +
		"u.city, " +
		"u.province, " +
		"u.postalcode, " +
		"u.country, " +
		"(case when prev.id is null then 'no' else 'yes' end) as renewal, " +
		"(case when u.student = 'n' then 'no' else 'yes' end) as student, " +
		"(case when u.upgradelevel = 'n' then 'National' when u.upgradelevel='c' then 'Chapter' else 'Unknown' end) as upgradedby, " +
		"(case when u.language = 'fr' then 'fr' else 'en' end) as language " +
		"from users u " +
		"left join roles prev on prev.userid = u.id and prev.end is not null and prev.groupid=6 " +
		"join roles r on r.userid=u.id and r.groupid=6 and r.end is null "; 
		
		if(chapter != null)
		{
			query += "join roles chapter on chapter.userid=u.id and chapter.groupid=? and chapter.end is null and chapter.level='m' ";
		}
		
		query += "group by u.id order by r.start desc limit 0, 800";
		
		SQLQuery hibQuery = hibernateSession.createSQLQuery(query)
			.addScalar("start", Hibernate.STRING)
			.addScalar("firstname", Hibernate.STRING)
			.addScalar("lastname", Hibernate.STRING)
			.addScalar("email", Hibernate.STRING)
			.addScalar("phone", Hibernate.STRING)
			.addScalar("address1", Hibernate.STRING)
			.addScalar("suite", Hibernate.STRING)
			.addScalar("address2", Hibernate.STRING)
			.addScalar("city", Hibernate.STRING)
			.addScalar("province", Hibernate.STRING)
			.addScalar("postalcode", Hibernate.STRING)
			.addScalar("country", Hibernate.STRING)
			.addScalar("renewal", Hibernate.STRING)
			.addScalar("student", Hibernate.STRING)
			.addScalar("upgradedby", Hibernate.STRING)
			.addScalar("language", Hibernate.STRING);
		
		if(chapter != null)
		{
			hibQuery.setInteger(0,chapter.getId());
		}
		
		List memberList = hibQuery.list();


		Iterator members = memberList.iterator();

		while (members.hasNext())
		{
			csvData.add(toStringArray((Object[])members.next()));
		}

		
		try
		{
			this.setInterpageVar("csvData", csvData);
			this.setInterpageVar("csvFileName", "upgradeslist.csv");
		}
		catch(IllegalStateException e)
		{
			//session timeout!
			throw new IllegalStateException("Session timeout on CSV!", e);
		}

		throw new RedirectionException(path + "/csvfile/upgradeslist.csv");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}
	
	public String oldName()
	{
		return "RenewalList";
	}
}
