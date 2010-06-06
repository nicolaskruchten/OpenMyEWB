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

import ca.myewb.frame.Controller;
import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ForgotPasswordForm;
import ca.myewb.frame.forms.SignUpForm;
import ca.myewb.model.EmailModel;
import ca.myewb.model.UserModel;


public class MakeNewPassword extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		// Create and validate form object
		ForgotPasswordForm form = new ForgotPasswordForm(path
		                                                 + "/actions/MakeNewPassword",
		                                                 requestParams, "Unsubscribe");
		Message m = form.validate();

		if (m != null)
		{
			// Display error and prompt user to fix
			throw getValidationException(form, m,
			                             path + "/profile/ForgotPassword");
		}

		String email = form.getParameter("Email");
		// Retrieve the user account in question
		UserModel u = UserModel.getUserForEmail(email);

		if (u != null)
		{
			if (u.getUsername().equals(""))
			{
				Message msg = new ErrorMessage("Your email address is on a mailing list, but you do not yet have a full account, so you have no password. You can choose one now!");
				httpSession.setAttribute("message", msg);

				SignUpForm f = new SignUpForm(path + "/actions/DoSignUp",
				                              requestParams);
				f.getElement("Email").setValue(email);
				httpSession.setAttribute("form", f);

				throw new RedirectionException(path + "/profile/SignUp");
			}

			String newPass = UserModel.generateRandomPassword();
			u.setPassword(newPass);
			u.changePrimaryEmail(email);

			// And send the email
			VelocityContext mailCtx = new VelocityContext();

			if ((u.getFirstname() == null) || (u.getFirstname().equals(""))
			    || (u.getLastname() == null) || (u.getLastname().equals("")))
			{
				mailCtx.put("name", Helpers.getLongName() + " Member");
			}
			else
			{
				mailCtx.put("name", u.getFirstname() + " " + u.getLastname());
			}

			mailCtx.put("username", u.getUsername());
			mailCtx.put("password", newPass);
			mailCtx.put("helpers", new Helpers());

			Template template = Velocity.getTemplate("emails/newpassword.vm");
			StringWriter writer = new StringWriter();
			template.merge(mailCtx, writer);

			EmailModel.sendEmail(u.getEmail(), writer.toString());

			log.debug("Password reset for " + u.getUsername());
			setSessionMessage(("A new password was emailed to "
			                                     + email
			                                     + "."));
		}
		else
		{
			log.info("Lost password lookup failed for "
			         + email);
			setSessionErrorMessage(("The address "
			                                          + email
			                                          + " isn't in our database..."));
		}

		throw new RedirectionException(path + "/home/Home");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
	
	public String oldName()
	{
		return "MakeNewPassword";
	}
}
