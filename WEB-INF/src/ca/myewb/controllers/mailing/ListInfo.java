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
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.PostList;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.Form;
import ca.myewb.frame.toolbars.ListControl;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.GroupModel;


public class ListInfo extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		// Show requested group
		GroupModel list = (GroupModel)getAndCheckFromUrl(GroupModel.class);

		if (!Permissions.canReadGroupInfo(currentUser, list))
		{
			if (currentUser.getUsername().equals("guest"))
			{
				setInterpageVar("requestedURL",
				                path + "/mailing/ListInfo/"
				                + urlParams.getParam());
				setSessionMessage(("Please sign in to see the page you requested"));
				throw new RedirectionException(path + "/home/SignIn");
			}

			throw getSecurityException("Can't access this list!",
			                           path + "/mailing/Mailing");
		}

		ctx.put("list", list);

		if (currentUser.getUsername().equals("guest"))
		{
			Form theForm = checkForValidationFailure(ctx);

			if (theForm != null)
			{
				// guest signing up failed from ModListMembership
				ctx.put("form", theForm);
			}
		}
		else if(list.getVisible()) //non-guests get a toolbar
		{
			Vector<Toolbar> toolbars = new Vector<Toolbar>();
			ListControl theToolbar = new ListControl(ctx, currentUser, list);
			toolbars.add(theToolbar);
			ctx.put("toolbars", toolbars);
		}

		(new PostList(httpSession, hibernateSession, requestParams, urlParams,
		              currentUser)).list(ctx, "listposts", 15);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Mailing List Info";
	}

	public int weight()
	{
		return -1;
	}
}
