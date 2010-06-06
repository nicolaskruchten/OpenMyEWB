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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;

import ca.myewb.model.ApplicationSessionModel;
import ca.myewb.model.DailyStatsModel;
import ca.myewb.model.EmailModel;
import ca.myewb.model.PlacementModel;
import ca.myewb.model.UserModel;


public class Cron
{
	static SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d");

	public static void main(String[] args) throws IOException
	{
		Logger log = Logger.getLogger(Cron.class.getName());
		log.addAppender(new ConsoleAppender(new SimpleLayout()));

		if (args.length < 2)
		{
			log.error("need dbsuffix and run time mode as arguments!");
			System.exit(0);
		}

		String dbSuffix = args[0];
		String time = args[1];
		boolean safemode = ((args.length > 2) && (args[2].equals("-safe")));

		if (safemode)
		{
			log.info("cron job started in SAFEMODE, no changes will be persisted.");
		}


		Properties appProperties = new Properties();
		java.net.URL url = Thread.currentThread().getContextClassLoader().getResource("app.properties");
		InputStream stream = url.openStream();
		appProperties.load(stream);
		
		Helpers.setPrefixes(appProperties.getProperty("appprefix"),
				appProperties.getProperty("defaulturl"),
				appProperties.getProperty("domain"));
		Helpers.setEnShortName(appProperties.getProperty("enshortname"));
		Helpers.setFrShortName(appProperties.getProperty("frshortname"));
		Helpers.setLongName(appProperties.getProperty("longname"));
		Helpers.setSystemEmail(appProperties.getProperty("systememail"));
		Helpers.setDevMode(appProperties.getProperty("devmode").equals("yes"));
		
		// Prepare Hibernate
		Session session = null;
		Transaction tx = null;

		try
		{
			// Set up the database connection
			HibernateUtil.createFactory(dbSuffix);
			session = HibernateUtil.currentSession();
			tx = session.beginTransaction();

			Properties props = new Properties();
			props.setProperty("directive.foreach.counter.initial.value", "0");
			props.setProperty("file.resource.loader.path", "tmpl");		
			props.setProperty(Velocity.RUNTIME_LOG, "logs/velocity.log.txt");

			Velocity.init(props);
		}
		catch (Exception e)
		{
			log.error("Error initializing Hibernate or Velocity!  Bailing!", e);
			System.exit(1);
		}

		try
		{
			if(time.equals("daily"))
			{	
				daily(log, session);
			}
			else if(time.equals("halfhourly"))
			{
				halfHourly(log, session);
			}
			
			if (safemode)
			{
				log.info("SAFEMODE: rolling back transaction.");
				tx.rollback();
				log.info("transaction rolled back.");
			}
			else
			{
				tx.commit();
				log.info("transaction committed.");
			}
		}
		catch (Exception e)
		{
			tx.rollback();
			HibernateUtil.closeSession();
			log.error("Exception while running cron script: tx rolled back!", e);
			System.exit(1);
		}

		HibernateUtil.closeSession();
	}

	private static void halfHourly(Logger log, Session session) throws Exception 
	{
		createSessionEndEmails(log, session);	
	}

	private static void daily(Logger log, Session session) throws Exception {
		doTwoWeekWarnings(log, session);

		doOneWeekWarnings(log, session);

		doOneDayWarnings(log, session);

		doMembershipExpiry(log, session);

		doDailystatsBuffer(log, session);

		
		doNMTandLTOVmemberships(log, session);
	}
	
	private static void doNMTandLTOVmemberships(Logger log, Session session) throws HibernateException, Exception
	{
		log.info("Getting list of active placements...");
		Vector<PlacementModel> OVs = new Vector<PlacementModel>(PlacementModel.getActivePlacements());
		
		log.info("Getting list of NMT's...");
		List<UserModel> nmts = (new SafeHibList<UserModel>(session.createQuery("SELECT u FROM UserModel u, RoleModel r "
                + "WHERE r.user=u AND r.group=? AND r.end IS NULL")
                .setEntity(0, Helpers.getGroup("NMT")))).list();
		log.info("Getting list of Admins...");
		List<UserModel> admins = (new SafeHibList<UserModel>(session.createQuery("SELECT u FROM UserModel u, RoleModel r "
				+ "WHERE r.user=u AND r.group=? AND r.end IS NULL")
				.setEntity(0, Helpers.getGroup("Admin")))).list();
		
		
		Vector<UserModel> autoUpgrades = new Vector<UserModel>(nmts);
		autoUpgrades.addAll(admins);
		
		// Limit to long terms
		for(PlacementModel p : OVs)
		{
			if( p.isLongterm() )
			{
				autoUpgrades.add(p.getOv());
			}
		}
		
		log.info("Upgrading Users...");
		for( UserModel u : autoUpgrades )
		{
			if( u.getExpiry() == null || u.canRenew() )
			{
				log.info(u.getFirstname() + " " + u.getLastname() + "'s membership is being " + (u.getExpiry() == null ? "upgraded" : "renewed") + " automatically.");
				u.renew(null, false);
			}

		}
		
	}



	private static void doDailystatsBuffer(Logger log, Session session)
	{
		log.info("----- adding rows to dailystatsbuffer");

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, 35);

