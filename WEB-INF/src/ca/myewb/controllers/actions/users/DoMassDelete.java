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

import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.MassDeleteForm;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class DoMassDelete extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		MassDeleteForm form = new MassDeleteForm(path + "/actions/DoMassDelete", requestParams);

		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			throw getValidationException(form, m, path + "/mailing/MassDelete");
		}


		requireConfirmation("Confirm: really REALLY delete all those addresses?",
		                    "Please do this only for addresses which are for-sure bouncing. " +
		                    "As a sidenote, this action will probably take a while if there are more than " +
		                    "a dozen or so addresses on the list.",
		                    path + "/mailing/MassDelete",
		                    path + "/actions/DoMassDelete",
		                    "mailing", form);
		form = new MassDeleteForm(path + "/actions/DoMassDelete", requestParams);
		
		int totalNum = 0;
		int deletedNum =0;

		GroupModel usersGroup = Helpers.getGroup("Users");
		GroupModel orgGroup = Helpers.getGroup("Org");
		GroupModel guestGroup = Helpers.getGroup("Guest");
		
		String[] emails = form.getParameter("Emails").split("\n");
		for(String email: emails)
		{
			email = email.trim();

			if (!email.equals(""))
			{
				totalNum++;
				UserModel targetUser = UserModel.getUserForEmail(email);
				if((targetUser != null)
						&& !targetUser.isMember(usersGroup)
						&& targetUser.isMember(orgGroup)
						&& !targetUser.isMember(guestGroup))
				{
					deletedNum++;
					targetUser.delete();
				}
			}
		}

		// Leave a message in the session
		setSessionMessage(("Done: " + deletedNum +"/"+totalNum +" were deleted."));
		throw new RedirectionException(path + "/mailing/MassDelete");
	}


	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "DoMassDelete";
	}
}
