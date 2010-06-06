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
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.*;


public class HibernateUtil
{
	private static SessionFactory sessionFactory;
	private static String currentSuffix = "";
	public static final ThreadLocal<Session> sessionHolder = new ThreadLocal<Session>();
	private static Logger log = Logger.getLogger(HibernateUtil.class);
	private static boolean isBuilding = false;

	public static Session currentSession() throws HibernateException
	{
		if (!isBuilding)
		{
			try
			{
				Session s = sessionHolder.get();

				// Open a new Session, if this Thread has none yet
				if ((s == null) || (s.isOpen() == false))
				{
					s = sessionFactory.openSession();
					sessionHolder.set(s);
				}

				return s;
			}
			catch (Exception ex)
			{
				log.fatal("Problem getting session");
				throw new HibernateException(ex);
			}
		}
		else
		{
			return null;
		}
	}

	public static void closeSession() throws HibernateException
	{
		try
		{
			Session s = sessionHolder.get();
			sessionHolder.set(null);

			// Close the session if one exists
			if (s != null)
			{
				s.clear();
				s.close();
			}
		}
		catch (Exception ex)
		{
			log.fatal("Problem closing session");
			throw new HibernateException(ex);
		}
	}

	public static void createFactory(String suffix) throws HibernateException
	{
		if (!suffix.equals(currentSuffix))
		{
			isBuilding = true;
			currentSuffix = suffix;
			HibernateUtil.closeSession();

			try
			{
				// recreate the SessionFactory
				Configuration config = getConfiguration(suffix);
				HibernateUtil.sessionFactory = config.buildSessionFactory();
			}
			catch (Throwable ex)
			{
				throw new ExceptionInInitializerError(ex);
			}

			isBuilding = false;
		}
	}

	public static Configuration getConfiguration(String suffix) throws IOException
	{
		Configuration config = new Configuration();
		config = config.configure();
		
		Properties appProperties = new Properties();
		java.net.URL url = Thread.currentThread().getContextClassLoader().getResource("app.properties");
		InputStream stream = url.openStream();
		appProperties.load(stream);

		//here we 'add in' the database name, user, password from the application properties file
		String connString = config.getProperty("hibernate.connection.url") + appProperties.getProperty("dbprefix") + suffix;
		config.setProperty("hibernate.connection.url", connString);
		config.setProperty("hibernate.connection.username", appProperties.getProperty("dbuser"));
		config.setProperty("hibernate.connection.password", appProperties.getProperty("dbpass"));
		return config;
	}
	
	public static boolean isFactoryInitialized(String suffix)
	{
		return currentSuffix.equals(suffix);
	}
	
	public static boolean isFactoryInitialized()
	{
		return !isFactoryInitialized("");
	}
}
