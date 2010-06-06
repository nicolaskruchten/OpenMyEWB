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

package ca.myewb.controllers.volunteering;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.controllers.common.Member;
import ca.myewb.frame.Controller;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.OVInfoModel;
import ca.myewb.model.UserModel;


public class OVInfo extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		urlParams.processParams(new String[]{"id"}, new String[]{new Integer(currentUser.getId()).toString()});
		
		UserModel theUser = (UserModel) getAndCheckFromUrl(UserModel.class, "id");
		
		if(!theUser.equals(currentUser) && !currentUser.isAdmin())
		{
			throw getSecurityException("you can't view this information!", path + "/volunteering/PlacementList");
		}
		
		if(!theUser.isMember("OVs", false))
		{
			throw getSecurityException("that user is not an OVS!",  path + "/volunteering/PlacementList");
		}
		
		ca.myewb.model.OVInfoModel theInfo = theUser.getOVInfo();
		
		if(theInfo == null)
		{
			theInfo = OVInfoModel.newOVInfo();
		}
		ctx.put("ov", theUser);
		ctx.put("info", theInfo);

		Member.viewMember(ctx, theUser, false, false);

		Vector<Toolbar> toolbars = new Vector<Toolbar>();
		toolbars.add(new ca.myewb.frame.toolbars.OVInfo());
		ctx.put("toolbars", toolbars);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();

		s.add("OVs");

		return s;
	}

	public String displayName()
	{
		return "OV Info";
	}
	
	public int weight()
	{
		return 100;
	}
	

	public boolean secureAccessRequired()
	{
		return true;
	}
}
