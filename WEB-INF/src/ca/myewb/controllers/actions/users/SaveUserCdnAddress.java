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
import ca.myewb.frame.forms.UserProfileCdnAddressForm;


public class SaveUserCdnAddress extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		//PARAM regular or null
		String redirect = null;

		// Create & validate form object
		log.debug("Building form");

		// Get member status (regular vs associate)
		boolean isRegular = currentUser.isMember(Helpers.getGroup("Regular"));

		UserProfileCdnAddressForm form;
		urlParams.processParams(new String[]{"isRegular"},
		                        new String[]{"associate"});

		if (urlParams.get("isRegular").equals("regular"))
		{
			isRegular = true;
			form = new UserProfileCdnAddressForm(path
			                                     + "/actions/SaveUserCdnAddress/regular",
			                                     requestParams, true);
		}
		else
		{
			form = new UserProfileCdnAddressForm(path
			                                     + "/actions/SaveUserCdnAddress",
			                                     requestParams, isRegular);
		}

		log.debug("Validating form");

		Message m = form.validate();
		
		// No messages: changes are valid
		if (m != null)
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path
			                             + "/profile/EditProfileCdnAddress");
		}

		String address = form.getParameter("Address");
		String phone = form.getParameter("Phone");
		String cell = form.getParameter("Cell");
		String alt = form.getParameter("Alt");
		String business = form.getParameter("Business");
		
		currentUser.saveAddress(address, phone, business, cell, alt);

		String extraMessage = "";
		if (currentUser.getStudent() != 0)
		{
			if (currentUser.getStudent() == 'y')
			{
				redirect = (path + "/profile/EditProfileStudent");
			}
			else
			{
				redirect = (path + "/profile/EditProfilePro");
			}
		}
		else
		{
			extraMessage = "<br/>Step 3 was skipped, as you didn't specify your student status.";
			redirect = (path + "/profile/Profile");
		}

		setSessionMessage(("Contact info updated!" + extraMessage));
		log.debug("User update successful!");

		throw new RedirectionException(redirect);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
	
	public String oldName()
	{
		return "SaveUserCdnAddress";
	}
}
