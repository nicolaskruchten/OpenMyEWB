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

package ca.myewb.controllers.actions.ovapps;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.PlacementModel;


public class PlacementLifecycle extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		urlParams.processParams(new String[]{"action", "placementid"},
		                        new String[]{null, "-1"});

		PlacementModel p = (PlacementModel)getAndCheckFromUrl(PlacementModel.class, "placementid");
		String returnPath = path + "/volunteering/PlacementInfo/" + p.getId();
		
		
		if((urlParams.get("action") == null) || (!urlParams.get("action").equals("deactivate") && !urlParams.get("action").equals("delete")))
		{
			setSessionErrorMessage(("Invalid action."));
			throw new RedirectionException(returnPath);
		}
		
		requireConfirmation("Are you sure you want to " + urlParams.get("action") + " this placement?",
                "This action can only be undone by a direct database operation.",
                returnPath, 
                path + "/actions/PlacementLifecycle/" + urlParams.get("action") + "/" + urlParams.get("placementid"), 
                "volunteering",
                null);
		
		if(urlParams.get("action").equals("deactivate"))
		{
			p.deactivate();
			
			setSessionMessage(("Placement deactivated."));
		}
		else if(urlParams.get("action").equals("delete"))
		{
			p.delete();
			
			setSessionMessage(("Placement deleted."));
		}

		throw new RedirectionException(returnPath);
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
	
	public String oldName()
	{
		return "PlacementLifecycle";
	}
}
