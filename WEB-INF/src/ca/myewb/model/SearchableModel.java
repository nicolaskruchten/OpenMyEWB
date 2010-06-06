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

package ca.myewb.model;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Helpers;
import ca.myewb.logic.SearchableLogic;



public class SearchableModel extends SearchableLogic
{

	public SearchableModel() {
		super();
	}
	
	public static SearchableModel newSearchable(PostModel p, EventModel e, WhiteboardModel w)
	{
		SearchableModel s = new SearchableModel();
		
		s.setParentPost(p);
		s.setParentEvent(e);
		s.setParentWhiteboard(w);
		
		if(p != null)
		{
			s.setGroup(p.getGroup());
		}
		else if(e != null)
		{
			s.setGroup(e.getGroup());
		}
		else if(w != null)
		{
			s.setGroup(w.getGroup());
		}
		s.update();
		
		HibernateUtil.currentSession().save(s);
		
		return s;
		
	}
	
	public void update()
	{
		if(parentPost != null)
		{
			UserModel poster = parentPost.getPoster();
			setBody(parentPost.getSubject() + "\n" + parentPost.getIntro() + "\n" + parentPost.getBody()
					+ "\n" + poster.getFirstname() + " " + poster.getLastname());
			setDate(parentPost.getDate());
		}
		else if(parentEvent != null)
		{
			setBody(parentEvent.getName() + "\n" + parentEvent.getLocation() + "\n" + parentEvent.getNotes());
			setDate(parentEvent.getStartDate());
		}
		else if(parentWhiteboard != null)
		{
			if(parentWhiteboard.isEnabled())
				setBody(parentWhiteboard.getBody());
			else
				setBody("");
			setDate(parentWhiteboard.getLastEditDate());
		}
	}
	
	public void delete()
	{
		setGroup(Helpers.getGroup("DeletedPosts"));
	}	
}
