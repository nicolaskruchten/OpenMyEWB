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
import java.util.List;
import java.util.Vector;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Helpers;
import ca.myewb.logic.EmailLogic;


public class EmailModel extends EmailLogic
{
	EmailModel()  {
		super();
	}
	
	public EmailModel(List<String> to, String sender, String shortName, String subject, String textBody, String HTMLBody) throws Exception {
		super(to, sender, shortName, subject, textBody, HTMLBody);
	}
	
	
	//default sender, single recipient, infer subject, single stream
	public static void sendEmail(String to, String body)
	                 throws Exception
	{
		sendEmail(Helpers.getSystemEmail(), to, body);
	}

	//specified sender, single recipient, infer subject, single stream
	public static void sendEmail(String from, String to, String body)
	                 throws Exception
	{
		Vector<String> toList = new Vector<String>();
		toList.add(to);
		sendEmail(from, toList, body);
	}

	//specified sender, list of recipient, infer subject, single stream
	public static void sendEmail(String from, List<String> to, String body)
	                 throws Exception
	{
		int idx = body.indexOf('\n');
		String subject = body.substring(0, idx);
		String realBody = body.substring(body.indexOf('\n', idx + 1) + 1);
		
		sendEmail(from, to, subject, realBody, Helpers.getEnShortName());
	}

	//specified sender, list of recipient, specified subject, single stream
	public static void sendEmail(String from, List<String> to, 
			String subject, String body, String shortname) throws Exception
	{
		sendEmail(from, to, subject, body, 
				"<p class=\"postbody\">" + Helpers.wikiFormat(body) + "</p>", //sorry, ugly hack to HTML here
				shortname, false);
	}

	//specified sender, list of recipient, specified subject, text+html streams
	public static void sendEmail(String from, List<String> to, 
			String subject, String textBody, String htmlBody, String shortname, boolean showUnsubscribe) 
	throws Exception
	{
		EmailModel e = new EmailModel(to, from, shortname, subject, 
				EmailModel.doTemplateMerge(textBody, subject, "emails/wrapper.txt.vm",  showUnsubscribe), 
				EmailModel.doTemplateMerge(htmlBody, subject, "emails/wrapper.html.vm", showUnsubscribe)
				);
		HibernateUtil.currentSession().save(e);
	}

	private static String doTemplateMerge(String content, String subject, String tmplPath, 
			boolean showUnsubscribe) throws Exception
	{
		Template template = Velocity.getTemplate(tmplPath);
		VelocityContext ctx = new VelocityContext();
		ctx.put("helpers", new Helpers());
		ctx.put("content", content);
		ctx.put("subject", subject);
		ctx.put("showUnsubscribe", showUnsubscribe);
		
		StringWriter writer = new StringWriter();
		template.merge(ctx, writer);
	
		return writer.toString();
	}
	
}
