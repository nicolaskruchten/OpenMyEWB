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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import ca.myewb.beans.Post;
import ca.myewb.frame.FileNameWrapper;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.PostOrder;
import ca.myewb.model.PostModel;
import ca.myewb.model.SearchableModel;
import ca.myewb.model.TagModel;
import ca.myewb.model.WhiteboardModel;

public abstract class PostLogic extends Post {
	
	protected int newReplies;
	private PostModel lastReply;

	protected PostLogic(){
		super();
		newReplies = -1;
		lastReply = null;
	}

	public TreeSet<String> getSortedTags() {
		TreeSet<String> sorted = new TreeSet<String>();
	
		for (TagLogic tag : tags)
		{
			sorted.add(tag.getName());
		}
	
	
		return sorted;
	}

	public void addTag(TagLogic t) {
		tags.add((TagModel)t);
		t.addPost(this);
	}

	public void remTag(TagLogic t) {
		tags.remove(t);
		t.remPost(this);
	}

	public void addReply(PostLogic p) {
		if (p.getParent() != null)
		{
			p.getParent().remReply(p);
		}
	
		p.setParent(this);
		replies.add((PostModel)p);
		group.addPost(p);
	}

	protected void remReply(PostLogic p) {
		replies.remove(p);
	}

	public boolean equals(PostLogic p) {
		return ((getPoster() == p.getPoster()) && (group == p.getGroup())
		       && (subject.equals(p.getSubject()))
		       && (body.equals(p.getBody())) && (date.equals(p.getDate())));
	}

	public String getStitchedPost() {
		String postText;
	
		if (intro.length() > 3 && body.length() > 3
			&& intro.substring(intro.length() - 3).equals("...")
		    && body.substring(0, 3).equals("..."))
		{
			postText = intro.substring(0, intro.length() - 3)
			           + body.substring(3);
		}
		else if(intro.trim().equals(""))
		{
			postText = body;
		}
		else if(body.trim().equals(""))
		{
			postText = intro;
		}
		else
		{
			postText = intro + "\n\n" + body;
		}
		
		return postText;
	}

	public int getNewReplies(Date lastLogin) 
	{
		if((parent == null) && (newReplies == -1))
		{
			List result = HibernateUtil.currentSession().createQuery("SELECT COUNT(p) FROM PostModel p WHERE p.date > :lastLogin AND p.parent= :this")
            .setTimestamp("lastLogin", lastLogin)
            .setEntity("this", this).list();

			newReplies = ((Long)result.get(0)).intValue();
		}
		
		return newReplies;
	}

	public PostModel getLatestReply() {
		if(lastReply == null && !replies.isEmpty())
		{
			List<PostModel> ordered = new LinkedList<PostModel>();
			ordered.addAll(replies);
			Collections.sort(ordered, new PostOrder());
	
			lastReply = ordered.get(0);
		}
		return lastReply;
	}

	public boolean hasFilesInSelfOrResponses()
	{
		if(this.hasfile)
		{
			return true;
		}
		
		if(this.hasActiveWhiteboard() && this.getWhiteboard().getHasfile())
		{
			return true;
		}
		
		for(Post reply:this.replies)
		{
			if(reply.getHasfile())
			{
				return true;
			}
		}
				
		return false;
	}
	
	public List getAttachedFiles() {
		Vector<FileNameWrapper> zipFiles = new Vector<FileNameWrapper>();
	
		if (this.hasfile)
		{
			File theDir = new File(Helpers.getUserFilesDir() + "/posts/" + this.id);
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
	
	public void feature()
	{
		setFeatured(true);
		
		for(PostModel p: getReplies())
		{
			p.setFeatured(true);
		}
	}
	
	public void unfeature()
	{
		setFeatured(false);
		
		for(PostModel p: getReplies())
		{
			p.setFeatured(false);
		}
	}
	
	public boolean hasActiveWhiteboard()
	{
		return getWhiteboard() != null;
	}
	
	public WhiteboardModel getWhiteboard() 
	{
		if(whiteboards.size() == 0)
			return null;
		
		return whiteboards.iterator().next();
	}

	public void setWhiteboard(WhiteboardModel whiteboard) 
	{
		if(whiteboards.size() != 0)
			whiteboards.remove(whiteboards.iterator().next());
		
		whiteboards.add(whiteboard);
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
