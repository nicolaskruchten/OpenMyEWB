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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Projections;

import ca.myewb.controllers.common.Member;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.SafeHibList;
import ca.myewb.frame.forms.Form;
import ca.myewb.frame.forms.JoinListForm;
import ca.myewb.frame.toolbars.FindMemberToolbar;
import ca.myewb.frame.toolbars.ListMemberControl;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class ListMember extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		setInterpageVar("membersearchtarget", path + "/mailing/ListMember");

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
				}			}

			Member.viewMember(ctx, targetUser, false, false);
			
			addFormsToCtx(ctx, targetUser);

			toolbars.add(new ListMemberControl());
			ctx.put("memberpage", "frame/memberinfo.vm");
		}
		ctx.put("toolbars", toolbars);
	}

	private void addFormsToCtx(Context ctx, UserModel targetUser) throws Exception
	{
		// Find all mailing lists that the current user (not the target user) can modify
		List<GroupModel> lists = getControllableGroups();

		// Remove form
		Form f;
		HashSet<GroupModel> hs = new HashSet<GroupModel>();
		hs.addAll(targetUser.getGroups());

		List<GroupModel> lists2 = new ArrayList<GroupModel>();
		lists2.addAll(hs);
		lists2.retainAll(lists);

		setInterpageVar("isJoinListForm", "yes");

		//leave form
		f = new JoinListForm(path + "/actions/ModifyListMembership",
		                     requestParams, lists2);
		f.setValue("ActionType", "remove");
		f.setValue("Emails", targetUser.getEmail());
		ctx.put("showform2", !lists2.isEmpty());
		ctx.put("form2", f);

		//join form
		lists.removeAll(targetUser.getGroups());
		f = new JoinListForm(path + "/actions/ModifyListMembership",
		                     requestParams, lists);
		f.setValue("ActionType", "add");
		f.setValue("Emails", targetUser.getEmail());
		ctx.put("showform3", !lists.isEmpty());
		ctx.put("form3", f);
	}

	private List<GroupModel> getControllableGroups()
	{
		List<GroupModel> lists;

		if (currentUser.isAdmin())
		{
			lists = (new SafeHibList<GroupModel>(hibernateSession.createQuery("SELECT g FROM GroupModel g where g.visible=true and g.admin=false")))
			        .list();
		}
		else
		{
			Criteria crit = hibernateSession.createCriteria(GroupModel.class);

			crit.createAlias("roles", "r");
			crit.add(Restrictions.eq("r.user", currentUser));
			crit.add(Restrictions.isNull("r.end"));
			crit.add(Restrictions.eq("r.level", new Character('l')));
			crit.add(Restrictions.eq("visible", new Boolean(true)));
			crit.add(Restrictions.eq("admin", new Boolean(false)));

			crit.setProjection(Projections.groupProperty("id"));
			lists = (new SafeHibList<GroupModel>(crit)).list();

			Iterator it = lists.iterator();
			List<GroupModel> lists2 = new ArrayList<GroupModel>();

			while (it.hasNext())
			{
				lists2.add((GroupModel)hibernateSession.get(GroupModel.class,
				                                       (Integer)it.next()));
			}

			lists = lists2;
			lists.addAll(currentUser.getChapter().getVisibleChildren());
		}

		return lists;
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Exec");

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
		return 95;
	}
}
