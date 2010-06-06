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
import java.util.List;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Message;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.SignInForm;
import ca.myewb.model.UserModel;


public class DoSignIn extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		if (!currentUser.getUsername().equals("guest"))
		{
			//generically a good idea
			//plus, helps with cookie-checking if we re-redirect here after 'successful signin'
			//only makes sense if we didn't enter the following 'if' the last time we hit this page
			throw new RedirectionException(path + "/home/Posts");
		}

		if (httpSession.isNew()
		    && ((urlParams.getParam() == null)
		       || !urlParams.getParam().equals("direct")))
		{
			throw new RedirectionException(path + "/home/SignIn/expired");
		}

	
		SignInForm loginForm = new SignInForm(path + "/actions/DoSignIn",
		                                      requestParams, "sign in");
		Message m = loginForm.validate();

		if (((loginForm.getParameter("Username") == null)
		    || (loginForm.getParameter("Username").equals(""))))
		{
			// Display error and prompt user to fix
			m = new ErrorMessage("Please enter an email address and password.");
		}

		if (m != null)
		{
			// Display error and prompt user to fix
			throw getValidationException(loginForm, m, path + "/home/SignIn");
		}


		// Find requested user
		List result = hibernateSession.createQuery("FROM UserModel u WHERE u.username=?")
		              .setString(0, loginForm.getParameter("Username")).list();

		UserModel user;
		
		if (result.isEmpty())
		{
			// Username does not exist
			user = UserModel.getUserForEmail(loginForm.getParameter("Username"));
			if(user == null)
			{
				setSessionErrorMessage(("Unknown username or email address"));
				log.debug("Signing failed: user doesn't exist");
				throw new RedirectionException(path + "/home/SignIn");
			}
		}
		else
		{
			user = (UserModel)result.get(0);
			// Has the user been deleted?
			if (user.isMember(Helpers.getGroup("Deleted"), false))
			{
				setSessionErrorMessage(("This user has been deleted"));

				log.debug("Deleted user cannot log in");
				throw new RedirectionException(path + "/home/SignIn");
			}
		}
		

		// Check password
		if (!user.checkPassword(loginForm.getParameter("Password")))
		{
			setSessionErrorMessage(("Incorrect password"));
			log.debug("Signing in failed: wrong password");
			throw new RedirectionException(path + "/home/SignIn");
		}

		// Successful login!
		httpSession.setAttribute("hideStickies", null);
		httpSession.setAttribute("userid", new Integer(user.getId()));

		log.debug("Setting user to " + user.getUsername());

		user.signIn();

		// Smart redirect
		if ((urlParams.getParam() != null)
		    && urlParams.getParam().equals("direct"))
		{
			//if this is a direct signing another site we need to check for cookies
			//so we revisit this page, and we'll get kicked out if the session is new
			setInterpageVar("newLogin", new Boolean(true));
			throw new RedirectionException(path + "/actions/DoSignIn");
		}
		
		String requestedURL = loginForm.getParameter("targetURL");

		setSessionMessage("Welcome back, " + user.getFirstname() + "!<br />" +
				"<a href=\"#\" onclick=\"pushBackNewPosts('" + path + "/actions/PushBackNewPosts', this)\" " +
				"style=\"font-size: 10px;\">click here to preserve new posts until you next sign in</a>");
		setInterpageVar("newLogin", new Boolean(true));

		String newPath;
		if ((requestedURL == null) || (requestedURL.equals("")))
			newPath = "/home/Posts";
		else
			newPath = requestedURL.substring(path.length());
		
		throw new RedirectionException(path + newPath);

	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
	
	public String oldName()
	{
		return "DoSignIn";
	}
}
