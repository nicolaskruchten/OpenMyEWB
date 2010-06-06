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

package ca.myewb.controllers.actions;

import java.io.StringWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;

import ca.myewb.beans.GroupChapter;
import ca.myewb.frame.Controller;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.Message;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.forms.ListMembershipForm;
import ca.myewb.model.EmailModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class ModifyListMembership extends Controller
{
	/*
	 * WARNING!!!
	 * this code is very complex and should be handled with care!
	 */
	public void handle(Context ctx) throws Exception
	{
		//assume URLparam is listid
		GroupModel grp = null;
		String listIDString = urlParams.getParam();

		if ((listIDString == null) || (listIDString.equals("")))
		{
			listIDString = requestParams.get("List");

			if ((listIDString == null) || (listIDString.equals("")))
			{
				log.warn("hit ModListMembership with no list param!!!");

				if (getInterpageVar("isJoinListForm") != null)
				{

					throw getSecurityException("No list was specified!", path + "/mailing/ListMember");
				}
				else
				{
					throw getSecurityException("No list was specified!", Helpers.getDefaultURL());
				}
			}

			try
			{
				grp = (GroupModel)getAndCheck(GroupModel.class, new Integer(listIDString));
			}
			catch (Exception e)
			{
				throw getSecurityException("There was a problem with the URL you requested.", Helpers.getDefaultURL());
			}
		}
		else
		{
			grp = (GroupModel)getAndCheckFromUrl(GroupModel.class);
		}


		Boolean isLeaderForm = (Boolean)getInterpageVar("isLeaderForm");
		Boolean isNormalListForm = (Boolean)getInterpageVar("isNormalListForm");

		if (isLeaderForm == null)
		{
			isLeaderForm = false;
		}

		if (isNormalListForm == null)
		{
			isNormalListForm = false;
		}

		log.debug("leaderform: " + isLeaderForm + "; normalform: " + isNormalListForm);

		ListMembershipForm form = new ListMembershipForm(path
		                                                 + "/actions/ModifyListMembership/"
		                                                 + grp.getId(),
		                                                 requestParams,
		                                                 isLeaderForm,
		                                                 isNormalListForm);

		Message m = form.validate();

		if (m != null)
		{
			// Display error and prompt user to fix
			if (isLeaderForm)
			{
				throw getValidationException(form, m, path + "/mailing/ListMgmt/" + grp.getId());
			}
			else
			{
				throw getValidationException(form, m, path + "/mailing/ListInfo/" + grp.getId());
			}
		}

		String[] emails = form.getParameter("Emails").split("\n");

		filterRequest(form, emails, grp); //this call enforces security/request sanity

		String actionType = form.getParameter("ActionType");
		boolean grpIsChapter = grp.isChapter();
		boolean grpIsExec = grp.isExecList();
		boolean alternateRedirect = false;

		HashSet<String> emailsNoDupes = new HashSet<String>();
		for(String email: emails)
		{
			email = email.trim();
			if (!email.equals("")) //if it does, that's the guest user....
			{
				emailsNoDupes.add(email.toLowerCase());
			}
		}

		List<String> errors = new Vector<String>();
		TreeSet<String> toMail = new TreeSet<String>();
		
		for (String email: emailsNoDupes)
		{
			UserModel targetUser = UserModel.getUserForEmail(email);

			if (targetUser == null
			    && (actionType.equals("add")
			       || actionType.equals("upsender")
			       || actionType.equals("upleader")))
			{
				//we need to create a user
				targetUser = UserModel.newMailingListSignUp( email);
				
				// send the welcome email
				Template template = Velocity.getTemplate("emails/joinlist.vm");

				VelocityContext mailctx = new VelocityContext();
				mailctx.put("email", email);
				mailctx.put("totalshortname", grp.getTotalShortname());
				mailctx.put("helpers", new Helpers());
				
				if (currentUser.getUsername().equals("guest"))
				{
					mailctx.put("actor", "you");
				}
				else
				{
					mailctx.put("actor",
					        currentUser.getFirstname() + " "
					        + currentUser.getLastname());
				}

				StringWriter writer = new StringWriter();
				template.merge(mailctx, writer);
				EmailModel.sendEmail(email, writer.toString());
			}
			else if(targetUser == null)
			{
				//this is a removal, and the user doesn't even exist
				errors.add(email + " wasn't on the list");
			}

			if (targetUser != null)
			{
				//can't downgrade leaders of chapter or exec groups implicitly by unsubbing them
				if (actionType.equals("remove"))
				{
					if (!((grpIsChapter || grpIsExec) && targetUser.isLeader(grp, false)))
					{
						if (grpIsChapter)
						{
							//this code is only reachable if user is a leader
							//assuming a chapter leader or admin  is unsubbing members from the chapter list
							GroupChapterModel chapter = targetUser.getChapter();

							if ((chapter != null) && chapter.equals(grp))
							{
								//this group is the targetUser's own chapter: leave chapter
								targetUser.leaveChapter(chapter);

							}
							else
							{
								//this user is merely on another chapter's list: unsubscribe it
								targetUser.unsubscribe(grp);
							}
						}
						else
						{
							//generic list, just do what you're told
							targetUser.unsubscribe(grp);

							if (!grp.getPublic())
							{
								alternateRedirect = true;
							}
						}
					}
					else
					{
						errors.add(email
						           + " is a leader of this special list and wasn't removed");
					}
				}
				else if (actionType.equals("downsender"))
				{
					//can't downgrade leaders of chapter or exec groups implicitly by unsendering them
					if (!((grpIsChapter || grpIsExec)
					    && targetUser.isLeader(grp, false)))
					{
						targetUser.downgradeFromListSender(grp);
					}
					else
					{
						errors.add(email
						           + " is a leader of this special list and wasn't downgraded");
					}
				}
				else if (actionType.equals("downleader"))
				{
					targetUser.downgradeFromListLeader(grp);
				}
				else
				{
					boolean addToMail = false;
					
					//do we need to add them as a chapter member?
					if(!targetUser.isMember("Chapter")) //admins skip this block too
					{
						if (grp.isChapter())
						{
							//yup, this IS a chapter (they don't get re-added as recipients)
							addToMail = targetUser.joinChapter((GroupChapterModel)grp);
						}
						else if (grp.isExecList())
						{
							//yup, this is an exec list (wierd, but acceptable)
							addToMail = targetUser.joinChapter(grp.chapterIfExec());
						}
						else if (grp.getParent() != null)
						{
							//this is a chapter sub-list and the user has no chapter, so join it
							addToMail = targetUser.joinChapter(grp.getParent());
						}
					}
					
					if(actionType.equals("add"))
					{
						addToMail = targetUser.subscribe(grp) || addToMail;
					}
					else if (actionType.equals("upsender"))
					{
						addToMail = targetUser.upgradeToListSender(grp) || addToMail;
					}
					else if (actionType.equals("upleader"))
					{
						addToMail = targetUser.upgradeToListLeader(grp) || addToMail;
					}
					else
					{
						throw getSecurityException("Sorry, there was a minor server error.",
								"call to ModListMembership with invalid actionType! actionType="
						         + actionType + " user=" + currentUser.getUsername(),
						         getLeadPage());
					}
					
					if(addToMail)
					{
						toMail.add(targetUser.getEmail());
					}
				}
			}
		}
		
		if(!toMail.isEmpty() && grp.getWelcomeMessage() != null)
		{
			Vector<String> mailingList = new Vector<String>();
			mailingList.addAll(toMail);
			
			EmailModel.sendEmail( (grp.isChapter() ? ((GroupChapter)grp).getEmail() : Helpers.getSystemEmail()),
					mailingList, grp.getFullWelcomeEmail());
		}

		// Leave a message in the session
		if (errors.isEmpty())
		{
			setSessionMessage(("Done"));
		}
		else
		{
			String message = "Done, with exceptions: " +
					"<div align=\"center\"><ul style=\"color: black; padding: 0; margin: 0; margin-top:8px;\">";

			for (String error : errors)
			{
				message += ("<li style=\"margin-bottom: 5px;\">" + error + "</li>");
			}

			message += "</ul></div>";
			httpSession.setAttribute("message", new Message(message));
		}

		// Redirect to somewhere
		if (form.getParameter("Redirect", false) != null && !form.getParameter("Redirect", false).equals(""))
		{
			throw new RedirectionException(path + form.getParameter("Redirect", false));
		}
		else if (alternateRedirect)
		{
			throw new RedirectionException(path + "/mailing/Mailing");
		}
		else if (isLeaderForm)
		{
			throw new RedirectionException(path + "/mailing/ListMgmt/"
			                               + grp.getId());
		}
		else
		{
			throw new RedirectionException(path + "/mailing/ListInfo/"
			                               + grp.getId());
		}
	}

	private void filterRequest(ListMembershipForm form, String[] emails,
	                           GroupModel grp) throws Exception
	{
		String actionType = form.getParameter("ActionType");

		if(!grp.getVisible())
		{
			throw getSecurityException("Sorry, there was a minor server error.",
			                           "list is admin",
			                           Helpers.getDefaultURL());
		}
			
		if (currentUser.getUsername().equals("guest"))
		{
			if ((!actionType.equals("add")) || (emails.length > 1))
			{
				//user is trying non-guest action!
				throw getSecurityException("Sorry, there was a minor server error.",
				                           "non-guest action!",
				                           Helpers.getDefaultURL());
			}

			if (grp.getAdmin() && (!grp.equals(Helpers.getGroup("Org"))))
			{
				throw getSecurityException("Sorry, there was a minor server error.",
				                           "list is admin",
				                           Helpers.getDefaultURL());
			}
		}
		else if (grp.getAdmin())
		{
			throw getSecurityException("Sorry, there was a minor server error.",
			                           "list is admin", Helpers.getDefaultURL());
		}

		if (!grp.getPublic())
		{
			if (!(Permissions.canControlGroup(currentUser, grp)
			    || currentUser.isLeader(grp) || currentUser.isMember(grp)
			    || currentUser.isRecipient(grp) || currentUser.isSender(grp)))
			{
				//list is private and user is not a member
				throw getSecurityException("Sorry, there was a minor server error.",
				                           "list is private, cannot alter",
				                           Helpers.getDefaultURL());
			}
		}

		if (Permissions.canControlGroup(currentUser, grp))
		{
			if (grp.isExecList() || grp.isChapter())
			{
				if (actionType.equals("upleader")
				    || actionType.equals("downleader"))
				{
					throw getSecurityException("Sorry, there was a minor server error.",
					                           "trying to add/remove leader status from chapter or exec",
					                           Helpers.getDefaultURL());
				}
			}
		}
		else //currentUser is not leader or admin
		{
			if (actionType.equals("remove"))
			{
				if (grp.isChapter() && currentUser.isMember(grp, false))
				{
					//user is trying to remove self from chapter
					//redirect to LeaveChapter with a message
					setSessionErrorMessage(("To leave a chapter list, you must leave the chapter..."));
					throw new RedirectionException(path
					                               + "/actions/LeaveChapter");
				}
			}
			else if ((!actionType.equals("add")) || (emails.length > 1))
			{
				//user is trying leader action!
				throw getSecurityException("Sorry, there was a minor server error.",
				                           "user is trying leader action",
				                           Helpers.getDefaultURL());
			}
		}

		//if we get here, the request is ok and should proceed
		log.debug("modlist request passed filter");
	}

	public Set<String> invisibleGroups()
	{
		Set<String> s = new HashSet<String>();
		s.add("Org");

		//this page is visible to everyone, because even the guest user needs access
		return s;
	}

	public List<String> getNeededInterpageVars()
	{
		Vector<String> vars = new Vector<String>();
		vars.add("isLeaderForm");
		vars.add("isNormalListForm");
		vars.add("isJoinListForm"); //came from ListMember
		//vars.addAll(GenericConfirm.getRequiredInterpageVars());

		//these are so that the previous page can tell the action what type of form to return on failure
		return vars;
	}
}
