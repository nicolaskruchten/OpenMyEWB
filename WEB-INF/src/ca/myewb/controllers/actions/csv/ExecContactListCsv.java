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

import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.SafeHibList;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class ExecContactListCsv extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupModel list = (GroupModel)getAndCheckFromUrl(GroupModel.class);
		
		
		
		if (!(list.getAdmin() && list.getVisible()))
		{
			throw getSecurityException("Invalid request",
			                           path + "/mailing/Mailing");
		}
		
		List<UserModel> leaders = (new SafeHibList<UserModel>(hibernateSession.createQuery("SELECT u FROM UserModel u, RoleModel r "
		                                                                         + "WHERE r.user=u AND r.group.id=? AND r.end IS NULL GROUP BY u")
		                                            .setInteger(0, list.getId())))
		                     .list();

		Vector<String[]> csvData = new Vector<String[]>();

		csvData.add(new String[]{"First Name", "Last Name", "Email", "Phone", "Title", "Chapter"});
		
		
		Iterator listMembers = leaders.iterator();

		while (listMembers.hasNext())
		{
			UserModel theMember = (UserModel)listMembers.next();
			
			String chapterName = "";
			GroupChapterModel chapter = theMember.getChapter();
			if (chapter != null)
			{
				chapterName = chapter.getName();
			}
			
			csvData.add(new String[]
			            {
			                theMember.getFirstname(),
			                theMember.getLastname(), 
			                theMember.getEmail(), 
			                theMember.getPhone(), 
			                theMember.getExecTitle(),
			                chapterName
			            });
			
		}

		try
		{
			this.setInterpageVar("csvData", csvData);
			this.setInterpageVar("csvFileName", "execlist.csv");
		}
		catch(IllegalStateException e)
		{
			//session timeout!
			throw new IllegalStateException("Session timeout on CSV!", e);
		}
		
		throw new RedirectionException(path + "/csvfile/execlist.csv");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");
		return s;
	}
	
	public String oldName()
	{
		return "ExecContactListCsv";
	}
}
