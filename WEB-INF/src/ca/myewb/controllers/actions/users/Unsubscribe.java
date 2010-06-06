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

import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ForgotPasswordForm;
import ca.myewb.model.EmailModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class Unsubscribe extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ForgotPasswordForm form = new ForgotPasswordForm(path + "/actions/Unsubscribe", requestParams, "Unsubscribe");

		Message m = form.validate();

		if (!((m == null) || (isOnConfirmLeg())))
		{
			throw getValidationException(form, m, path + "/mailing/Unsubscription");
		}

		GroupModel usersGroup = Helpers.getGroup("Users");
		GroupModel orgGroup = Helpers.getGroup("Org");
		GroupModel guestGroup = Helpers.getGroup("Guest");
		
		if(!isOnConfirmLeg())
		{
			String email = form.getParameter("Email");
			UserModel targetUser = UserModel.getUserForEmail(email);
			if(targetUser == null)
			{		
				throw getSecurityException("That email is not in our database... Please try entering any other email addresses you may have.", path + "/mailing/Unsubscription");
			}
			else if (targetUser.isMember(usersGroup))
			{
				setSessionMessage(("That email address is associated with a full user account," +
						" so you must sign in below to unsubscribe from any mailing lists or to delete the account to stop receiving emails altogether. " +
						"<br />Once signed in, use the Mailing Lists tab to manage your subscriptions, or use the Profile tab " +
						"to delete your account if that is what you wish to do."));
				log.warn("Someone tried to delete " + targetUser.getUsername() + " from outside.");
				throw new RedirectionException(path + "/home/SignIn");
			}
		}
		
		requireConfirmation("Confirm: completely remove " + form.getParameter("Email") + " from all mailing lists and databases?",
                "If you are currently on multiple mailing lists, and would simply like to receive " +
                "fewer emails, you can hit 'No, cancel' above to choose a password (using the email address '" + 
                form.getParameter("Email") + "'). Once signed in, you can manage your mailing list subscriptions under the Mailing Lists tab.",
                path + "/profile/SignUp",
                path + "/actions/Unsubscribe",
                "mailing", null);
		form = new ForgotPasswordForm(path + "/actions/Unsubscribe", requestParams, "Unsubscribe");
		
		
		String email = form.getParameter("Email");
		UserModel targetUser = UserModel.getUserForEmail(email);
		if(targetUser.isMember(orgGroup) && !targetUser.isMember(guestGroup))
		{
			VelocityContext velocityContext = new VelocityContext();
			velocityContext.put("self", "yes");
			velocityContext.put("user", targetUser);
			velocityContext.put("helpers", new Helpers());
			Template template = Velocity.getTemplate("emails/deletion.vm");
			StringWriter writer = new StringWriter();
			template.merge(velocityContext, writer);
			EmailModel.sendEmail(targetUser.getEmail(), writer.toString());
			

			log.warn(targetUser.getEmail() + " deleted from outside.");
			targetUser.delete();
			setSessionMessage(("Done."));
			throw new RedirectionException(path + "/mailing/Unsubscription");
		}
			
		throw getSecurityException("Can't perform this operation.", path + "/mailing/Unsubscription");
		
	}


	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Guest");

		return s;
	}
	
	public String oldName()
	{
		return "Unsubscribe";
	}
}
