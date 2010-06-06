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
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.UserProfileForm;
import ca.myewb.model.UserModel;


public class SaveUser extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		//PARAM regular or null
		String redirect = null;

		// Create & validate form object
		log.debug("Building form");

		// Get member status (regular vs associate)
		boolean isRegular = currentUser.isMember(Helpers.getGroup("Regular"));

		UserProfileForm form;
		urlParams.processParams(new String[]{"isRegular"},
		                        new String[]{"associate"});

		if (urlParams.get("isRegular").equals("regular"))
		{
			isRegular = true;
			form = new UserProfileForm(path + "/actions/SaveUser/regular",
			                           requestParams, true);
		}
		else
		{
			form = new UserProfileForm(path + "/actions/SaveUser",
			                           requestParams, isRegular);
		}

		log.debug("Validating form");

		Message m = form.validate();
		
		String primaryEmail = form.getParameter("Email");
		UserModel userForEmail = UserModel.getUserForEmail(primaryEmail);
		if((m==null) && (userForEmail != null) && (!userForEmail.equals(currentUser)))
		{
			if( userForEmail.getUsername().equals("") )
			{
				currentUser.mergeRolesWithMailAccount(userForEmail);
			}
			else
			{
				m = new ErrorMessage("That primary email address is already linked to another user account.  " +
					"The system administrator has been notified and will contact you to resolve the situation.");
			
				form.setError("Email", "Please use a different email");
				
				log.warn(currentUser.getUsername() + " (" + currentUser.getEmail() + ") failed email change: " 
						+ primaryEmail + " is already in use by " + userForEmail.getUsername());
			}
		}

		// No messages: changes are valid
		if (m != null)
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m, path + "/profile/EditProfile");
		}
		
		String firstname = form.getParameter("Firstname");
		String lastname = form.getParameter("Lastname");
		String password = form.getParameter("Password");
		String language = form.getParameter("Language");
		String gender = form.getParameter("Gender");
		String student = form.getParameter("Student");
		String birthYear = form.getParameter("BirthYear");
		String canadianInfo = form.getParameter("Canadianinfo");
		String emailText = form.getParameter("Emails");
		
		String[] splitEmails = emailText.split("\n");
		HashSet<String> emails = new HashSet<String>();
		Vector<String> errors = new Vector<String>();
		for(int i = 0; i<splitEmails.length; i++)
		{
			String email = splitEmails[i].trim();
			if(!email.equals(""))
			{
				userForEmail = UserModel.getUserForEmail(email);
				
				if ((userForEmail != null) && !userForEmail.equals(currentUser)) 
				{
					if( userForEmail.getUsername().equals("") )
					{
						currentUser.mergeRolesWithMailAccount(userForEmail);
						emails.add(email);
					}
					else
					{
						errors.add(email + " is already linked to another user account");
						log.warn(currentUser.getUsername() + " (" + currentUser.getEmail() + ") failed email change: " 
								+ email + " is already in use by " + userForEmail.getUsername());
					}
				}
				else
				{
					emails.add(email);
				}
			}
		}
		
		
		emails.add(primaryEmail);
		
		currentUser.saveUser(firstname, lastname, primaryEmail, emails, password, language, gender, student, birthYear, canadianInfo);
		
		String extraMessage = "";
			
		if (!canadianInfo.equals(""))
		{

			if (canadianInfo.charAt(0) == 'y')
			{
				redirect = (path + "/profile/EditProfileCdnAddress/"
				           + urlParams.get("isRegular"));
			}
			else
			{
				redirect = (path + "/profile/EditProfileIntlAddress/"
				           + urlParams.get("isRegular"));
			}
		}
		else if (currentUser.getStudent() != 0)
		{
			extraMessage = "<br/>Step 2 was skipped, as you didn't specify a place of residence.";
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
			redirect = (path + "/profile/Profile");
			extraMessage = "<br/>Step 2 was skipped, as you didn't specify a place of residence.";
			extraMessage += "<br/>Step 3 was skipped, as you didn't specify your student status.";
		}

		setSessionMessage((errors.isEmpty() ? 
					"Basic info updated!"  + extraMessage: getExceptionString(errors) + extraMessage));
		log.debug("User update successful!");

		throw new RedirectionException(redirect);
	}

	private String getExceptionString(Vector<String> errors)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Basic info updated with exceptions:<br/>");
		
		for (String error : errors)
		{
			buf.append(error + "<br/>");
		}
		
		buf.append("The system administrator has been notified and will contact you shortly to resolve the situation.<br/>");
		
		return buf.toString();
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
	
	public String oldName()
	{
		return "SaveUser";
	}
}
