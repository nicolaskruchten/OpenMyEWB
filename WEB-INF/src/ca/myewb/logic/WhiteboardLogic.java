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

package ca.myewb.logic;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import ca.myewb.beans.Whiteboard;
import ca.myewb.frame.FileNameWrapper;
import ca.myewb.frame.Helpers;
import ca.myewb.model.SearchableModel;

public abstract class WhiteboardLogic extends Whiteboard {


	protected WhiteboardLogic(){
		super();
	}

	public boolean equals(WhiteboardLogic w) {
		return ((lastEditor == w.getLastEditor()) && (parentEvent.getGroup() == w.getParentEvent().getGroup())
		       && (body.equals(w.getBody())) && (lastEditDate.equals(w.getLastEditDate())));
	}
	
	public List getAttachedFiles() {
		Vector<FileNameWrapper> zipFiles = new Vector<FileNameWrapper>();
	
		if (this.hasfile)
		{
			File theDir = new File(Helpers.getUserFilesDir() + "/whiteboards/" + this.id);
			File[] fileArray = theDir.listFiles();
			Arrays.sort(fileArray);
			Vector<FileNameWrapper> files = new Vector<FileNameWrapper>();
	
			for (File theFile: fileArray)
			{
				if (theFile.toString().endsWith(".zip"))
					zipFiles.add(new FileNameWrapper(theFile));
				else
					files.add(new FileNameWrapper(theFile));
			}
			
			zipFiles.addAll(files);
		}
	
		return zipFiles;
	}
	
	public Object getParent()
	{
		if(getParentEvent() != null)
		{
			return getParentEvent();
		}
		else if(getParentPost() != null)
		{
			return getParentPost();
		}
		else if(getParentGroup() != null)
		{
			return getParentGroup();
		}
		
		return null;
	}
	
	public String getParentType()
	{
		if(getParentEvent() != null)
		{
			return "event";
		}
		else if(getParentPost() != null)
		{
			return "post";
		}
		else if(getParentGroup() != null)
		{
			return "group";
		}
		
		return null;
	}
	
	public SearchableModel getSearchable()
	{
		if(searchables.size() == 0)
			return null;
		
		return searchables.iterator().next();
	}

	public void setSearchable(SearchableModel searchable)
	{
		if(searchables.size() != 0)
			searchables.remove(searchables.iterator().next());
		
		searchables.add(searchable);
	}

}
