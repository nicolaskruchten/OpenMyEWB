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

import java.util.Date;

import org.apache.log4j.Logger;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Helpers;
import ca.myewb.logic.WhiteboardLogic;


public class WhiteboardModel extends WhiteboardLogic
{

	WhiteboardModel() {
		super();
	}
	
	public static WhiteboardModel newWhiteboard( EventModel parentEvent, PostModel parentPost, GroupModel parentGroup )
	{
		WhiteboardModel w = new WhiteboardModel();
				
		w.setBody("");
		w.setParentEvent(parentEvent);
		w.setParentPost(parentPost);
		w.setParentGroup(parentGroup);
		
		if( parentEvent != null )
		{
			w.setGroup(parentEvent.getGroup());
			Logger.getLogger(WhiteboardModel.class).info("Group set as " + parentEvent.getGroup().getName());
		}
		else if( parentPost != null )
		{
			w.setGroup(parentPost.getGroup());
			Logger.getLogger(WhiteboardModel.class).info("Group set as " + parentPost.getGroup().getName());
		}
		else
		{
			w.setGroup(parentGroup);
			Logger.getLogger(WhiteboardModel.class).info("Group set as " + parentGroup.getName());
		}
		
		w.setNumEdits( 0 );
		
		HibernateUtil.currentSession().save(w);
		
		w.setSearchable(SearchableModel.newSearchable(null, null, w));
		
		return w;
	}
	
	public void save(String body, UserModel lastEditor)
	{
		setBody(body);
		setLastEditDate(new Date());
		setLastEditor(lastEditor);
		setNumEdits(getNumEdits() + 1);
		Helpers.currentDailyStats().logWhiteboardEdit();
		getSearchable().update();
	}
		
	public WhiteboardModel clone()
	{
		WhiteboardModel w2 = new WhiteboardModel();
		w2.setLastEditor(getLastEditor());
		w2.setBody(body);
		w2.setLastEditDate(lastEditDate);
		w2.setParentEvent(parentEvent);
	
		return w2;
	}
	
	public void delete()
	{
		setGroup(Helpers.getGroup("DeletedPosts"));
		getSearchable().delete();
	}

}
