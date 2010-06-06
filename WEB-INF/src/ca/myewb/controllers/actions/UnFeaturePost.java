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

package ca.myewb.controllers.actions;

import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.PostModel;


public class UnFeaturePost extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		PostModel p = (PostModel)getAndCheckFromUrl(PostModel.class);

		if(p.getParent() != null)
		{
			p = p.getParent();
		}
		
		if (!currentUser.isAdmin())
		{
			throw getSecurityException("You cannot remove this from the hot-list.",
			                           path + "/home/Posts");
		}

		p.unfeature();
		
		// Redirect to somewhere
		throw new RedirectionException(path + "/ajax/keepalive");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Admin");

		return s;
	}
}
