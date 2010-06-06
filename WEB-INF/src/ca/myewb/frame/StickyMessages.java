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

package ca.myewb.frame;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;

import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class StickyMessages
{
	private Session session;
	private UserModel user;
	private String url;
	private boolean isRegular = false;
	private boolean isExec = false;
	private String base;

	public StickyMessages(UserModel user, String url)
	               throws HibernateException
	{
		this.user = user;
		this.session = HibernateUtil.currentSession();
		this.url = url;
		base = Helpers.getAppPrefix();
	}

	public List<StickyMessage> getMessages()
	                                throws HibernateException, Exception
	{
		LinkedList<StickyMessage> msgs = new LinkedList<StickyMessage>();
		
		addExpiryMessages(msgs);
		if(!msgs.isEmpty())
		{
			return msgs;
		}

		addGroupMessages(msgs);
		if(!msgs.isEmpty())
		{
			return msgs;
		}
		
		addMissingInfoMessages(msgs);
		if(!msgs.isEmpty())
		{
			return msgs;
		}
		
		addExecMessages(msgs);
		return msgs;
	}

	private void addExpiryMessages(LinkedList<StickyMessage> msgs)
	{
		if (!isRegular)
		{
			return;
		}
		
		if (user.canRenew() &&
				(!url.contains("/profile/PayDues")) && 
				(!url.contains("/profile/EditProfile")) && 
				(!url.contains("/profile/EditProfileIntlAddress")) && 
				(!url.contains("/profile/EditProfileCdnAddress")))
		{
			Date expiry = user.getExpiry();

			Calendar cal = Calendar.getInstance();
			cal.setTime(expiry);
			cal.add(Calendar.YEAR, 1);
			msgs.add(new StickyMessage("Your regular membership will expire "
			                           + Helpers.formatDate(expiry)
			                           + "!<br />" + "<a href=\"" + base
			                           + "/profile/PayDues\">Click here to renew your membership until "
			                           + Helpers.formatAbsDate(cal.getTime()) + " &raquo;</a>", false));
		}
	}

	private void addMissingInfoMessages(LinkedList<StickyMessage> msgs)
	{
		if (!isRegular)
		{
			return;
		}
		
		if ((!url.contains("/profile/EditProfile")) && 
				(!url.contains("/profile/EditProfileIntlAddress")) && 
				(!url.contains("/profile/EditProfileCdnAddress")) && 
				((user.getAddress() == null) || (user.getPhone() == null)))
		{
			msgs.add(new StickyMessage("We need some more information to make you a regular member.<br />"
			                           + "Please click <a href=\"" + base 
			                           + "/profile/EditProfile\">here</a> to fill it in.", false));
		}
		
	}

	
	private void addExecMessages(LinkedList<StickyMessage> msgs)
	{
		if(!isExec)
		{
			return;
		}
		
		String title = "";
		String pic = "";
		String reg = "";
		
		if(user.getExecTitle().equals(""))
		{
			title = "<li style=\"margin-bottom: 5px;\">You should have an executive title. <a href=\"/chapter/ExecTitle/" + user.getId() + "\">choose a title &raquo;</a></li>";
		}	
		
		if(!isRegular)
		{
			reg = "<li style=\"margin-bottom: 5px;\">You should be a regular member. <a href=\"/profile/PayDues\">become a regular member &raquo;</a></li>";
		}
		
		if(!user.hasPicture())
		{
			pic = "<li style=\"margin-bottom: 5px;\">Your user profile should include a picture. <a href=\"/profile/ChangePicture\">upload a picture &raquo;</a></li>";
		}
		
		if(!title.equals("") || !pic.equals("") || ! reg.equals(""))
		{
			msgs.add(new StickyMessage("As an executive of your chapter, <br />you are strongly encouraged to address the following issues with your user account: "
					+  "<div align=\"center\"><ul style=\"color: black; padding: 0; margin: 0; margin-top:8px;\">"
					+ title + reg + pic + "</ul></div>", false));
		}
	}

	private void addGroupMessages(LinkedList<StickyMessage> msgs)
	{
		List<GroupModel> userGroups = user.getGroups('m');
		
		StickyMessage m = null;
		
		for(GroupModel g: userGroups)
		{
			if(g.getAdmin())
			{
				if((g.getMessage() != null) && !g.getMessage().equals(""))
				{
					m = new StickyMessage(g.getMessage(), true);
				}
				
				if(g.getShortname().equals("Exec"))
				{
					isExec = true;
				}
				
				if(g.getShortname().equals("Regular"))
				{
					isRegular = true;
				}
			}
		}
		if(m != null)
		{
			msgs.add(m);
		}
	}
}
