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
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.Member;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.forms.UserEmailForm;
import ca.myewb.frame.toolbars.FindMemberToolbar;
import ca.myewb.frame.toolbars.OtherUpgrade;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.UserModel;


public class MemberInfo extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		setInterpageVar("membersearchtarget", path + "/chapter/MemberInfo");

		urlParams.processParams(new String[]{"mode"}, new String[]{"new"});

		UserModel targetUser = new Member(httpSession, hibernateSession,
		                             requestParams, urlParams, currentUser).view(ctx);

		Vector<Toolbar> toolbars = new Vector<Toolbar>();
		if (targetUser == null)
		{
			toolbars.add(new FindMemberToolbar());
			ctx.put("memberpage", "frame/findmember.vm");
		}
		else
		{
			if (!Permissions.canReadPersonalDetails(currentUser, targetUser))
			{
				if(targetUser.isAdmin())
				{
					throw getSecurityException("That member is in your chapter, and normally you would " +
							"be able to access their user page, but they are listed as an administrator, " +
							"and so their account info is locked to you. " + 
							"You can still add/remove them from any mailing list using " +
							"the 'list member mgmt' page of that list.",
					                           path + "/chapter/MemberInfo");
				}
				else
				{
					throw getSecurityException("That member isn't a member of your chapter, but shows up in this search because they are on one of your chapter's mailing lists... " +
							"You can still add/remove them from any mailing list using " +
							"the 'list member mgmt' page of that list.", path + "/chapter/MemberInfo");
				}
			}
			
			Member.viewMember(ctx, targetUser, true, false);
			
			if(currentUser.isAdmin())
			{
				UserEmailForm f = (UserEmailForm)checkForValidationFailure(ctx);
				if(f == null)
				{
					f = new UserEmailForm(path + "/actions/SaveEmailAddresses/" + targetUser.getId(),
	                        requestParams);
					f.setValue("Email", targetUser.getEmail());
					f.setValue("Emails", targetUser.getFormattedEmailList());
				}
				ctx.put("form2", f);
			}

			OtherUpgrade theToolbar = new OtherUpgrade(currentUser, targetUser);
			theToolbar.setUpCtx(ctx);
			toolbars.add(theToolbar);
			ctx.put("memberpage", "frame/memberinfo.vm");
		}
		ctx.put("toolbars", toolbars);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");
		s.add("NMT");

		return s;
	}

	public String displayName()
	{
		return "Member Mgmt";
	}

	public List<String> getNeededInterpageVars()
	{
		return Member.getRequiredInterpageVars();
	}

	public int weight()
	{
		return 2;
	}
}
