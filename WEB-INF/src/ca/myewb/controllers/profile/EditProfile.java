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

package ca.myewb.controllers.profile;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.forms.UserProfileForm;


public class EditProfile extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		//PARAM regular or not
		// Load up the form, so that we can edit ourselves
		UserProfileForm f = (UserProfileForm)checkForValidationFailure(ctx);

		if (f == null)
		{
			// Get member status (regular vs associate)
			// is it safe to assume any non-regular member is associate?  because i do =)
			boolean isRegular = currentUser.isMember(Helpers.getGroup("Regular"));

			if ((urlParams.getParam() != null)
			    && urlParams.getParam().equals("regular"))
			{
				isRegular = true;
				f = new UserProfileForm(path + "/actions/SaveUser/regular",
				                        requestParams, true);
			}
			else
			{
				f = new UserProfileForm(path + "/actions/SaveUser",
				                        requestParams, isRegular);
			}

			f.setValue("Firstname", currentUser.getFirstname());
			f.setValue("Lastname", currentUser.getLastname());
			f.setValue("Email", currentUser.getEmail());
			f.setValue("Emails", currentUser.getFormattedEmailList());
			f.setValue("Language", currentUser.getLanguage());
			f.setValue("Gender", String.valueOf(currentUser.getGender()));

			if (currentUser.getBirth() != 0)
			{
				f.setValue("BirthYear", String.valueOf(currentUser.getBirth()));
			}
			else
			{
				f.setValue("BirthYear", "");
			}

			f.setValue("Student", String.valueOf(currentUser.getStudent()));
			f.setValue("Canadianinfo",
			           String.valueOf(currentUser.getCanadianinfo()));
		}

		ctx.put("form", f);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}

	public String displayName()
	{
		return "Update Info";
	}
}
