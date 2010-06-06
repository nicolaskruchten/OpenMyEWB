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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.net.SMTPAppender;
import org.apache.velocity.app.Velocity;
import org.hibernate.HibernateException;

import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.SMTPLogFilter;
import ca.myewb.frame.SessionListener;
import ca.myewb.frame.WarnLogFilter;


public class InitServlet extends HttpServlet
{
	public void init()
	{
		// Prepare for logging
		Logger log = Logger.getLogger(this.getClass());
		Logger rootLog = Logger.getLogger("ca.myewb");

		log.info("--------------");
		log.info("reloading app");
		Helpers.setLocalRoot(this.getServletContext().getRealPath("/"));
		SessionListener.doInit(getServletContext());
		
		Properties appProperties = new Properties();
	    try
		{
			java.net.URL url = Thread.currentThread().getContextClassLoader().getResource("app.properties");
			InputStream stream = url.openStream();
			appProperties.load(stream);
		}
		catch (IOException e1)
		{
			log.error("could not load app.properties!");
		}
		
		Helpers.setPrefixes(appProperties.getProperty("appprefix"),
				appProperties.getProperty("defaulturl"),
				appProperties.getProperty("domain"));
		Helpers.setEnShortName(appProperties.getProperty("enshortname"));
		Helpers.setFrShortName(appProperties.getProperty("frshortname"));
		Helpers.setLongName(appProperties.getProperty("longname"));
		Helpers.setSystemEmail(appProperties.getProperty("systememail"));
		Helpers.setDevMode(appProperties.getProperty("devmode").equals("yes"));

		String dbSuffix = appProperties.getProperty("dbsuffix");

		try
		{
			HibernateUtil.createFactory(dbSuffix);
			log.debug("hibernate sessionFactory created (using " + dbSuffix
			          + " db)");
		}
		catch (HibernateException e)
		{
			log.fatal("Could not create hibernate session Factory!");
		}

		if (appProperties.getProperty("txtlog").equals("yes"))
		{
			try
			{
				rootLog.addAppender(new FileAppender(new SimpleLayout(),
				                                     this.getServletConfig()
				                                     .getServletContext()
				                                     .getRealPath("logs/app.log.txt"),
				                                     true));
				log.debug("txt logging enabled");
			}
			catch (IOException e)
			{
				log.fatal("Could not create text Appender!");
			}
		}
		else
		{
			log.debug("txt logging disabled");
		}

		if (appProperties.getProperty("smtplog").equals("yes"))
		{
			try
			{
				SMTPAppender smtp = new SMTPAppender();
				smtp.setFrom(Helpers.getSystemEmail());
				smtp.setTo(appProperties.getProperty("systememail"));
				smtp.setSMTPHost(appProperties.getProperty("smtphost"));
				smtp.setSubject(Helpers.getLongName() + " error report");
				smtp.setLayout(new PatternLayout("%d{ISO8601} - %m%n"));
				smtp.activateOptions();
				smtp.setBufferSize(50);
				smtp.addFilter(new SMTPLogFilter());
				rootLog.addAppender(smtp);

				log.debug("smtp error logging enabled");
			}
			catch (Exception e)
			{
				log.fatal("Could not create smtp Appender!");
			}

			try
			{
				FileAppender warnFA = new FileAppender(new PatternLayout("%n%d{ISO8601} - %m%n"),
				                                       this.getServletConfig()
				                                       .getServletContext()
				                                       .getRealPath("logs/warnings.log.txt"),
				                                       true);
				warnFA.setThreshold(Level.WARN);
				warnFA.addFilter(new WarnLogFilter());
				rootLog.addAppender(warnFA);
				log.debug("warning text logging enabled as per smtp log settings");
			}
			catch (IOException e)
			{
				log.fatal("Could not create text Appender!");
			}
		}
		else
		{
			log.debug("smtp logging disabled");
		}


		try
		{		
			Properties props = new Properties();
			props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
			props.setProperty("runtime.log.logsystem.log4j.category", "ca.myewb.velocity"); 
			props.setProperty("directive.foreach.counter.initial.value", "0");
			props.setProperty("resource.manager.logwhenfound", "false");
			props.setProperty("file.resource.loader.path", this.getServletContext().getRealPath("/tmpl/"));
			props.setProperty("file.resource.loader.cache", "true");
			props.setProperty("file.resource.loader.modificationCheckInterval", "5");

			props.setProperty(Velocity.RUNTIME_LOG, this.getServletContext().getRealPath("logs/velocity.log.txt"));
			Velocity.init(props);
			log.debug("velocity initialized");
		}
		catch (Exception e)
		{
			log.fatal("Could not initialize Velocity!");
		}


		log.debug("system initialized!");
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
	{
	}
}
