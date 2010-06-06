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

package ca.myewb.controllers.actions;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.PostModel;


public class DeletePost extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		PostModel thePost = (PostModel)getAndCheckFromUrl(PostModel.class);

		if (!Permissions.canDeletePost(currentUser, thePost))
		{
			throw getSecurityException("You cannot delete this post",
			                           path + "/ShowPost/"
			                           + urlParams.getParam());
		}

		requireConfirmation("Are you sure you want to delete this post?",
		                    "Only an administrator could undo this action. You should only delete posts which are inappropriate.",
		                    path + "/home/ShowPost/" + urlParams.getParam(),
		                    path + "/actions/DeletePost/"
		                    + urlParams.getParam(), "home", null);

		thePost.delete();
		
		log.info(currentUser.getUsername() + " deleted post #"
		         + thePost.getId());

		setSessionMessage(("Deleted"));
		throw new RedirectionException(Helpers.getDefaultURL());
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
}
