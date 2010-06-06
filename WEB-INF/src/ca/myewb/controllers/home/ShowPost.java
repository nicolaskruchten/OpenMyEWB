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

package ca.myewb.controllers.home;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ReplyEditForm;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;


public class ShowPost extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		// Retrieve requested post
		PostModel p = (PostModel)getAndCheckFromUrl(PostModel.class);
		
		if (!Permissions.canReadPost(currentUser, p))
		{
			if (currentUser.getUsername().equals("guest"))
			{
				setInterpageVar("requestedURL",
				                path + "/home/ShowPost/" + urlParams.getParam());
				setSessionMessage(("Please sign in to see the post you requested"));
				throw new RedirectionException(path + "/home/SignIn");
			}


			if(p.getPoster().equals(currentUser))
			{
				throw getSecurityException("You wrote this post, " +
						"but no longer belong to the group to which it was posted, so" +
						" you may not view it in full or reply to it. " +
						"This is a system limitation of, sorry!",
                        path + "/home/Posts");
			}
			else
			{
				throw getSecurityException("You can't see that post!",
                        path + "/home/Posts");
			}
		}

		if (p.getParent() != null)
		{
			throw new RedirectionException(path + "/home/ShowPost/"
			                               + p.getParent().getId());
		}

		log.debug("Showing post: " + urlParams.getParam());

		// Retrieve replies
		List replies  = hibernateSession.createQuery("FROM PostModel WHERE parent=? ORDER BY date ASC")
		          .setInteger(0, p.getId()).list();

		// If user is logged in, let them reply
		GroupModel group = p.getGroup();
				
		if (!currentUser.getUsername().equals("guest") && group.getVisible() && !p.hasActiveWhiteboard())
		{
				boolean canSend = (group.getId() != 1) && !(group.isChapter()) 
				&& (Permissions.canSendEmailToGroup(currentUser, group));
				
				ctx.put("canSend", canSend);

				ReplyEditForm form = (ReplyEditForm)checkForValidationFailure(ctx);

				if (form == null)
				{
					
					form = new ReplyEditForm(path + "/actions/SaveReply/"
					                         + urlParams.getParam(), requestParams,
					                         canSend, group.getTotalShortname());
				}

				ctx.put("form", form);
		}

		// Put it into the context for the template to display
		ctx.put("post", p);
		ctx.put("replies", replies);

		if (currentUser.isLeader(p.getGroup(), true))
		{
			ctx.put("isLeader", "true");
		}
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Show Post";
	}
}
