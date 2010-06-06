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
import ca.myewb.frame.RedirectionException;


public class NmtContactListCsv extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		Vector<String[]> csvData = new Vector<String[]>();
		csvData.add(new String[] {
					"Firstname",
					"Lastname",
					"Email",
					"Title",
					"Work Phone",
					"Main Phone",
					"Alternate Phone",
					"Cell Phone",
					"Address1",
					"Suite",
					"Address2",
					"City",
					"Province",
					"Postal Code",
					"Country"
				});
		String query = "select u.firstname, u.lastname, u.email, r.title, u.businessno, u.phone, u.alternateno, u.cellno, "
			+ "u.address1, u.suite, u.address2, u.city, u.province, u.postalcode, u.country "
			+ "from users u, roles r where r.userid=u.id and r.groupid=3 and r.end is null";
		
		List memberList = hibernateSession.createSQLQuery(query)
			.addScalar("firstname", Hibernate.STRING)
			.addScalar("lastname", Hibernate.STRING)
			.addScalar("email", Hibernate.STRING)
			.addScalar("title", Hibernate.STRING)
			.addScalar("businessno", Hibernate.STRING)
			.addScalar("phone", Hibernate.STRING)
			.addScalar("alternateno", Hibernate.STRING)
			.addScalar("cellno", Hibernate.STRING)
			.addScalar("address1", Hibernate.STRING)
			.addScalar("suite", Hibernate.STRING)
			.addScalar("address2", Hibernate.STRING)
			.addScalar("city", Hibernate.STRING)
			.addScalar("province", Hibernate.STRING)
			.addScalar("postalcode", Hibernate.STRING)
			.addScalar("country", Hibernate.STRING)
			.list();


		Iterator members = memberList.iterator();

		while (members.hasNext())
		{
			csvData.add(toStringArray((Object[])members.next()));
		}

		
		try
		{
			this.setInterpageVar("csvData", csvData);
			this.setInterpageVar("csvFileName", "officecontacts.csv");
		}
		catch(IllegalStateException e)
		{
			//session timeout!
			throw new IllegalStateException("Session timeout on CSV!", e);
		}

		throw new RedirectionException(path + "/csvfile/officecontacts.csv");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "NmtContactListCsv";
	}
}
