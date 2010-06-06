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

package ca.myewb.controllers.profile;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.SignUpForm;
import ca.myewb.model.GroupChapterModel;


public class SignUp extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		if (!currentUser.getUsername().equals("guest"))
		{
			throw new RedirectionException(path + "/profile/ShowInfo");
		}


		SignUpForm register = (SignUpForm)checkForValidationFailure(ctx);

		if (register == null)
		{
			// Load up a fresh form object
			register = new SignUpForm(path + "/actions/DoSignUp", requestParams);
			if ((urlParams.getParam() != null) && !urlParams.getParam().equals("verbose"))
			{
				GroupChapterModel chapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class);
				register.setValue("Chapter", Integer.toString(chapter.getId()));
			}
		}

		ctx.put("form", register);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Guest");

		return s;
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}

	public String displayName()
	{
		return "Sign Up";
	}

	public int weight()
	{
		return 10;
	}
}
