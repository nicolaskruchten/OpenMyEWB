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

import java.io.File;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.EmailModel;
import ca.myewb.model.UserModel;


public class DoDeleteUser extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		String confirmURL = path + "/actions/DoDeleteUser";

		if (urlParams.getParam() != null)
		{
			confirmURL += ("/" + urlParams.getParam());
		}

		requireConfirmation("Are you sure you want to delete this account? This action cannot be undone",
		                    "While all posts will remain, all profile information and mailing list memberships will be lost!",
		                    path + "/profile/Profile", confirmURL, "profile",
		                    null);

		UserModel theUser = null;

		VelocityContext velocityContext = new VelocityContext();
		
		if (urlParams.getParam() == null) //user is deleting him/herself
		{
			theUser = currentUser;
			log.info("user is deleting self: " + theUser.getUsername());
			velocityContext.put("self", "yes");
		}
		else //deletion of another user has been requested
		{
			velocityContext.put("self", "no");
			if (currentUser.isAdmin())
			{
				theUser = (UserModel)getAndCheckFromUrl(UserModel.class);

				if (theUser.getUsername().equals("guest"))
				{
					throw getSecurityException("I don't know WHAT you think you're doing, but you can't delete the guest user!",
					                           path + "/profile/Profile");
				}

				log.info("admin is deleting user: " + theUser.getUsername());
			}
			else
			{
				throw getSecurityException("You can't delete members!",
				                           path + "/home/Home");
			}
		}

		if (theUser.isLastExec())
		{
			throw getSecurityException("Cannot delete the last exec of a chapter",
			                           path + "/profile/Profile");
		}

		if (theUser.isLastAdmin())
		{
			throw getSecurityException("You're the last admin you fool! You can't delete yourself!",
			                           path + "/profile/Profile");
		}

		// And send the email
		velocityContext.put("user", theUser);
		velocityContext.put("helpers", new Helpers());
		Template template = Velocity.getTemplate("emails/deletion.vm");
		StringWriter writer = new StringWriter();
		template.merge(velocityContext, writer);

		EmailModel.sendEmail(theUser.getEmail(), writer.toString());

		// Nuke the user picture
		log.debug("Deleting " + Helpers.getUserFilesDir() + "/userpics/thumbs/"
		          + Integer.toString(theUser.getId()) + ".jpg");

		File file = new File(Helpers.getUserFilesDir() + "/userpics/thumbs/"
		                     + Integer.toString(theUser.getId()) + ".jpg");

		if (file.exists())
		{
			file.delete();
		}

		file = new File(Helpers.getUserFilesDir() + "/userpics/fullsize/"
		                + Integer.toString(theUser.getId()) + ".jpg");

		if (file.exists())
		{
			file.delete();
		}

		// If they're signed in, remove them from the online users list
		Hashtable users = (Hashtable)httpSession.getServletContext()
		                  .getAttribute("userList");
		users.remove(new Integer(theUser.getId()));

		// And now really do it		
		theUser.delete();
		
		log.info("Deleted user " + theUser.getUsername());

		if (urlParams.getParam() == null)
		{
			// And you are now a guest
			httpSession.setAttribute("userid", new Integer(1));

			setSessionMessage("Your account has been deleted...");
			throw new RedirectionException(path + "/home/Home");
		}
		else
		{
			setSessionMessage("Account has been deleted...");
			throw new RedirectionException(path + "/chapter/MemberInfo");
		}
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}
	
	public String oldName()
	{
		return "DoDeleteUser";
	}
}
