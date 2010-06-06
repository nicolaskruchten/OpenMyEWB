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

package ca.myewb.controllers.mailing;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.forms.ListMembershipForm;
import ca.myewb.frame.toolbars.ListControl;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.GroupModel;


public class ListMgmt extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupModel list = (GroupModel)getAndCheckFromUrl(GroupModel.class);

		if (!Permissions.canAdministerGroupMembership(currentUser, list))
		{

			throw getSecurityException("Can't manage this group's membership!",
			                           path + "/mailing/ListInfo/"
			                           + urlParams.getParam());
		}

		ctx.put("list", list);

		ListMembershipForm form = (ListMembershipForm)checkForValidationFailure(ctx);

		if (form == null)
		{
			// First try: create a fresh form
			setInterpageVar("isLeaderForm", true);

			if (list.isChapter() || list.isExecList())
			{
				form = new ListMembershipForm(path
				                              + "/actions/ModifyListMembership/"
				                              + list.getId(), requestParams,
				                              true, false);
				setInterpageVar("isNormalListForm", false);
			}
			else
			{
				form = new ListMembershipForm(path
				                              + "/actions/ModifyListMembership/"
				                              + list.getId(), requestParams,
				                              true, true);
				setInterpageVar("isNormalListForm", true);
			}
		}

		ctx.put("form", form);

		Vector<Toolbar> toolbars = new Vector<Toolbar>();
		ListControl theToolbar = new ListControl(ctx, currentUser, list);
		toolbars.add(theToolbar);
		ctx.put("toolbars", toolbars);
	}

	public List<String> getNeededInterpageVars()
	{
		Vector<String> vars = new Vector<String>();
		vars.add("isLeaderForm");
		vars.add("isNormalListForm");

		return vars;
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}

	public String displayName()
	{
		return "Mailing List Mgmt";
	}

	public int weight()
	{
		return -1;
	}
}
