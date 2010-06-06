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
import ca.myewb.frame.forms.PayDuesForm;
import ca.myewb.model.GroupChapterModel;


public class RegularSignUp extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		if (!currentUser.getUsername().equals("guest"))
		{
			throw new RedirectionException(path + "/profile/PayDues");
		}
		
		PayDuesForm f = (PayDuesForm)checkForValidationFailure(ctx);
		
		if(f == null)
		{
			f = new PayDuesForm(path + "/actions/DoRegularSignup", requestParams, false, true);
			if ((urlParams.getParam() != null) && !urlParams.getParam().equals("verbose"))
			{
				GroupChapterModel chapter = (GroupChapterModel)getAndCheckFromUrl(GroupChapterModel.class);
				f.setValue("Chapter", Integer.toString(chapter.getId()));
			}
		}

		ctx.put("form", f);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}
	
	public boolean secureAccessRequired()
	{
		return true;
	}

	public String displayName()
	{
		return "Become a Regular Member";
	}

	public int weight()
	{
		return -90;
	}
}
