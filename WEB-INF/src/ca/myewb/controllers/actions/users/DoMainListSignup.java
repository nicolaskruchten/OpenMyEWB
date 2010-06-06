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
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.MainListSignupForm;
import ca.myewb.model.EmailModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class DoMainListSignup extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		MainListSignupForm form = new MainListSignupForm(path + "/actions/DoMainListSignup", requestParams);

		Message m = form.validate();

		if (m != null)
		{
			throw getValidationException(form, m, path + "/mailing/MainListSignup");
		}

		GroupModel orgGroup = Helpers.getGroup("Org");

		String[] emails = form.getParameter("Emails").split("\n");
		
		List<String> errors = new Vector<String>();
		
		for(String email: emails)
		{
			email = email.trim();

			if (!email.equals(""))
			{
				UserModel targetUser = UserModel.getUserForEmail(email);
				if(targetUser == null)
				{
					UserModel.newMailingListSignUp(email);
				}
				if(!form.getParameter("EmailType").equals("none"))
				{
					//send the welcome email
					Template template = null;
					if (form.getParameter("EmailType").equals("donor"))
					{
						template = Velocity.getTemplate("emails/donorjoinlist.vm");
					} else
					// use default
					{
						template = Velocity.getTemplate("emails/joinlist.vm");
					}
	
					VelocityContext mailctx = new VelocityContext();
					mailctx.put("helpers", new Helpers());
					mailctx.put("email", email);
					mailctx.put("totalshortname", orgGroup.getTotalShortname());
					
					if (currentUser.getUsername().equals("guest"))
					{
						mailctx.put("actor", "you");
					}
					else
					{
						mailctx.put("actor",
						        currentUser.getFirstname() + " "
						        + currentUser.getLastname());
					}
	
					StringWriter writer = new StringWriter();
					template.merge(mailctx, writer);
					EmailModel.sendEmail(email, writer.toString());
				}
			}
			else
			{
				errors.add( email + " is a reserved address. You cannot signup a new user with that address.");
			}
		}

		// Leave a message in the sessionif (errors.isEmpty())
		if (errors.isEmpty())
		{
			setSessionMessage(("Done"));
		}
		else
		{
			String message = "Done, with exceptions: " +
					"<div align=\"center\"><ul style=\"color: black; padding: 0; margin: 0; margin-top:8px;\">";

			for (String error : errors)
			{
				message += ("<li style=\"margin-bottom: 5px;\">" + error + "</li>");
			}

			message += "</ul></div>";
			httpSession.setAttribute("message", new Message(message));
		}
		
		throw new RedirectionException(path + "/mailing/ListInfo/1");
	}


	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "DoMainListSignup";
	}
}
