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


import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hibernate.Session;

import ca.myewb.beans.Post;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.SafeHibList;
import ca.myewb.frame.servlet.AjaxServlet;
import ca.myewb.logic.PostLogic;
import ca.myewb.logic.TagLogic;


public class PostModel extends PostLogic
{

	PostModel() {
		super();
	}
	
	public static PostModel newPost(UserModel currentUser, GroupModel targetGroup, 
			String subject, String intro, String body, String unsplitTags)
	{
		return newPost(currentUser, targetGroup, subject, intro, body, unsplitTags, false);
	}
	
	public static PostModel newPost(UserModel currentUser, GroupModel targetGroup, 
			String subject, String intro, String body, String unsplitTags, boolean whiteboard)
	{
		Session hibernateSession = HibernateUtil.currentSession();
		PostModel p = new PostModel();
		hibernateSession.save(p);
		AjaxServlet.invalidateFrontPageCache(targetGroup.getId());
		
		p.setSubject(subject);
		p.setIntro(intro);
		
		if(!body.trim().equals(""))
		{
			p.setBody(body);
		}

		targetGroup.addPost(p);
		
		for(String tag: TagLogic.extractTagNames(unsplitTags))
		{
			p.addTag(TagModel.getOrCreateTag(tag));
		}
		
		currentUser.addPost(p);

		hibernateSession.flush();
		
		if( whiteboard )
		{
			p.setWhiteboard(WhiteboardModel.newWhiteboard(null, p, null));
		}
		Helpers.currentDailyStats().logPost();
		
		p.setSearchable(SearchableModel.newSearchable(p, null, null));
		
		return p;
	}
	
	
	public PostModel reply(UserModel currentUser, String body, String tags)
	{
		if( !hasActiveWhiteboard() )
		{
			Session hibernateSession = HibernateUtil.currentSession();
			PostModel p = new PostModel();
			hibernateSession.save(p);
			AjaxServlet.invalidateFrontPageCache(getGroup().getId());
	
			p.setBody(body);
			addReply(p); //sets group on reply
			
			Logger.getLogger(this.getClass()).info("Post "
					+ this.getId()
					+ " has "
					+ this.getReplies().size()
					+ " replies.");
			
			if(this.getReplies().size() == Post.RepliesToFeature)
			{
				this.feature();
			}
			
			if(this.isFeatured())
			{
				p.feature();
			}
			
			for(String tag: TagLogic.extractTagNames(tags))
			{
				addTag(TagModel.getOrCreateTag(tag));
			}
			
			currentUser.addPost(p);
			
			setLastReply(new Date());
	
			hibernateSession.flush();
			Helpers.currentDailyStats().logReply();
			
			p.setSearchable(SearchableModel.newSearchable(p, null, null));
			
			return p;
		}
		else
		{
			Logger.getLogger(this.getClass()).debug("Tried to reply to a post that has a whiteboard.");
			return null;
		}
		
	}

	public void delete()
	{
		delete(true);
	}
	
	public void delete(boolean removeFromParent)
	{
		AjaxServlet.invalidateFrontPageCache(getGroup().getId());
		if (parent == null)
		{
			//we're a toplevel post
			
			if(getWhiteboard() == null)
			{
				Iterator<PostModel> it = replies.iterator();
				while (it.hasNext())
				{
					PostModel reply = it.next();
					reply.delete(false);
					it.remove();
					reply.setParent(null);
				}
			}
			else
			{
				getWhiteboard().delete();
			}
		}
		else
		{
			//we're a reply
			subject = "reply";
			intro = "parent's subject was: " + parent.getSubject();
			
			Calendar lReply = GregorianCalendar.getInstance();
			Calendar rCal = GregorianCalendar.getInstance();
			lReply.setTime(parent.getDate());
			
			for( PostModel r : parent.getReplies() )
			{
				rCal.setTime(r.getDate());				
				if(!r.getGroup().equals(Helpers.getGroup("DeletedPosts")) &&
						lReply.before(rCal) && !r.equals(this))
				{
					lReply.setTime(r.getDate());
				}
			}
			
			parent.setLastReply(lReply.getTime());
			if(removeFromParent)
			{
				parent.getReplies().remove(this);
				parent = null;
			}
		}

		subject = "*deleted* " + subject;
		intro = "deleted " + (new Date()).toString() + ", original group was: "
		        + group.getName() + "\n- - -\n" + intro;
		this.group = Helpers.getGroup("DeletedPosts");
		getSearchable().delete();
	}

	public PostModel clone()
	{
		PostModel p2 = new PostModel();
		p2.setPoster(getPoster());
		p2.setGroup(group);
		p2.setSubject(subject);
		p2.setIntro(intro);
		p2.setBody(body);
		p2.setDate(date);
		p2.setTags(tags);
		p2.setParent(parent);
		p2.setReplies(replies);
		p2.newReplies = newReplies;
		p2.setEmailed(emailed);
	
		return p2;
	}
	

	public void sendAsEmail(String sender) throws Exception
	{
		sendAsEmail(sender, null);	
	}
	
	public void sendAsEmail(String sender, List<String> excludedEmails) throws Exception
	{
		String htmlMessage = doTemplateMerge(this, "emails/post.html.vm");
		String textMessage = doTemplateMerge(this, "emails/post.txt.vm");

		String shortname = getGroup().getTotalShortname();
		String fullSubject;
		
		if(parent == null)
		{
			this.setEmailed(true);
			fullSubject = "[" + shortname + "] " + subject;
		}
		else
		{
			fullSubject = "Re: [" + shortname + "] " + parent.getSubject();
		}
		
		List<String> groupMemberEmails = getGroup().getMemberEmails();
		if(excludedEmails != null)
		{
			groupMemberEmails.removeAll(excludedEmails);
		}
		
		if(!groupMemberEmails.isEmpty())
		{
			EmailModel.sendEmail(sender, groupMemberEmails, fullSubject, textMessage, htmlMessage, shortname, true);
		}
	}
	
	public void sendAsWatchListEmail(String sender, List<String> emailsForReplies) throws Exception
	{
		String htmlMessage = doTemplateMerge(this, "emails/post.html.vm");
		String textMessage = doTemplateMerge(this, "emails/post.txt.vm");

		String shortname =  Helpers.getEnShortName() + "-watchlist";
		String fullSubject  = "Re: [" + shortname + "] " + parent.getSubject();
		
		EmailModel.sendEmail(sender, emailsForReplies, fullSubject, textMessage, htmlMessage, shortname, false);
	}
	
	private String doTemplateMerge(PostLogic post, String templatePath) throws Exception 
	{
		Template template = Velocity.getTemplate(templatePath);
		VelocityContext ctx = new VelocityContext();
		ctx.put("post", post);
		ctx.put("helpers", new Helpers());
	
		StringWriter writer = new StringWriter();
		template.merge(ctx, writer);
	
		String toString = writer.toString();
	
		return toString;
	}
	
	public static Date getLatestReplyDate()
	{
		String sql = " SELECT max(`lastReply`) FROM posts ";
		return (Date)HibernateUtil.currentSession().createSQLQuery(sql).uniqueResult();
	}

	public List<String> getEmailsForReplies() 
	{
		return new SafeHibList<String>(HibernateUtil.currentSession().createSQLQuery("SELECT DISTINCT u.email FROM users u, flaggedposts p WHERE u.repliesasemails = 0b1 AND u.id = p.userid AND p.postid = :pid").setInteger("pid", getId())).list();
	}
	

}
