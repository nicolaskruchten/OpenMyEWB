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

package ca.myewb.controllers.home;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.SignInForm;


public class SignIn extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		if (!currentUser.getUsername().equals("guest"))
		{
			//needed because this page is Org-visible
			throw new RedirectionException(path + "/home/Posts");
		}

		SignInForm loginForm = (SignInForm)checkForValidationFailure(ctx);

		httpSession.removeAttribute("message"); 

		if (loginForm == null)
		{
		
			// First try: create a fresh form
			loginForm = new SignInForm(path + "/actions/DoSignIn",
			                           requestParams, "sign in");
			String requestedURL = (String)getInterpageVar("requestedURL");
			if(requestedURL != null)
			{
				loginForm.setValue("targetURL", requestedURL);
			}
		}

		ctx.put("targetURL", getInterpageVar("requestedURL"));

		ctx.put("form", loginForm);

		if ((urlParams.getParam() != null)
		    && (urlParams.getParam().equals("expired")))
		{
			setSessionErrorMessage(("Session expired after 1hr of inactivity. Please sign in again to proceed.<br />Note: If you keep seeing this message, your browser likely does not accept cookies (see below).")); //session is likely new, no previous messages can be in session
		}
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "Sign In";
	}

	public List<String> getNeededInterpageVars()
	{
		Vector<String> vars = new Vector<String>();
		vars.add("requestedURL");

		return vars;
	}
}
