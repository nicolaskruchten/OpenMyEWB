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

import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;

import org.apache.velocity.context.Context;

import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.forms.GroupFileUploadForm;
import ca.myewb.model.GroupModel;


public class UploadGroupFile extends Controller
{
	public void handle(Context ctx) throws Exception
	{
		GroupModel g = (GroupModel)getAndCheckFromUrl(GroupModel.class);
		
		if(!Permissions.canManageFilesInGroup(currentUser, g))
		{
			throw getSecurityException("Can't upload to that group!", path + "/mailing/MyLists/");
		}
		
		GroupFileUploadForm form = (GroupFileUploadForm)checkForValidationFailure(ctx);
		
		String filePath = "";
		
		if(urlParams.getParams().length <= 1)
		{
			filePath = "/";
		}
		else
		{
			for( int i = 1; i < urlParams.getParams().length; i++ )
			{
				filePath += "/" + URLDecoder.decode(urlParams.getParams()[i],"UTF-8");
			}
		}
		
		if( form == null ){
			form = new GroupFileUploadForm(path + "/actions/SaveGroupFile/" + g.getId() + filePath, requestParams);
		}
		
		ctx.put("form", form);
		ctx.put("group", g);
		ctx.put("uploadPath", filePath);
	}

	public Set<String> invisibleGroups()
	{ 
		Set<String> s = new HashSet<String>();
		s.add("Users");

		return s;
	}

	public String displayName()
	{
		return "Upload Files to Group Files";
	}

	public int weight()
	{
		return 10;
	}
}
