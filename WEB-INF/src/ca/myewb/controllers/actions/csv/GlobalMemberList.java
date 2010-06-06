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
import ca.myewb.frame.RedirectionException;


public class GlobalMemberList extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		Vector<String[]> csvData = new Vector<String[]>();
		csvData.add(new String[] {
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
					"Student?",
					"Language",
					"Current?"
				});
		String query = "select " +
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
				"(case when u.student = 'n' then 'no' else 'yes' end) as student, " +
				"(case when u.language = 'fr' then 'fr' else 'en' end) as language, " +
				"(case when u.expiry is null then 'no' else 'yes' end) as current " +
				"from users u, roles r " +
				"where u.id=r.userid and r.groupid=6 " +
				"and u.email is not null and u.email !='' " +
				"group by u.id"; 
		
		SQLQuery hibQuery = hibernateSession.createSQLQuery(query)
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
			.addScalar("student", Hibernate.STRING)
			.addScalar("language", Hibernate.STRING)
			.addScalar("current", Hibernate.STRING);
	
		
		List memberList = hibQuery.list();


		Iterator members = memberList.iterator();

		while (members.hasNext())
		{
			csvData.add(toStringArray((Object[])members.next()));
		}

		
		try
		{
			this.setInterpageVar("csvData", csvData);
			this.setInterpageVar("csvFileName", "members.csv");
		}
		catch(IllegalStateException e)
		{
			//session timeout!
			throw new IllegalStateException("Session timeout on CSV!", e);
		}

		throw new RedirectionException(path + "/csvfile/members.csv");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
}
