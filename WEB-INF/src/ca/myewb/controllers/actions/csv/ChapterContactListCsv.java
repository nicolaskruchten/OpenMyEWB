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


public class ChapterContactListCsv extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		Vector<String[]> csvData = new Vector<String[]>();
		csvData.add(new String[] {
					"Name",
					"Email",
					"Phone",
					"Fax",
					"Address1",
					"Suite",
					"Address2",
					"City",
					"Province",
					"Postal Code"
				});
		String query = "select g.name, email, c.phone, c.fax, "
			+ "c.address1, c.suite, c.address2, c.city, c.province, c.postalcode "
			+ "from groups g, groupchapter c where g.id=c.id";
		
		List memberList = hibernateSession.createSQLQuery(query)
			.addScalar("name", Hibernate.STRING)
			.addScalar("email", Hibernate.STRING)
			.addScalar("phone", Hibernate.STRING)
			.addScalar("fax", Hibernate.STRING)
			.addScalar("address1", Hibernate.STRING)
			.addScalar("suite", Hibernate.STRING)
			.addScalar("address2", Hibernate.STRING)
			.addScalar("city", Hibernate.STRING)
			.addScalar("province", Hibernate.STRING)
			.addScalar("postalcode", Hibernate.STRING)
			.list();


		Iterator members = memberList.iterator();

		while (members.hasNext())
		{
			csvData.add(toStringArray((Object[])members.next()));
		}

		
		try
		{
			this.setInterpageVar("csvData", csvData);
			this.setInterpageVar("csvFileName", "chapters.csv");
		}
		catch(IllegalStateException e)
		{
			//session timeout!
			throw new IllegalStateException("Session timeout on CSV!", e);
		}

		throw new RedirectionException(path + "/csvfile/chapters.csv");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "ChapterContactListCsv";
	}
}
