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

package ca.myewb.controllers.actions.users;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;


public class SetDisplayMode extends Controller
{
	public void handle(Context ctx) throws Exception
	{

		urlParams.processParams(new String[]{"showEmails", "showReplies", "sortByLastReply", "area", "class", "filter"},
		                        new String[]{"yes", "yes", "no", "home", "Posts", "Any"});
		
		boolean showEmails = urlParams.get("showEmails").equals("yes");
		boolean showReplies = urlParams.get("showReplies").equals("yes");
		boolean sortByLastReply = urlParams.get("sortByLastReply").equals("yes");

		httpSession.setAttribute("showEmails", showEmails ? "yes" : "no");
		httpSession.setAttribute("showReplies", showReplies ? "yes" : "no");
		httpSession.setAttribute("sortByLastReply", sortByLastReply ? "yes" : "no");
		
		if(currentUser.isMember("Users"))
		{
			currentUser.setShowemails(showEmails);
			currentUser.setShowreplies(showReplies);
			currentUser.setSortByLastReply(sortByLastReply);
		}

		throw new RedirectionException(path + "/" 
				+ urlParams.get("area") + "/" + urlParams.get("class") + "/" + urlParams.get("filter"));
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
	
	public String oldName()
	{
		return "SetDisplayMode";
	}
}
