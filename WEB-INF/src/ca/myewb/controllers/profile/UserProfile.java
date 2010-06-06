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

import ca.myewb.controllers.common.Member;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.UserModel;


public class UserProfile extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		UserModel targetUser = (UserModel)getAndCheckFromUrl(UserModel.class);
		if(targetUser.getId() == currentUser.getId())
		{
			throw new RedirectionException(path + "/profile/ShowInfo");
		}
		
		if(Permissions.canReadPersonalDetails(currentUser, targetUser))
		{
			throw new RedirectionException(path + "/chapter/MemberInfo/" + targetUser.getId());
		}
		
		Member.viewMember(ctx, targetUser, true, true);
	}


	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		return s;
	}

	public String displayName()
	{
		return "User Profile";
	}

	public int weight()
	{
		return -100;
	}
}