		for (int i = 0; i < 35; i++)
		{
			List list = session.createQuery("from DailyStatsModel as ds where ds.day=?")
			            .setDate(0, cal.getTime()).list();

			if (list.isEmpty())
			{
				log.info("added row for " + cal.getTime().toString());

				DailyStatsModel.newDailyStats(cal.getTime());
				cal.add(Calendar.DAY_OF_YEAR, -1);
			}
		}
	}

	private static void doTwoWeekWarnings(Logger log, Session session)
	                               throws Exception
	{
		// 2-week warning
		log.info("----- 14-day warning");

		Calendar calendar = Calendar.getInstance();
		Criteria crit = session.createCriteria(UserModel.class);
		crit.add(Restrictions.isNotNull("email"));
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		calendar.add(Calendar.DAY_OF_YEAR, 14);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		crit.add(Restrictions.eq("expiry", calendar.getTime()));

		Iterator it = crit.list().iterator();

		while (it.hasNext())
		{
			UserModel u = (UserModel)it.next();
			log.info(u.getFirstname() + " " + u.getLastname() + ": "
			         + u.getEmail());

			VelocityContext mailCtx = new VelocityContext();
			mailCtx.put("helpers", new Helpers());
			mailCtx.put("name", u.getFirstname());
			mailCtx.put("numdays", "14 days");
			mailCtx.put("expiry", formatter.format(calendar.getTime()));

			Template template = Velocity.getTemplate("emails/expirywarning.vm");
			StringWriter writer = new StringWriter();
			template.merge(mailCtx, writer);

			EmailModel.sendEmail(u.getEmail(), writer.toString());
		}
	}

	private static void doOneWeekWarnings(Logger log, Session session)
	                               throws Exception
	{
		// 1-week warning
		log.info("----- 7-day warning");

		Calendar calendar = Calendar.getInstance();
		Criteria crit = session.createCriteria(UserModel.class);
		crit.add(Restrictions.isNotNull("email"));
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		calendar.add(Calendar.DAY_OF_YEAR, 7);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		crit.add(Restrictions.eq("expiry", calendar.getTime()));

		Iterator it = crit.list().iterator();

		while (it.hasNext())
		{
			UserModel u = (UserModel)it.next();
			log.info(u.getFirstname() + " " + u.getLastname() + ": "
			         + u.getEmail());

			VelocityContext mailCtx = new VelocityContext();
			mailCtx.put("helpers", new Helpers());
			mailCtx.put("name", u.getFirstname());
			mailCtx.put("numdays", "7 days");
			mailCtx.put("expiry", formatter.format(calendar.getTime()));

			Template template = Velocity.getTemplate("emails/expirywarning.vm");
			StringWriter writer = new StringWriter();
			template.merge(mailCtx, writer);

			EmailModel.sendEmail(u.getEmail(), writer.toString());
		}
	}

	private static void doOneDayWarnings(Logger log, Session session)
	                              throws Exception
	{
		// 1-day warning
		log.info("----- 1-day warning");

		Calendar calendar = Calendar.getInstance();
		Criteria crit = session.createCriteria(UserModel.class);
		crit.add(Restrictions.isNotNull("email"));
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		crit.add(Restrictions.eq("expiry", calendar.getTime()));

		Iterator it = crit.list().iterator();

		while (it.hasNext())
		{
			UserModel u = (UserModel)it.next();
			log.info(u.getFirstname() + " " + u.getLastname() + ": "
			         + u.getEmail());

			VelocityContext mailCtx = new VelocityContext();
			mailCtx.put("helpers", new Helpers());
			mailCtx.put("name", u.getFirstname());
			mailCtx.put("numdays", "1 day");
			mailCtx.put("expiry", formatter.format(calendar.getTime()));

			Template template = Velocity.getTemplate("emails/expirywarning.vm");
			StringWriter writer = new StringWriter();
			template.merge(mailCtx, writer);

			EmailModel.sendEmail(u.getEmail(), writer.toString());
		}
	}

	private static void doMembershipExpiry(Logger log, Session session)
	                                throws Exception
	{
		// expiry
		log.info("----- Expiry");

		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);

		Criteria crit = session.createCriteria(UserModel.class);
		crit.add(Restrictions.isNotNull("email"));
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.le("expiry", calendar.getTime()));

		Iterator it = crit.list().iterator();

		while (it.hasNext())
		{
			UserModel u = (UserModel)it.next();
			log.info(u.getFirstname() + " " + u.getLastname() + ": "
			         + u.getEmail());

			u.expire();

			VelocityContext mailCtx = new VelocityContext();
			mailCtx.put("helpers", new Helpers());
			mailCtx.put("name", u.getFirstname());

			Template template = Velocity.getTemplate("emails/expiry.vm");
			StringWriter writer = new StringWriter();
			template.merge(mailCtx, writer);

			EmailModel.sendEmail(u.getEmail(), writer.toString());
		}
	}
	
	private static void createSessionEndEmails(Logger log, Session session) throws Exception
	{
		List<ApplicationSessionModel> sessions = new SafeHibList<ApplicationSessionModel>(
				session.createCriteria(ApplicationSessionModel.class)
				.add(Restrictions.le("closeDate", new Date()))
				.add(Restrictions.eq("emailSent", false))
				.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
				).list();
		for (Iterator iter = sessions.iterator(); iter.hasNext();)
		{
			ApplicationSessionModel s = (ApplicationSessionModel) iter.next();
			
					
			EmailModel.sendEmail(Helpers.getSystemEmail(),
					s.getApplicantEmails(true), 
					"[" + Helpers.getEnShortName() + "-applications] Application Session " + s.getName() + " has closed", 
					s.getCloseEmailText(), 
					Helpers.getEnShortName() + "-applications");
			
			s.croned();
			
			log.info("Created close session email for " + s.getName());
		}
	}
}
