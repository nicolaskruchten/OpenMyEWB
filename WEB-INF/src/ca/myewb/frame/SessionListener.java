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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


// Thanks to http://forum.java.sun.com/thread.jspa?threadID=529198&messageID=2544237
// for the skeleton code
public class SessionListener implements ServletContextListener,
                                        HttpSessionListener
{
	private static boolean needsInit = false;
	
	public void contextInitialized(ServletContextEvent e)
	{
		/* No work is done here because Helpers.getLocalRoot() is not yet valid.
		   Instead, we call doInit() from InitServlet, after Helpers.localRoot
		   has been initialized.
		   
		   The needsInit toggle is used because, apparently, the InitServlet
		   may be run multiple times in the lifetime of an app, while this
		   listener only runs once at startup...???
		   http://mail-archives.apache.org/mod_mbox/tomcat-users/200408.mbox/%3C9C5166762F311146951505C6790A9CF80229BDA9@US-VS1.corp.mpi.com%3E
		 */

		needsInit = true;
	}

	public static void doInit(ServletContext ctx)
	{
		if (!needsInit)
			return;
		
        Set sessionList = new HashSet();
        Hashtable users = new Hashtable();
        Hashtable userTimes = new Hashtable();
        
        try
        {
//          ObjectInputStream sessInput = new ObjectInputStream(new FileInputStream(Helpers.getLocalRoot() + "/work/sessions.save"));
          ObjectInputStream userInput = new ObjectInputStream(new FileInputStream(Helpers.getLocalRoot() + "/work/users.save"));
          ObjectInputStream userTimeInput = new ObjectInputStream(new FileInputStream(Helpers.getLocalRoot() + "/work/usertimes.save"));
          
//          sessionList = (Set)sessInput.readObject();
          users = (Hashtable)userInput.readObject();
          userTimes = (Hashtable)userTimeInput.readObject();
        }
        catch (IOException ex)
        {
          // Fail silently; this only means that we lose "who's online" data
        }
        catch (ClassNotFoundException ex)
        {
          // Fail silently; this only means that we lose "who's online" data
        }
        catch (Exception ex)
        {
        }
        
        ctx.setAttribute("sessionList", sessionList);
        ctx.setAttribute("userList", users);
        ctx.setAttribute("userTime", userTimes);
        
        needsInit = false;
	}
	
	public void contextDestroyed(ServletContextEvent e)
	{
		needsInit = true;
		
        Set sessionList = (Set)e.getServletContext().getAttribute("sessionList");
        Hashtable users = (Hashtable)e.getServletContext().getAttribute("userList");
        Hashtable userTime = (Hashtable)e.getServletContext().getAttribute("userTime");

        try
        {
          new ObjectOutputStream(new FileOutputStream(Helpers.getLocalRoot() + "/work/sessions.save")).writeObject(sessionList);
          new ObjectOutputStream(new FileOutputStream(Helpers.getLocalRoot() + "/work/users.save")).writeObject(users);
          new ObjectOutputStream(new FileOutputStream(Helpers.getLocalRoot() + "/work/usertimes.save")).writeObject(userTime);
        }
        catch (IOException ex)
        {
          // Fail silently; this only means that we lose "who's online" data
        }
        catch (Exception ex)
        {
        }
	}

	@SuppressWarnings("unchecked")
	public void sessionCreated(HttpSessionEvent e)
	{
		HttpSession session = e.getSession();
		Set sessionList = (Set)session.getServletContext()
		                  .getAttribute("sessionList");
		sessionList.add(new String(session.getId()));
	}

	public void sessionDestroyed(HttpSessionEvent e)
	{
		HttpSession session = e.getSession();
		Integer userid = (Integer)e.getSession().getAttribute("userid");

		if ((userid != null) && (userid.intValue() != 1))
		{
			Hashtable users = (Hashtable)session.getServletContext()
			                  .getAttribute("userList");
			users.remove(userid);
			
			Hashtable userTime = (Hashtable)session.getServletContext()
            					 .getAttribute("userTime");
			userTime.remove(userid);
		}

		Set sessionList = (Set)session.getServletContext()
		                  .getAttribute("sessionList");
		sessionList.remove(new String(session.getId()));
	}
}
