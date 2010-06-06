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

package ca.myewb.controllers.mailing;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.velocity.context.Context;

import ca.myewb.frame.FileNameWrapper;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.toolbars.ListControl;
import ca.myewb.frame.toolbars.Toolbar;
import ca.myewb.model.GroupModel;


public class ShowGroupFiles extends Controller
{
	private final int defaultDepth = 2;
	
	public void handle(Context ctx) throws Exception
	{
		GroupModel g = (GroupModel)getAndCheckFromUrl(GroupModel.class);

		if (!Permissions.canReadFilesInGroup(currentUser, g))
		{
			if (currentUser.getUsername().equals("guest"))
			{
				setInterpageVar("requestedURL",
				                path + "/mailing/ShowGroupFiles" + urlParams.getParam());
				setSessionMessage(("Please sign in to see the files you requested"));
				throw new RedirectionException(path + "/home/SignIn");
			}
			else
			{
				throw getSecurityException("Can't send mail to this list!", path + "/mailing/Mailing");
			}
		}
		
		File root = new File( Helpers.getUserFilesDir() + "groupfiles/" + g.getId() + "/");
		LinkedList<FileNameWrapper> directories = new LinkedList<FileNameWrapper>();
		LinkedList<FileNameWrapper> files = new LinkedList<FileNameWrapper>();
		
		if(root.exists())
		{

			TreeSet<File> dirList = new TreeSet<File>(Arrays.asList(root.listFiles()));
			
			for( File f : dirList )
			{
				if(!f.getName().equals(".trash"))
				{
					if(f.isDirectory())
					{
						directories.add(new FileNameWrapper(f));
					}
					else if(f.isFile())
					{
						files.add(new FileNameWrapper(f));
					}
				}
			}
		}
		log.info("Document Root set at " + root.getAbsolutePath());
			
		ctx.put("directories", directories);
		ctx.put("files", files);
		ctx.put("relPath", "");
		ctx.put("group", g);
		ctx.put("canEdit", Permissions.canManageFilesInGroup(currentUser, g));
		ctx.put("pathHash", new FileNameWrapper(root).getMD5Hash());
		
		log.info("dirID set as " + g.getId());
		
		//Show Toolbar
		Vector<Toolbar> toolbars = new Vector<Toolbar>();
		ListControl theToolbar = new ListControl(ctx, currentUser, g);
		toolbars.add(theToolbar);
		ctx.put("toolbars", toolbars);
		ctx.put("list", g);
		
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}

	public String displayName()
	{
		try{
			GroupModel g = (GroupModel)getAndCheckFromUrl(GroupModel.class);
			return g.getName() + "'s Files";
		}
		catch(Exception e){
			return "Files";
		}
	}

	public int weight()
	{
		return -1;
	}
	
}
