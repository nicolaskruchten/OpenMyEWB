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

import ca.myewb.frame.Controller;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.PlacementModel;


public class PlacementList extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		ctx.put("unassigned", PlacementModel.getUnassignedPlacements());

		ctx.put("active", PlacementModel.getActivePlacements());

		ctx.put("inactive", PlacementModel.getInactivePlacements());
		
		ctx.put("deleted", PlacementModel.getDeletedPlacements());
		
		Vector<Toolbar> toolbars = new Vector<Toolbar>();
		toolbars.add(new ca.myewb.frame.toolbars.PlacementList());
		ctx.put("toolbars", toolbars);
	}

	public Set<String> defaultGroups()
	{
		Set<String> s = new HashSet<String>();

		s.add("Admin");

		return s;
	}

	public String displayName()
	{
		return "Placement List";
	}
	
	public int weight()
	{
		return 101;
	}
}
