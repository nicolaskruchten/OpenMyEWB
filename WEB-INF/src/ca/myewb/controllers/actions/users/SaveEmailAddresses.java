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
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.UserEmailForm;
import ca.myewb.model.UserModel;


public class SaveEmailAddresses extends Controller
{
	public void handle(Context ctx) throws Exception
	{

		UserModel targetUser = (UserModel) getAndCheckFromUrl(UserModel.class);

		UserEmailForm form = new UserEmailForm(path + "/actions/SaveEmailAddresses/" + targetUser.getId(),
			                           requestParams);

		Message m = form.validate();
		
		String primaryEmail = form.getParameter("Email");
		UserModel userForEmail = UserModel.getUserForEmail(primaryEmail);
		if((m==null) && (userForEmail != null) && (!userForEmail.equals(targetUser)))
		{
			if( userForEmail.getUsername().equals("") )
			{
				targetUser.mergeRolesWithMailAccount(userForEmail);
			}
			else
			{
				m = new ErrorMessage("Primary email address is already linked to user " + userForEmail.getUsername());
			
				form.setError("Email", "Please use a different email");
			}
			
		}

		// No messages: changes are valid
		if (m != null)
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m, path + "/chapter/MemberInfo/" + targetUser.getId());
		}
		
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
				if ((userForEmail != null) && (!userForEmail.equals(targetUser))) 
				{
					if( userForEmail.getUsername().equals("") )
					{
						targetUser.mergeRolesWithMailAccount(userForEmail);
						emails.add(email);
					}
					else
					{
						errors.add(email + " is already linked to user " + userForEmail.getUsername());
					}
				}
				else
				{
					emails.add(email);
				}
			}
		}
		
		
		emails.add(primaryEmail);
		
		targetUser.saveEmails(primaryEmail, emails);

		setSessionMessage((errors.isEmpty() ? "Basic info updated!"  : getExceptionString(errors)));


		throw new RedirectionException(path + "/chapter/MemberInfo/" + targetUser.getId());
	}

	private String getExceptionString(Vector<String> errors)
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Basic info updated with exceptions:<br/>");
		
		for (String error : errors)
		{
			buf.append(error + "<br/>");
		}
				
		return buf.toString();
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	public String oldName()
	{
		return "SaveEmailAddresses";
	}
}
