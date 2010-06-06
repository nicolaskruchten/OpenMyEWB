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

package ca.myewb.controllers.volunteering;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.Member;
import ca.myewb.frame.Controller;
import ca.myewb.frame.forms.OVAssignmentForm;
import ca.myewb.frame.toolbars.FindMemberToolbar;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.PlacementModel;
import ca.myewb.model.UserModel;


public class AssignOv extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		setInterpageVar("membersearchtarget", path + "/volunteering/AssignOv");

		urlParams.processParams(new String[]{"mode"}, new String[]{"new"});

		UserModel targetUser = new Member(httpSession, hibernateSession,
		                             requestParams, urlParams, currentUser).view(ctx);

		if (targetUser == null)
		{
			Vector<Toolbar> toolbars = new Vector<Toolbar>();
			toolbars.add(new FindMemberToolbar());
			ctx.put("toolbars", toolbars);
			ctx.put("memberpage", "frame/findmember.vm");
		}
		else
		{
			Member.viewMember(ctx, targetUser, false, false);

			List<PlacementModel> unassignedPlacements = PlacementModel.getUnassignedPlacements();
			
			ctx.put("form2", new OVAssignmentForm(path + "/actions/DoOvAssignment/" + targetUser.getId(),
                    requestParams,
                    unassignedPlacements));
			ctx.put("memberpage", "frame/memberinfo.vm");
		}
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}

	public String displayName()
	{
		return "Assignment";
	}

	public List<String> getNeededInterpageVars()
	{
		return Member.getRequiredInterpageVars();
	}

	public int weight()
	{
		return 30;
	}
}
