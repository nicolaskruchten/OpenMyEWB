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

package ca.myewb.controllers.chapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.forms.ExecTitleForm;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class ExecTitle extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		UserModel targetUser = (UserModel)getAndCheckFromUrl(UserModel.class);
		boolean exec = targetUser.isMember(Helpers.getGroup("Exec"));
		boolean natlRep = targetUser.isMember(Helpers.getGroup("NatlRep"));

		if (!Permissions.canUpdateUserStatus(currentUser, targetUser))
		{
			throw getSecurityException("You don't have the right permissions to do this!",
			                           path + "/chapter/MemberInfo");
		}

		if (!exec && !natlRep )
		{
			throw getSecurityException("That person is not an exec!",
			                           path + "/chapter/MemberInfo");
		}

		// And show the form
		ExecTitleForm f = (ExecTitleForm)checkForValidationFailure(ctx);

		if (f == null)
		{
			// Create a new form
			f = new ExecTitleForm(path + "/actions/SaveExecTitle", requestParams, !targetUser.getChapter().isProfessional(), targetUser.getChapter().isProfessional());
			if(exec)
			{
				f.setValue("Title", targetUser.getExecTitle());
			}
			else
			{
				f.setValue("Title", targetUser.getNatlRepTitle());
			}

			List<GroupModel> repLists2 = Helpers.getNationalRepLists(!targetUser.getChapter().isProfessional(), targetUser.getChapter().isProfessional());
			for(GroupModel grp: repLists2)
			{
				if (targetUser.isMember(grp, false))
				{
					f.setValue(grp.getShortname(), "on");
				}
			}

			f.setValue("Targetid", urlParams.getParam());
		}

		ctx.put("form", f);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

		return s;
	}

	public String displayName()
	{
		return "Exec Title";
	}
}
