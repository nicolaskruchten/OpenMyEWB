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

package ca.myewb.frame.toolbars;

import ca.myewb.controllers.common.PostList;
import ca.myewb.controllers.common.WhiteboardList;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.model.UserModel;

public class AtAGlance extends Toolbar
{
	int newPosts;
	int newReplies;
	int newEdits;
	
	public AtAGlance() throws Exception
	{
		super();
		this.title = "At a Glance";
		this.template = "frame/toolbars/ataglance.vm";
	}

	public void compute(UserModel user) {
		PostList p = new PostList(null, HibernateUtil.currentSession(), null, null, user);
		newPosts = p.visiblePostsCount(null, true, false, false, false, true, false);
		newReplies = p.visiblePostsCount(null, true, true, false, false, true, false) - newPosts;
		WhiteboardList whiteboardList = new WhiteboardList(null, HibernateUtil.currentSession(), null, null, user);
		newEdits = whiteboardList.visibleWhiteboardCount();
		
	}

	public int getNewPosts()
	{
		return newPosts;
	}

	public int getNewReplies()
	{
		return newReplies;
	}
	
	public int getNewEdits()
	{
		return newEdits;
	}
}
