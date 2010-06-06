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


import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.logic.TagLogic;



public class TagModel extends TagLogic
{

	public TagModel() {
		super();
	}
	
	public static TagModel getTag(String name)
	{
		List result = HibernateUtil.currentSession().createQuery("FROM TagModel WHERE uniquename=?")
        	.setString(0, name).list();
		if(result.size() == 0)
		{
			return null;
		}
		
		return (TagModel)result.get(0);
	}

	public static TagModel getOrCreateTag(String name)
	{
		TagModel t = TagModel.getTag(name);
		
		if(t == null)
		{
			t = new TagModel();
			t.setName(name);
			t.setUniqueName(name);
			HibernateUtil.currentSession().save(t);
		}
		return t;
	}
	
	public static void putCloudInCtx(UserModel currentUser, int numToShow, boolean posts, Context ctx)
	{
		Vector<String> words = new Vector<String>();
		Hashtable<String, Integer> percentTagged = new Hashtable<String, Integer>();
		Hashtable<String, Integer> usage = new Hashtable<String, Integer>();

		int i = 0;
		for (Object tag: TagLogic.getMatchingVisibleTags(currentUser, numToShow, posts))
		{
			Object[] tuple = (Object[])tag;
			String theWord = (String)tuple[0];
			words.add(theWord);
			usage.put(theWord, (Integer)tuple[1]);	
			percentTagged.put(theWord, new Integer(100*(numToShow-i)/numToShow) );
			i++;
		}

		Collections.sort(words); //alphabetically
		ctx.put("tags", words);
		ctx.put("percentTagged", percentTagged);
		ctx.put("usage", usage);
	}
	
}
