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
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Message;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.SignUpForm;
import ca.myewb.model.EmailModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.UserModel;

public class DoSignUp extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		// Create and validate form object
		SignUpForm register = new SignUpForm(path + "/actions/DoSignUp",
				requestParams);
		Message m = register.validate();
		UserModel u = null;

		if (m == null)
		{
			// Email address
			
			u = UserModel.getUserForEmail(register.getParameter("Email"));
			if (u != null && !u.getUsername().equals(""))
			{
				m = new ErrorMessage(
						"The email address you entered is already associated with an account!");
				httpSession.setAttribute("message", m);
				throw new RedirectionException(path
                        + "/profile/ForgotPassword");
			}
		}

		if (m != null)
		{
			// Display error and prompt user to fix
			throw getValidationException(register, m, path + "/profile/SignUp");
		}

		// All is good: proceed with user creation
		String email = register.getParameter("Email");
		String firstname = register.getParameter("Firstname");
		String lastname = register.getParameter("Lastname");
		String password = register.getParameter("Password");
		String chapterSelect = register.getParameter("Chapter");

		u = UserModel.newAssociateSignUp(u, email, firstname,
				lastname, password);
		
		if(chapterSelect != null && !chapterSelect.equals(""))
		{
			int chapID = Integer.parseInt(chapterSelect);
			GroupChapterModel chapter = (GroupChapterModel)HibernateUtil.currentSession().load(GroupChapterModel.class, chapID);
			
			if(u.getChapter() != chapter)
			{
				u.leaveChapter(u.getChapter());
				
				if(u.joinChapter(chapter) && chapter.getWelcomeMessage() != null)
				{
					EmailModel.sendEmail(chapter.getEmail(), u.getEmail(), chapter.getFullWelcomeEmail());
				}
			}
		}

		// Send them an email
		VelocityContext mailCtx = new VelocityContext();
		mailCtx.put("password", password);
		mailCtx.put("name", u.getFirstname() + " " + u.getLastname());
		mailCtx.put("helpers", new Helpers());

		Template template = Velocity.getTemplate("emails/signup.vm");
		StringWriter writer = new StringWriter();
		template.merge(mailCtx, writer);

		EmailModel.sendEmail(u.getEmail(), writer.toString());
		log.debug("user object saved to db");

		// Log the person in
		httpSession.setAttribute("userid", new Integer(u.getId()));


		// Leave a message in the session
		setSessionMessage("Welcome to " + Helpers.getLongName() + ", " + u.getFirstname() + " " + u.getLastname() + "!" +
				"<br /> An email containing your password has been sent to you " +
				"and we now invite you to tell us a bit more about yourself (this is entirely optional)");

		httpSession.setAttribute("hideStickies", null);
		throw new RedirectionException(path + "/profile/EditProfile");
	}


	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
	
	public String oldName()
	{
		return "DoSignUp";
	}
}
