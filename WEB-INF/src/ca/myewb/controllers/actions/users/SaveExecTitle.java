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

package ca.myewb.controllers.actions.users;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ExecTitleForm;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class SaveExecTitle extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ExecTitleForm form = new ExecTitleForm(path + "/actions/SaveExecTitle",
		                                       requestParams, true, true);

		Message m = form.validate();

		if (m != null)
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path + "/chapter/ExecTitle/"
			                             + form.getParameter("Targetid"));
		}

		// No messages: form validated
		UserModel targetUser = (UserModel)getAndCheck(UserModel.class,
		                                    new Integer(form.getParameter("Targetid")));
		boolean exec = targetUser.isMember(Helpers.getGroup("Exec"));
		boolean natlRep = targetUser.isMember(Helpers.getGroup("NatlRep"));


		if (!Permissions.canUpdateUserStatus(currentUser, targetUser))
		{
			throw getSecurityException("You don't have the right permissions to do this!",
			                           path + "/chapter/MemberInfo");
		}

		if (!exec && !natlRep)
		{
			throw getSecurityException("That person is not an exec!",
			                           path + "/chapter/MemberInfo");
		}

		String title = form.getParameter("Title");


		
		// Manage National Rep lists
		Hashtable<GroupModel, Boolean> repLists = new Hashtable<GroupModel, Boolean>();
		List<GroupModel> repLists2 = Helpers.getNationalRepLists(true, true);
		for(GroupModel grp: repLists2)
		{
			repLists.put(grp, form.getParameter(grp.getShortname()).equals("on"));
		}
		
		// Do the actual change
		if(exec)
		{
			targetUser.changeExecTitle(title, repLists);
		}
		else
		{
			targetUser.changeNatlRepTitle(title, repLists);
		}

		// Leave a message in the session
		setSessionMessage(("Exec title info saved!"));

		// Redirect to somewhere
		throw new RedirectionException(path + "/chapter/MemberInfo/" + targetUser.getId());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}
	
	public String oldName()
	{
		return "SaveExecTitle";
	}
}
