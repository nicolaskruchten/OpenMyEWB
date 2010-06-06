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
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.element.Text;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class DoSilentMainListSignup extends Controller
{
	public void handle(Context ctx) throws Exception
	{	
		urlParams.processParams(new String[]{"email", "listid"}, new String[]{"notvalid", "1"});
		
		String email = urlParams.get("email").trim().toLowerCase();
		
		if (!email.equals("") && (new Text("validator", "validator", email, false)).ensureEmail())
		{
			UserModel targetUser = UserModel.getUserForEmail(email);
			if(targetUser == null)
			{
				targetUser = UserModel.newMailingListSignUp( email);
			}

			if(!urlParams.get("listid").equals("1"))
			{
				GroupModel theGroup = null;
				try
				{			
					int id = Integer.parseInt(urlParams.get("listid"));
					theGroup = (GroupModel)hibernateSession.get(GroupModel.class, id);
				}
				catch(NumberFormatException nfe)
				{
					throw new RedirectionException(path + "/ajax/keepalive");
				}
				
				if((theGroup != null) && Permissions.guestsCanReadPostsInGroup( theGroup))
				{
					if(!targetUser.isAdmin() && (targetUser.getChapter() == null))
					{
						if (theGroup.isChapter())
						{
							//yup, this IS a chapter (they don't get re-added as recipients)
							targetUser.joinChapter((GroupChapterModel)theGroup);
						}
						else if (theGroup.getParent() != null)
						{
							//this is a chapter sub-list and the user has no chapter, so join it
							targetUser.joinChapter(theGroup.getParent());
						}
					}
					
					targetUser.subscribe(theGroup);
				}
			}
		}
		throw new RedirectionException(path + "/ajax/keepalive");
	}


	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
	
	public String oldName()
	{
		return "DoSilentMainListSignup";
	}
}
