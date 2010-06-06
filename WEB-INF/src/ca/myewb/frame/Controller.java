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

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.velocity.context.Context;
import org.hibernate.Session;

import ca.myewb.frame.forms.Form;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public abstract class Controller
{
	public static final String path = Helpers.getAppPrefix();
	protected Session hibernateSession;
	protected HttpSession httpSession;
	protected Logger log = Logger.getLogger(this.getClass());
	public PostParamWrapper requestParams;
	protected GetParamWrapper urlParams;
	protected UserModel currentUser;
	private HttpServletRequest request = null;
	private HttpServletResponse response = null;

	public abstract void handle(Context ctx) throws Exception;

	public Set<String> defaultGroups()
	{
		return null;
	}

	public Set<String> invisibleGroups()
	{
		return null;
	}
	
	public boolean secureAccessRequired()
	{
		return false;
	}

	public String displayName()
	{
		return this.getClass().getName()
		       .substring(this.getClass().getName().lastIndexOf('.') + 1);
	}

	public String oldName()
	{
		return null;
	}
	
	public int weight()
	{
		return 0;
	}

	public void setHibernateSession(Session session)
	{
		this.hibernateSession = session;
	}

	public void setHttpSession(HttpSession httpSession)
	{
		this.httpSession = httpSession;
	}

	public void setRequestParams(PostParamWrapper requestParams)
	{
		this.requestParams = requestParams;
	}
	
	public void setHttpRequest(HttpServletRequest request)
	{
		this.request = request;
	}
	
	public void setHttpResponse(HttpServletResponse response)
	{
		this.response = response;
	}

	public List<String> getNeededInterpageVars()
	{
		return new Vector<String>();
	}

	@SuppressWarnings("unchecked")
	protected void setInterpageVar(String name, Object var)
	{
		Hashtable<String, Object> hashtable = (Hashtable)httpSession
		                                                                     .getAttribute("interpageVars");
		hashtable.put(name, var);
	}

	protected Object getInterpageVar(String name)
	{
		return ((Hashtable)httpSession.getAttribute("interpageVars")).get(name);
	}

	protected void removeInterpageVar(String name)
	{
		((Hashtable)httpSession.getAttribute("interpageVars")).remove(name);
	}
	
	@SuppressWarnings("unchecked")
	protected PostParamWrapper getStoredParams(String datestamp)
	{
		try
		{
			log.debug("Number of stored params: " + ((Hashtable<String, PostParamWrapper>)httpSession.getAttribute("storedParams")).size());	
			return ((Hashtable<String, PostParamWrapper>)httpSession.getAttribute("storedParams")).get(datestamp);
		}
		catch( Exception e )
		{
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected Form getStoredForm(String datestamp)
	{
		try
		{		
			return ((Hashtable<String, Form>)httpSession.getAttribute("storedForms")).get(datestamp);
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void setConfirmData(String datestamp, Form f, PostParamWrapper p)
	{
		((Hashtable<String, PostParamWrapper>)httpSession.getAttribute("storedParams")).put(datestamp, p);
		if( f != null )
		{
			((Hashtable<String, Form>)httpSession.getAttribute("storedForms")).put(datestamp, f);
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void removeConfirmData(String datestamp)
	{
		((Hashtable<String, PostParamWrapper>)httpSession.getAttribute("storedParams")).remove(datestamp);
		if(((Hashtable<String, Form>)httpSession.getAttribute("storedForms")).containsKey(datestamp))
		{
			((Hashtable<String, Form>)httpSession.getAttribute("storedForms")).remove(datestamp);
		}
	}

	public void setCurrentUser(UserModel currentUser)
	{
		this.currentUser = currentUser;
	}

	public void setUrlParams(GetParamWrapper urlParams)
	{
		this.urlParams = urlParams;
	}

	protected boolean isOnConfirmLeg()
	{
		return requestParams.get("confirmed") != null;
	}

	protected void requireConfirmation(String bigMessage, String littleMessage,
	                                   String cancelURL, String confirmURL,
	                                   String area, Form form)
	                            throws RedirectionException
	{
		requireConfirmation(bigMessage, littleMessage, cancelURL, confirmURL, area, form, false);
	}
	
	protected void requireConfirmation(String bigMessage, String littleMessage,
                String cancelURL, String confirmURL,
                String area, Form form, boolean fileMessage)
         throws RedirectionException
    {
		//NOTE: controllers calling this must have interpagevars from GenericConfirm.getRequiredInterpageVars

		//from the point of view of the controller, calling this function acts as a passthrough if confirmed
		//although the controller must create the form it intends to use AFTER the call, even if it was created before for validation
		//if not confirmed, if a form was passed in, that form is passed back to the cancelURL with a formmessage
		//if no form was passed in, the user just lands at the cancelURL
		if (requestParams.get("confirmed") != null)
		{
			String datestamp = requestParams.get("datestamp");
			
			if (requestParams.get("confirmed").equals("yes"))
			{
				requestParams = getStoredParams(datestamp); 

				if (requestParams == null) //we've somehow lost the interpagevars
				{
					log.debug("We've lost the interpagevars!  Redirecting to "
					          + cancelURL);

					removeConfirmData(datestamp);
					
					throw new RedirectionException(cancelURL);
				}
				
				removeConfirmData(datestamp);

				return;
			}
			else //should be 'no'
			{
				Form storedForm = getStoredForm(datestamp); 

				if (storedForm != null)
				{
					if(fileMessage)
					{
						setSessionErrorMessage(("You didn't confirm: no action taken<br />Please reattach any files before resubmitting."));
					}
					else
					{
						setSessionErrorMessage(("You didn't confirm: no action taken"));
					}
					httpSession.setAttribute("form", storedForm);
				}
				
				removeConfirmData(datestamp);

				throw new RedirectionException(cancelURL);
			}
		}
		else
		{
			String datestamp = Long.toString(System.currentTimeMillis());
			
			log.info("Form data saved with datestamp: " + datestamp);
			
			setConfirmData(datestamp, form, requestParams);

			setInterpageVar("datestamp", datestamp);
			setInterpageVar("bigMessage", bigMessage);
			setInterpageVar("littleMessage", littleMessage);
			setInterpageVar("confirmURL", confirmURL);
			
			throw new RedirectionException(path + "/" + area + "/Confirm");
		}
	}

	protected int requireIdUrlParam(String paramName)
	                         throws RedirectionException
	{
		String param = null;

		if (paramName == null)
		{
			param = urlParams.getParam(); //look at first parameter
		}
		else
		{
			param = urlParams.get(paramName);
		}


		try
		{
			param = param.replaceAll("\\.", "").replaceAll(",", "");
			
			int trial = new Integer(param);

			if (trial < 1)
			{
				throw new Exception();
			}
			else
			{
				return trial;
			}
		}
		catch (Exception e) //presumably an NPE or NFE or the number is negative
		{
			throw getSecurityException("The previously requested URL was invalid.",
			                           "invalid or missing urlParam", getLeadPage());
		}
	}

	protected Object getAndCheckFromUrl(Class clazz)
	                             throws RedirectionException
	{
		return getAndCheckFromUrl(clazz, null); //look at first parameter
	}

	protected Object getAndCheckFromUrl(Class clazz, String paramName)
	                             throws RedirectionException
	{
		int id;

		if(clazz.equals(GroupModel.class) || clazz.equals(GroupChapterModel.class))
		{
			String name = "";
			if (paramName == null)
			{
				name = urlParams.getParam(); //look at first parameter
			}
			else
			{
				name = urlParams.get(paramName);
			}
				
			try
			{
				GroupChapterModel g = (GroupChapterModel)HibernateUtil.currentSession()
			       .createQuery("FROM GroupChapterModel g where g.shortname=?")
			       .setString(0, name).uniqueResult();
				if(g != null)
				{
					return getAndCheck(clazz, g.getId());
				}
			}
			catch(Exception e)
			{
				log.warn("can't happen");
			}
		}
		
		try
		{
			id = requireIdUrlParam(paramName);
		}
		catch (RedirectionException e)
		{
			//log here again so we can catch WHAT she was trying to load with a bad ID
			log.warn("tried to request a " + clazz.getName() + " with a bad id");
			throw e;
		}

		return getAndCheck(clazz, id);
	}

	public Object getAndCheck(Class clazz, Serializable id)
	                      throws RedirectionException
	{
		//this is basically a session.load() replacement, 
		//as per the hib docs's recommendation to use get() and then check the result
		Object obj = hibernateSession.get(clazz, id);

		if (obj != null)
		{
			return obj;
		}
		else
		{
			throw getSecurityException("There was a minor server error... sorry!",
			                           "bad id for " + clazz.getName(),
			                           getLeadPage());
		}
	}

	protected String getLeadPage()
	{
		String[] pkgs = this.getClass().getName().split("\\.");

		if (pkgs[pkgs.length - 2].equals("actions"))
		{
			return Helpers.getDefaultURL();
		}

		String leadPage = path + "/" + pkgs[pkgs.length - 2] + "/"
		                  + pkgs[pkgs.length - 2].substring(0, 1).toUpperCase()
		                  + pkgs[pkgs.length - 2].substring(1);

		return leadPage;
	}

	public RedirectionException getSecurityException(String message,
	                                                    String returnPath)
	{
		return getSecurityException(message, null, returnPath);

		//most of the time, a note isn't needed, the message is descriptive enough
	}

	protected RedirectionException getSecurityException(String message,
	                                                    String note,
	                                                    String returnPath)
	{
		//leave a message in session
		httpSession.setAttribute("message", new ErrorMessage(message));

		//security logging
		String logmsg = "user " + currentUser.getUsername()
		                + " got message: \"" + message + "\" when accessing "
		                + this.getClass().getName() + " with GET params="
		                + urlParams.toString() + " and POST params="
		                + requestParams.toString();

		if (note != null)
		{
			logmsg += (" NOTE: " + note);
		}

		log.warn(logmsg);

		//return the exception to throw
		return new RedirectionException(returnPath, true);
	}

	protected Form checkForValidationFailure(Context ctx)
	{
		//we return a form which the controller generally then tries to downcast
		//that's just an extra check in case the flow got messed up
		Form possibleForm = (Form)httpSession.getAttribute("form");

		if (possibleForm == null)
		{
			return null;
		}

		ctx.put("formmessage", httpSession.getAttribute("formmessage"));

		httpSession.removeAttribute("form");
		httpSession.removeAttribute("formmessage");

		return possibleForm;
	}

	protected RedirectionException getValidationException(Form form,
	                                                      Message message,
	                                                      String returnPath)
	{
		httpSession.setAttribute("formmessage", message);
		httpSession.setAttribute("form", form);
		log.debug("form validation failure: " + message.getMessage());

		return new RedirectionException(returnPath);
	}

	protected String[] toStringArray(Object[] in)
	{
		String[] result = new String[in.length];
		for(int i=0; i<in.length; i++)
		{
			result[i] = (String)in[i];
		}
		return result;
	}

	protected void setSessionMessage(String msg)
	{
		httpSession.setAttribute("message", new Message(msg));
	}
	
	protected void setSessionErrorMessage(String msg)
	{
		httpSession.setAttribute("message", new ErrorMessage(msg));
	}
	
	
	public String getCookie(String name)
	{
		if (request == null)
			return null;
		
		Cookie[] cookies = request.getCookies();
		
		if (cookies == null)
			return null;
		
		for (int i = 0; i < cookies.length; i++)
			if (cookies[i].getName().equals(name))
				return cookies[i].getValue();
		
		return null;
	}
	
	public boolean setCookie(String name, String value)
	{
		if (response == null)
			return false;
		
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(Helpers.getAppPrefix() + "/");
		cookie.setMaxAge(12 * 31 * 24 * 60 * 60);
		response.addCookie(cookie);
		return true;
	}
}
