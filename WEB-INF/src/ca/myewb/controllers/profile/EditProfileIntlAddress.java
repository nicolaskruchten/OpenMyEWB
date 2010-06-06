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
import ca.myewb.frame.forms.UserProfileIntlAddressForm;


public class EditProfileIntlAddress extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		//PARAM regular or not

		// Load up the form, so that we can edit ourselves
		UserProfileIntlAddressForm f = (UserProfileIntlAddressForm)checkForValidationFailure(ctx);

		if (f == null)
		{
			// Get member status (regular vs associate)
			// is it safe to assume any non-regular member is associate?  because i do =)
			boolean isRegular = currentUser.isMember(Helpers.getGroup("Regular"));

			if ((urlParams.getParam() != null)
			    && urlParams.getParam().equals("regular"))
			{
				isRegular = true;
				f = new UserProfileIntlAddressForm(path
				                                   + "/actions/SaveUserIntlAddress/regular",
				                                   requestParams, true);
			}
			else
			{
				f = new UserProfileIntlAddressForm(path
				                                   + "/actions/SaveUserIntlAddress",
				                                   requestParams, isRegular);
			}

			f.setValue("Address", currentUser.getAddress());
			f.setValue("Phone", currentUser.getPhone());
			f.setValue("Cell", currentUser.getCellno());
			f.setValue("Alt", currentUser.getAlternateno());
			f.setValue("Business", currentUser.getBusinessno());
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
