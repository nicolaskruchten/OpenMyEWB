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
import ca.myewb.model.GroupModel;


public class MailingListCsv extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupModel list = (GroupModel)getAndCheckFromUrl(GroupModel.class);
		
		if(list.getAdmin() && list.getVisible())
		{
			throw new RedirectionException(path + "/actions/ExecContactListCsv/" + list.getId());
		}
		
		if (!Permissions.canAdministerGroupMembership(currentUser, list))
		{
			throw getSecurityException("You don't have the right permissions to view a member list!",
			                           path + "/mailing/ListInfo/"
			                           + list.getId());
		}

		Vector<String[]> csvData = new Vector<String[]>();


		csvData.add(new String[]{"First Name", "Last Name", "Email", "Status"});
		

		String query = "SELECT u.firstname, u.lastname, u.email, ";
		query += "(CASE WHEN (r.level='m' OR r.level='r') THEN 'recipient' WHEN r.level='s' THEN 'sender' ELSE 'leader' END) as status ";
		query += "FROM users u, roles r WHERE r.userid=u.id AND r.groupid=? AND r.end IS NULL " +
				"ORDER BY (CASE WHEN (r.level='m' OR r.level='r') THEN 1 WHEN r.level='s' THEN 2 ELSE 3 END) desc";
		List memberList = hibernateSession.createSQLQuery(query)
							.addScalar("firstname")
							.addScalar("lastname")
							.addScalar("email")
							.addScalar("status", Hibernate.STRING)
							.setInteger(0,list.getId()).list();


		Iterator members = memberList.iterator();
		
		String[] lastValues = null;
		
		while (members.hasNext())
		{
			String[] theseValues = toStringArray((Object[])members.next());
			if((lastValues != null) && !theseValues[2].equals(lastValues[2]))
			{
				csvData.add(lastValues); //output last row of r, r/s or r/l possibilities
			}
			lastValues = theseValues;
		}
		csvData.add(lastValues);

		try
		{
			this.setInterpageVar("csvData", csvData);
			this.setInterpageVar("csvFileName", "mailinglist.csv");
		}
		catch(IllegalStateException e)
		{
			//session timeout!
			throw new IllegalStateException("Session timeout on CSV!", e);
		}
		
		throw new RedirectionException(path + "/csvfile/mailinglist.csv");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
	
	public String oldName()
	{
		return "MailingListCsv";
	}
}
