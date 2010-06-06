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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.forms.PostEditForm;
import ca.myewb.model.GroupModel;


public class NewPost extends Controller
{
	public void handle(Context ctx) throws Exception
	{

		urlParams.processParams(new String[]{"group"}, new String[]{"1"});
		
		GroupModel g = null;
		if(urlParams.get("group") != null && !urlParams.get("group").equals("0"))
		{
			g = (GroupModel)getAndCheckFromUrl(GroupModel.class, "group");
		}
		
		PostEditForm post = (PostEditForm)checkForValidationFailure(ctx);

		if (post == null)
		{
			// First try: create a fresh form
			List<GroupModel> postGroups = Permissions.postGroups(currentUser);
			LinkedHashSet<GroupModel> hs = new LinkedHashSet<GroupModel>();
			hs.addAll(postGroups);
			
			Vector<GroupModel> gList = new Vector<GroupModel>();
			for(GroupModel temp: hs)
			{
				gList.add(temp);
			}
			
			post = new PostEditForm(path + "/actions/SavePost", requestParams, g, gList, currentUser);
			post.setValue("ResponseType", "Replies");
		}

		ctx.put("form", post);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}

	public String displayName()
	{
		return "New Post";
	}

	public int weight()
	{
		return 10;
	}
}
