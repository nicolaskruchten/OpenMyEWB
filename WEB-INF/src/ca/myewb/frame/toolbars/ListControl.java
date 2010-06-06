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

package ca.myewb.frame.toolbars;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Permissions;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class ListControl extends Toolbar
{
	public ListControl(Context ctx, UserModel currentUser, GroupModel list)
	            throws Exception
	{
		super();
		this.title = "Actions";
		this.template = "frame/toolbars/listcontrol.vm";

		//the only lists that can be edited or deleted are non-admin, non-chapter, non-exec lists: canbecontrolledby
		//the only lists that can be managed are non-admin lists: canbecontrolledby
		//the only lists that can be joined are non-admin lists: non-level
		//the only lists that can be left are non-admin lists: level
		//the only lists where one can request a CSV are lists: canbecontrolledby
		//any list can be sent to: canbecontrolledby
		if (list.getAdmin())
		{
			ctx.put("listIsAdmin", "yes");
		}
		else
		{
			ctx.put("listIsAdmin", "no");
		}

		if (list.isChapter())
		{
			ctx.put("isChapter", "yes");
		}
		else
		{
			ctx.put("isChapter", "no");
		}

		if (list.isExecList())
		{
			ctx.put("isExec", "yes");
		}
		else
		{
			ctx.put("isExec", "no");
		}

		String level = null;

		if (currentUser.isLeader(list, false))
		{
			level = "leader";
		}
		else if (currentUser.isSender(list, false))
		{
			level = "sender";
		}
		else if (currentUser.isMember(list, false)
		         || currentUser.isRecipient(list, false))
		{
			level = "recipient";
		}

		ctx.put("level", level);

		if (Permissions.canControlGroup(currentUser, list))
		{
			ctx.put("isLeader", "yes");
		}
	}
}
