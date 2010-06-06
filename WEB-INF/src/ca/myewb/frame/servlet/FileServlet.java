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

package ca.myewb.frame.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.UserModel;
import ca.myewb.model.WhiteboardModel;


public class FileServlet extends HttpServlet
{
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	           throws ServletException, IOException
	{
		
		Logger log = Logger.getLogger(this.getClass());
		log.info("****** (post/whiteboard file)");
		log.info("requestURI= " + req.getRequestURI());
		log.info("referer= " + req.getHeader("Referer"));
		log.info("user-agent= " + req.getHeader("User-Agent"));

		try
		{
			Session s = HibernateUtil.currentSession();
			HttpSession httpSession = req.getSession();
			log.info("request URI = " + req.getRequestURI());
			String[] path = Helpers.getURIComponents(req.getRequestURI());
			String prefix = path[0];
			String id = path[1];
			String file = "";
			
			log.info("Prefix: " + prefix + ", ID: " + id );
			
			for( int i = 2; i < path.length ; i++ )
			{
				file += "/" + path[i];
			}
			
			file = URLDecoder.decode(file, "UTF-8");
			
			UserModel currentUser = WrapperServlet.getUser(Helpers.getDefaultURL(),
                    log, s, httpSession);

			String internalFolder = "";
			if(prefix.equals("postfile") )
			{
				internalFolder = "/posts/";
				
				PostModel thePost = (PostModel)s.get(PostModel.class, new Integer(id));
				if((thePost == null) || !Permissions.canReadPost(currentUser, thePost))
				{
					log.debug((thePost == null)? "The post is null" : currentUser.getUsername() + " does not have permission to see post number " + thePost.getId() + " belonging to " + thePost.getGroup().getName());
					throw new RedirectionException(Controller.path + "/home/ShowPost/" + id);
				}
			}
			else if(prefix.equals("whiteboardfile"))
			{
				internalFolder = "/whiteboards/";

				WhiteboardModel theWhiteboard = (WhiteboardModel)s.get(WhiteboardModel.class, new Integer(id));
				if(theWhiteboard == null || !theWhiteboard.isEnabled())
				{			
					throw new RedirectionException(Controller.path + "/events/EditWhiteboard/-1");
				}
				
				if (!Permissions.canUpdateWhiteboard(currentUser,theWhiteboard))	
				{	
					throw new RedirectionException(Controller.path + "/events/EventInfo/" + theWhiteboard.getParentEvent().getId());
				}
			}
			else if(prefix.equals("groupfiles"))
			{
				if( currentUser.getUsername().equals("guest") )
				{
					log.info("A guest tried to get to files in group number " + id);
					throw new RedirectionException(Controller.path + "/mailing/ShowGroupFiles/" + id );
				}
				
				internalFolder = "/groupfiles/";

				GroupModel theGroup = (GroupModel)s.get(GroupModel.class, new Integer(id));
				
				if((theGroup == null) || !Permissions.canReadFilesInGroup(currentUser, theGroup))
				{				
					throw new RedirectionException(Controller.path + "/mailing/ListInfo/" + id);
				}
				
			}
			else
			{
				httpSession.setAttribute("message",
                        new ErrorMessage("The URL you requested is invalid."));
				throw new RedirectionException(Helpers.getDefaultURL());
			}
	
			/////// SECURITY CHECKS COMPLETE
			
			File theFile = new File(Helpers.getUserFilesDir() + internalFolder + id + file);		
			OutputStream out = res.getOutputStream();

			InputStream in = null;

			try
			{
				in = new BufferedInputStream(new FileInputStream(theFile));

				String name = theFile.getName();
				boolean forceDownload = true;
				
				if(name.endsWith(".doc"))
				{
					res.setContentType("application/msword");
				}
				else if(name.endsWith(".zip"))
				{
					res.setContentType("application/zip");
				}
				else if(name.endsWith(".html") || name.endsWith(".html"))
				{
					res.setContentType("text/html");
				}
				else if(name.endsWith(".xls"))
				{
					res.setContentType("application/vnd.ms-excel");
				}
				else if(name.endsWith(".ppt"))
				{
					res.setContentType("application/vnd.ms-powerpoint");
				}
				else if(name.endsWith(".ppt"))
				{
					res.setContentType("application/vnd.ms-powerpoint");
				}
				else if(name.endsWith(".pdf"))
				{
					res.setContentType("application/pdf");
					forceDownload = false;
				}
				else if(name.endsWith(".jpg")||name.endsWith(".jpeg"))
				{
					res.setContentType("image/jpeg");
					forceDownload = false;
				}
				else if(name.endsWith(".gif"))
				{
					res.setContentType("image/gif");
					forceDownload = false;
				}
				else if(name.endsWith(".png"))
				{
					res.setContentType("image/png");
					forceDownload = false;
				}
				else
				{
					res.setContentType("application/x-download");
				}
				
					
				if(forceDownload)
				{
					res.setHeader("Content-Disposition",
				              "attachment; filename=\"" + name + "\"");
				}

				res.setContentLength((int) theFile.length());
				
				byte[] buf = new byte[4 * 1024]; // 4K buffer
				int bytesRead;

				while ((bytesRead = in.read(buf)) != -1)
				{
					out.write(buf, 0, bytesRead);
				}
			}
			catch (FileNotFoundException ex)
			{
				// If it was not a subdirectory-related problem,
				// keep throwing the error
				if (file.indexOf('/') == -1)
					throw ex;
			}
			finally
			{
				if (in != null)
				{
					in.close();
				}
			}
		}
		catch (RedirectionException re)
		{
			log.info("Clean redirect: " + re.getTargetURL(), re);
			res.sendRedirect(re.getTargetURL());
		}
		catch (Exception e)
		{
			log.error("FileServletError!", e);
			res.sendError(500, e.toString());
		}
	}
}
