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

import java.awt.BasicStroke;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardPieItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DataUtilities;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.SortOrder;

import ca.myewb.frame.ErrorMessage;
import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.Permissions;
import ca.myewb.frame.RedirectionException;
import ca.myewb.frame.SafeHibList;
import ca.myewb.model.DailyStatsModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.UserModel;


public class GraphServlet extends HttpServlet
{
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	           throws ServletException, IOException
	{
		Logger log = Logger.getLogger(this.getClass());

		try
		{
			Session s = HibernateUtil.currentSession();
			HttpSession httpSession = req.getSession();
			UserModel currentUser = WrapperServlet.getUser(Helpers.getDefaultURL(), log, s, httpSession);

			String url = req.getRequestURI();

			if (!currentUser.isMember(Helpers.getGroup("Exec")))
			{
				log.warn(currentUser.getUsername() + "tried to access GraphServlet!");
				httpSession.setAttribute("message", new ErrorMessage("You can't access the graphs!"));
				throw new RedirectionException(Helpers.getDefaultURL());
			}

			JFreeChart chart = null;

			if (url.contains("genderpie"))
			{
				chart = getGenderPie(s);
			}
			else if (url.contains("languagepie"))
			{
				chart = getLanguagePie(s);
			}
			else if (url.contains("studentpie"))
			{
				chart = getStudentPie(s);
			}
			else if (url.contains("chapterrankpie"))
			{
				chart = getChapterRankPie(s, url);
			}
			else if (url.contains("rankpie"))
			{
				chart = getRankPie();
			}
			else if (url.contains("nochapterpie"))
			{
				chart = getNoChapterPie();
			}
			else if (url.contains("chapterpie"))
			{
				chart = getChapterPie(s);
			}
			else if (url.contains("postpie"))
			{
				chart = getPostPie(s);
			}
			else if (url.contains("post2pie"))
			{
				chart = getPost2Pie(s);
			}
			else if (url.contains("post3pie"))
			{
				chart = getPost3Pie(s);
			}
			else if (url.contains("provincepie"))
			{
				chart = getProvincePie(s);
			}
			else if (url.contains("lastlogin"))
			{
				chart = getLastLogin(s);
			}
			else if (url.contains("daily4stats"))
			{
				chart = getDailyStats(s);
			}
			else if (url.contains("daily2stats"))
			{
				chart = getDaily2Stats(s);
			}
			else if (url.contains("daily3stats"))
			{
				chart = getDaily3Stats(s);
			}
			else if (url.contains("dailyintegratedstats"))
			{
				chart = getDailyIntegratedStats(s);
			}
			else if (url.contains("logins"))
			{
				chart = getLogins(s);
			}
			else if (url.contains("dailynewstats"))
			{
				chart = getNewDailyStats(s);
			}
			else if (url.contains("birthyears"))
			{
				chart = getBirthyears(s);
			}
			else if (url.contains("listmemberships") || url.contains("chaptermemberships"))
			{
				chart = getListMemberships(s,
						Integer.parseInt(url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'))),
						currentUser
				);
			}
			

			res.setContentType("image/png");

			OutputStream out = res.getOutputStream();
			
			if (url.contains("listmemberships"))
			{
				ChartUtilities.writeChartAsPNG(out, chart, 700, 500);
			}
			else
			{
				ChartUtilities.writeChartAsPNG(out, chart, 800, 600);
			}
		}
		catch (RedirectionException re)
		{
			log.info("Clean redirect: " + re.getTargetURL());
			res.sendRedirect(re.getTargetURL());
		}
		catch (Exception e)
		{
			log.error("graph servlet error", e);
			res.sendError(500, e.toString());
		}
	}

	private static Date getStartDate()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -14);
		return cal.getTime();
	}
	
	private JFreeChart getDailyIntegratedStats(Session s) throws CloneNotSupportedException
	{	
		JFreeChart chart;
		List<DailyStatsModel> stats = (new SafeHibList<DailyStatsModel>(
				s.createQuery("select ds from DailyStatsModel as ds where day<? and day>=? order by day desc")
		                      .setDate(0,new Date()).setDate(1, GraphServlet.getStartDate()))).list();
		TimeSeriesCollection theData = new TimeSeriesCollection();
		TimeSeriesCollection theData2 = new TimeSeriesCollection();
	
		TimeSeries users = new TimeSeries("Total Users",Day.class);
		theData.addSeries(users);
	
		TimeSeries regulars = new TimeSeries("Regular Members",Day.class);
		theData2.addSeries(regulars);
	
		int numUsers = Helpers.getGroup("Org").getNumMembers();
		int numReg = Helpers.getGroup("Regular").getNumMembers();
	
		for (DailyStatsModel ds : stats)
		{
			Day theDay = new Day(ds.getDay());
			users.add(theDay, numUsers);
			regulars.add(theDay, numReg);
			numUsers -= (ds.getMailinglistsignups() + ds.getSignups() - ds.getDeletions());
			numReg -= (ds.getRegupgrades() - ds.getRegdowngrades());
		}
	
		chart = ChartFactory.createTimeSeriesChart("Membership",
		                                           "Day", "Users",
		                                           theData, true, true,
		                                           true);
	
		XYPlot plot = (XYPlot)chart.getPlot();
		
		NumberAxis axis2=new NumberAxis("Regular Members");
		plot.setRangeAxis(1,axis2);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		plot.setDataset(1, MovingAverage.createMovingAverage(theData2, "", 14, 0));
		plot.mapDatasetToRangeAxis(1,1);
		
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(0);
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer = (XYLineAndShapeRenderer) renderer.clone();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		plot.setRenderer(1, renderer);
		return chart;
	}

	
	private JFreeChart getBirthyears(Session s)
	{
		JFreeChart chart;
		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		
		List logins = s.createQuery("select count(*), birth from UserModel where birth!=0 group by birth order by birth").list();
		XYSeries theData = new XYSeries("Ages");
		seriesCollection.addSeries(theData);
		
		for(int i=0; i<logins.size(); i++)
		{
			Object[] tuple = (Object[])logins.get(i);
			theData.add(Calendar.getInstance().get(Calendar.YEAR) - (Integer)tuple[1], (Long)tuple[0]);
		}
		
		logins = s.createQuery("select count(*), birth from UserModel where birth!=0 and gender='f' group by birth order by birth").list();
		XYSeries theData2 = new XYSeries("Female");
		seriesCollection.addSeries(theData2);
		
		for(int i=0; i<logins.size(); i++)
		{
			Object[] tuple = (Object[])logins.get(i);
			theData2.add(Calendar.getInstance().get(Calendar.YEAR) - (Integer)tuple[1], (Long)tuple[0]);
		}
		
		logins = s.createQuery("select count(*), birth from UserModel where birth!=0 and gender='m' group by birth order by birth").list();
		XYSeries theData3 = new XYSeries("Male");
		seriesCollection.addSeries(theData3);
		
		for(int i=0; i<logins.size(); i++)
		{
			Object[] tuple = (Object[])logins.get(i);
			theData3.add(Calendar.getInstance().get(Calendar.YEAR) - (Integer)tuple[1], (Long)tuple[0]);
		}

		chart = ChartFactory.createXYLineChart("Age Distribution",
		                                           "Age", "Number of Users",
		                                           seriesCollection,
		                                           PlotOrientation.VERTICAL,
		                                           true, true, true);

		XYPlot plot = (XYPlot)chart.getPlot();
		plot.getDomainAxis().setUpperBound(80);
		plot.getDomainAxis().setLowerBound(15);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		return chart;
	}

	private JFreeChart getLogins(Session s)
	{
		JFreeChart chart;

		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		XYSeries theData = new XYSeries("Logins");
		seriesCollection.addSeries(theData);
		List logins = s.createQuery("select count(*), logins from UserModel where logins!=0 and logins<=100 group by logins order by logins desc").list();		
		Integer numLogins = ((Long)s.createQuery("select count(*) from UserModel where logins>100").uniqueResult()).intValue();

		for(int i=0; i<logins.size(); i++)
		{
			Object[] tuple = (Object[])logins.get(i);
			numLogins += ((Long)tuple[0]).intValue();
			theData.add((Integer)tuple[1], numLogins);
		}
		
		XYSeries theData3 = new XYSeries("Female");
		seriesCollection.addSeries(theData3);
		logins = s.createQuery("select count(*), logins from UserModel where gender='f' and logins!=0 and logins<=100 group by logins order by logins desc").list();		
		numLogins = ((Long)s.createQuery("select count(*) from UserModel where gender='f' and logins>100").uniqueResult()).intValue();

		for(int i=0; i<logins.size(); i++)
		{
			Object[] tuple = (Object[])logins.get(i);
			numLogins += ((Long)tuple[0]).intValue();
			theData3.add((Integer)tuple[1], numLogins);
		}
		
		XYSeries theData2 = new XYSeries("Male");
		seriesCollection.addSeries(theData2);
		logins = s.createQuery("select count(*), logins from UserModel where gender='m' and logins!=0 and logins<=100 group by logins order by logins desc").list();		
		numLogins = ((Long)s.createQuery("select count(*) from UserModel where gender='m' and logins>100").uniqueResult()).intValue();

		for(int i=0; i<logins.size(); i++)
		{
			Object[] tuple = (Object[])logins.get(i);
			numLogins += ((Long)tuple[0]).intValue();
			theData2.add((Integer)tuple[1], numLogins);
		}
		

		chart = ChartFactory.createXYLineChart("Logins Distribution",
		                                           "Number of Logins", "Number of Users",
		                                           seriesCollection,
		                                           PlotOrientation.VERTICAL,
		                                           true, true, true);

		XYPlot plot = (XYPlot)chart.getPlot();
		plot.getDomainAxis().setUpperBound(100);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		return chart;
	}
	
	private JFreeChart getLastLogin(Session s) throws CloneNotSupportedException
	{
		
		Integer numCurrentLogins = ((Long)s.createQuery("select count(*) from UserModel " +
				"where currentLogin is not null and currentLogin >= :date")
				.setDate("date", getStartDate())
				.uniqueResult()).intValue();
		
		List currentStats = s.createSQLQuery("SELECT DATE(currentLogin) as date, count( * ) as lastLogins " +
				"FROM users where currentLogin is not null  and currentLogin >= :date " +
				"GROUP BY DATE( currentLogin )")
				.addScalar("date", Hibernate.DATE)
				.addScalar("lastLogins", Hibernate.INTEGER)
				.setDate("date", getStartDate())
				.list();
		
		TimeSeriesCollection theData = new TimeSeriesCollection();
		TimeSeriesCollection theData2 = new TimeSeriesCollection();

		TimeSeries current = new TimeSeries("Num Latest Sign-ins",Day.class);
		theData.addSeries(current);
		TimeSeries current2 = new TimeSeries("Signed-in Users Since",Day.class);
		theData2.addSeries(current2);

		
		for (Object ds : currentStats)
		{
			Date date = (Date) ((Object[])ds)[0];
			Day day = new Day(date);
			Integer integer = (Integer)((Object[])ds)[1];
			current.add(day, integer);
			numCurrentLogins -= integer.intValue();
			current2.add(day, numCurrentLogins);
			
		}

		
		JFreeChart chart = ChartFactory.createTimeSeriesChart("Sign-in Recency",
													"Day", "Sign-ins", 
		                                           theData,
		                                           true, true,
		                                           true);

		XYPlot plot = (XYPlot)chart.getPlot();
		
		NumberAxis axis2=new NumberAxis("Users");
		plot.setRangeAxis(1,axis2);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		plot.setDataset(1, theData2);
		plot.mapDatasetToRangeAxis(1,1);
		
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer(0);
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(2.0f));
		renderer = (XYLineAndShapeRenderer) renderer.clone();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(2.0f));
		plot.setRenderer(1, renderer);
		return chart;
	}
	
	private JFreeChart getDaily3Stats(Session s)
	{
		JFreeChart chart;
		List<DailyStatsModel> stats = (new SafeHibList<DailyStatsModel>(
				s.createQuery("select ds from DailyStatsModel as ds where day<? and day>=? order by day desc")
		                      .setDate(0,new Date()).setDate(1, GraphServlet.getStartDate()))).list();
		TimeSeriesCollection theData = new TimeSeriesCollection();

		TimeSeries regUpgrades = new TimeSeries("Regular Upgrades",Day.class);
		theData.addSeries(regUpgrades);

		TimeSeries renewals = new TimeSeries("Regular Renewals",Day.class);
		theData.addSeries(renewals);

		TimeSeries regDowngrades = new TimeSeries("Regular Downgrades",Day.class);
		theData.addSeries(regDowngrades);


		for (DailyStatsModel ds : stats)
		{
			Day theDay = new Day(ds.getDay());
			regUpgrades.add(theDay, ds.getRegupgrades());
			renewals.add(theDay, ds.getRenewals());
			regDowngrades.add(theDay, ds.getRegdowngrades());
		}

		chart = ChartFactory.createTimeSeriesChart("Regular membership changes (14-day moving avg)",
		                                           "Day", "Occurrences",
		                                           MovingAverage.createMovingAverage(theData, "", 14, 0) , true, true,
		                                           true);

		XYPlot plot = (XYPlot)chart.getPlot();
		plot.getRangeAxis().setUpperBound(10);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot
		                                                                                                                                                                                                                                                                                                                                                                             .getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(2.0f));
		renderer.setSeriesStroke(2, new BasicStroke(2.0f));
		return chart;
	}

	private JFreeChart getDaily2Stats(Session s)
	{
		JFreeChart chart;
		List<DailyStatsModel> stats = (new SafeHibList<DailyStatsModel>(
				s.createQuery("select ds from DailyStatsModel as ds where day<? and day>=? order by day desc")
		                      .setDate(0,new Date()).setDate(1, GraphServlet.getStartDate()))).list();
		TimeSeriesCollection theData = new TimeSeriesCollection();

		TimeSeries signups = new TimeSeries("signups", Day.class);
		theData.addSeries(signups);

		TimeSeries mlSignups = new TimeSeries("Mailing list signups", Day.class);
		theData.addSeries(mlSignups);

		TimeSeries mlUpgrades = new TimeSeries("Mailing list upgrades", Day.class);
		theData.addSeries(mlUpgrades);

		TimeSeries deletions = new TimeSeries("Deletions", Day.class);
		theData.addSeries(deletions);

		for (DailyStatsModel ds : stats)
		{
			Day theDay = new Day(ds.getDay());
			signups.add(theDay, ds.getSignups());
			deletions.add(theDay, ds.getDeletions());
			mlSignups.add(theDay, ds.getMailinglistsignups());
			mlUpgrades.add(theDay, ds.getMailinglistupgrades());
		}

		chart = ChartFactory.createTimeSeriesChart("Account changes (14-day moving avg)",
		                                           "Day", "Occurrences",
		                                           MovingAverage.createMovingAverage(theData, "", 14, 0), true, true,
		                                           true);

		XYPlot plot = (XYPlot)chart.getPlot();
		plot.getRangeAxis().setUpperBound(25);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot
		                                                                                                                                                                                                                                                                                                                         .getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(2.0f));
		renderer.setSeriesStroke(2, new BasicStroke(2.0f));
		renderer.setSeriesStroke(3, new BasicStroke(2.0f));
		return chart;
	}

	private JFreeChart getDailyStats(Session s)
	{
		JFreeChart chart;
		List<DailyStatsModel> stats = (new SafeHibList<DailyStatsModel>(
				s.createQuery("select ds from DailyStatsModel as ds where day<? and day>=? order by day desc")
		                      .setDate(0,new Date()).setDate(1, GraphServlet.getStartDate()))).list();
		TimeSeriesCollection theData = new TimeSeriesCollection();

		TimeSeries signins = new TimeSeries("Raw Signins", Day.class);
		TimeSeries posts = new TimeSeries("Posts", Day.class);
		TimeSeries replies = new TimeSeries("Replies", Day.class);
		TimeSeries whiteboards = new TimeSeries("Whiteboards", Day.class);
		TimeSeries files = new TimeSeries("Files", Day.class);

		for (DailyStatsModel ds : stats)
		{
			Day theDay = new Day(ds.getDay());
			signins.add(theDay, ds.getSignins());
			posts.add(theDay, ds.getPosts());
			replies.add(theDay, ds.getReplies());
			whiteboards.add(theDay, ds.getWhiteboardEdits());
			files.add(theDay, ds.getFilesadded());
		}

		String title = "Usage (14-day moving avg)";
		theData.addSeries(MovingAverage.createMovingAverage(signins, "Signins", 14, 0));
		theData.addSeries(MovingAverage.createMovingAverage(posts, "Posts", 14, 0));
		theData.addSeries(MovingAverage.createMovingAverage(replies, "Replies", 14, 0));
		theData.addSeries(signins);
		theData.addSeries(MovingAverage.createMovingAverage(whiteboards, "Whiteboard Edits", 14, 0));
		theData.addSeries(MovingAverage.createMovingAverage(files, "Files Uploaded", 14, 0));
		
		chart = ChartFactory.createTimeSeriesChart(title,
		                                           "Day", "Occurrences",
		                                           theData, true, true,
		                                           true);

		XYPlot plot = (XYPlot)chart.getPlot();
		plot.getRangeAxis().setUpperBound(400);
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(2.0f));
		renderer.setSeriesStroke(2, new BasicStroke(2.0f));
		renderer.setSeriesStroke(3, new BasicStroke(2.0f));
		renderer.setSeriesStroke(4, new BasicStroke(2.0f));
		renderer.setSeriesStroke(5, new BasicStroke(2.0f));
		return chart;
	}
	
	private JFreeChart getNewDailyStats(Session s)
	{
		JFreeChart chart;
		List<DailyStatsModel> stats = (new SafeHibList<DailyStatsModel>(
				s.createQuery("select ds from DailyStatsModel as ds where day<? and day>=? order by day desc")
		                      .setDate(0,new Date()).setDate(1, GraphServlet.getStartDate()))).list();
		TimeSeriesCollection theData = new TimeSeriesCollection();

		TimeSeries events = new TimeSeries("Events Created", Day.class);
		TimeSeries mailings = new TimeSeries("Event Mailings", Day.class);

		for (DailyStatsModel ds : stats)
		{
			Day theDay = new Day(ds.getDay());
			events.add(theDay, ds.getEvents());
			mailings.add(theDay, ds.getEventMailings());
		}

		String title = "Usage";
		theData.addSeries(MovingAverage.createMovingAverage(events, "Events Created", 14, 0));
		theData.addSeries(MovingAverage.createMovingAverage(mailings, "Event Mailings", 14, 0));
		
		
		chart = ChartFactory.createTimeSeriesChart(title,
		                                           "Day", "Occurrences",
		                                           theData, true, true,
		                                           true);

		XYPlot plot = (XYPlot)chart.getPlot();
		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		renderer.setSeriesStroke(1, new BasicStroke(2.0f));
		return chart;
	}

	private JFreeChart getProvincePie(Session s)
	{
		JFreeChart chart;
		String[] provinces = 
		                     {
		                         "PE", "YT", "NT", "NU", "ON", 
		                         "QC", "AB", "BC", "NL", "MB", "NB", "NS",
		                         "SK"
		                     };
		DefaultPieDataset ds = new DefaultPieDataset();
		String query = "select count(*) from UserModel as u where u.province=?";

		int total = 0;
		for (String province : provinces)
		{
			Integer integer = ((Long)s.createQuery(query)
			                     .setString(0, province)
			                     .list().get(0)).intValue();
			total += integer;
			ds.setValue(province,
			            integer);
		}

		chart = ChartFactory.createPieChart("Province Breakdown (for " + total + " known addresses)",
		                                    ds, false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}
	

	private JFreeChart getPostPie(Session s)
	{
		JFreeChart chart;
		DefaultPieDataset ds = new DefaultPieDataset();


		int numPosts = 0;
		int numTypePosts = 0;
		
		String query;
		
		
		query = "select count(*) from PostModel as p where p.poster.gender='m' and p.emailed=true and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();;
		ds.setValue("Emails by Males", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.poster.gender='m' and " +
				"p.parent is null and p.emailed=false and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();;
		ds.setValue("Posts by Males", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.poster.gender='m' and " +
				"p.parent is not null and p.emailed=false and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();;
		ds.setValue("Replies by Males", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.poster.gender!='f' and p.poster.gender!='m' and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();;
		ds.setValue("All Posts by Unknown Gender", numTypePosts);
		numPosts += numTypePosts;

		query = "select count(*) from PostModel as p where p.poster.gender='f' and " +
				"p.parent is not null and p.emailed=false and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();;
		ds.setValue("Replies by Females", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.poster.gender='f' and " +
				"p.parent is null and p.emailed=false and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();;
		ds.setValue("Posts by Females", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.poster.gender='f' and p.emailed=true and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();;
		ds.setValue("Emails by Females", numTypePosts);
		numPosts += numTypePosts;

		chart = ChartFactory.createPieChart("Post Type/Author Gender Breakdown (for "
		                                    + numPosts
		                                    + " posts)", ds,
		                                    false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}

	private JFreeChart getPost2Pie(Session s)
	{
		JFreeChart chart;
		DefaultPieDataset ds = new DefaultPieDataset();


		int numPosts = 0;
		int numTypePosts = 0;
		
		String query = "select count(*) from PostModel as p where p.group.id=1 and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("Org List", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.group.admin=false and p.group.postName not like 'anyone in the%' and " +
				"p.group.public=true and p.group.parent is null and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("General Public Lists", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.group.admin=false and " +
				"p.group.public=false and p.group.parent is null and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("General Private Lists", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.group.admin=true and p.group.visible=true" +
				" and p.group.id != 1 and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("All-exec Lists", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.group.id = 14 and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("Deleted Posts", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.group.postName like 'anyone in the%' and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("Chapter Lists", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.group.parent!=null and " +
				"p.group.shortname='exec' and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("Chapter-exec Lists", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.group.parent!=null and " +
				"p.group.shortname!='exec' and p.group.public=true and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("Chapter Public Lists", numTypePosts);
		numPosts += numTypePosts;
		
		query = "select count(*) from PostModel as p where p.group.parent!=null and " +
				"p.group.shortname!='exec' and p.group.public=false and p.date>?";
		numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
				.list().get(0)).intValue();
		ds.setValue("Chapter Private Lists", numTypePosts);
		numPosts += numTypePosts;
		

		chart = ChartFactory.createPieChart("Post Group Breakdown (for "
		                                    + numPosts
		                                    + " posts)", ds,
		                                    false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}
	
	private JFreeChart getPost3Pie(Session s)
	{
		DefaultKeyedValues data = new DefaultKeyedValues();

		int numPosts = 0;
		int numTypePosts = 0;
		String query = "select count(*) from PostModel as p where p.date > ? and " +
				"(p.group=? or p.group.parent=?)";

		for (GroupChapterModel chapter: GroupChapterModel.getChapters())
		{
			numTypePosts = ((Long)s.createQuery(query).setDate(0, GraphServlet.getStartDate())
					.setEntity(1, chapter).setEntity(2, chapter).list().get(0)).intValue();
			numPosts += numTypePosts;
			data.addValue(chapter.getShortname(), numTypePosts);
		}

		return getChapterPareto(data, 
				"Posts by Chapter (for "+ numPosts + " posts)", 
				"Posts", "Number of Posts");
		
	}

	private JFreeChart getChapterPie(Session s)
	{
		DefaultKeyedValues data = new DefaultKeyedValues();

		for (GroupChapterModel chapter: GroupChapterModel.getChapters())
		{
			data.addValue(chapter.getShortname(), chapter.getNumMembers());
		}

		return getChapterPareto(data, 
				"Chapter Membership Breakdown By Chapter", 
				"Membership", "Number of Members");
	}

	


	private JFreeChart getChapterPareto(DefaultKeyedValues data, String title, String quantity, String range)
	{

		JFreeChart chart;
		
		data.sortByValues(SortOrder.DESCENDING);
		KeyedValues cummulative = DataUtilities.getCumulativePercentages(data);
		CategoryDataset dataset = DatasetUtilities.createCategoryDataset(quantity, data);
		    
		 // create the chart...
		chart = ChartFactory.createBarChart(
			title,  // chart title
			"Chapter",                     // domain axis label
			range,                     // range axis label
			dataset,                        // data
			PlotOrientation.VERTICAL,
			true,                           // include legend
			true,
			false
		);
		
		CategoryPlot plot = chart.getCategoryPlot();
		
		CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setLowerMargin(0.02);
		domainAxis.setUpperMargin(0.02);
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		
		// set the range axis to display integers only...
		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
		
		CategoryDataset dataset2 = DatasetUtilities.createCategoryDataset("Cummulative", cummulative);
		NumberAxis axis2 = new NumberAxis("Percent");
		axis2.setNumberFormatOverride(NumberFormat.getPercentInstance());
		axis2.setUpperBound(1);
		axis2.setLowerBound(0);
		plot.setRangeAxis(1, axis2);
		plot.setDataset(1, dataset2);
		plot.setRenderer(1, renderer2);
		plot.mapDatasetToRangeAxis(1, 1);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
		
		return chart;	
	}

	private JFreeChart getNoChapterPie()
	{
		JFreeChart chart;
		DefaultPieDataset ds = new DefaultPieDataset();

		Integer numChapter = Helpers.getGroup("Chapter").getNumMembers();
		ds.setValue("in a chapter", numChapter);

		ds.setValue("not in a chapter",
		            Helpers.getGroup("Org").getNumMembers() - numChapter);

		chart = ChartFactory.createPieChart("Chapter Membership Breakdown",
		                                    ds, false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}

	private JFreeChart getRankPie()
	{
		JFreeChart chart;
		DefaultPieDataset ds = new DefaultPieDataset();

		int numRegular = Helpers.getGroup("Regular").getNumMembers();
		int numAssociate = Helpers.getGroup("Associate").getNumMembers();
		int numUsers = Helpers.getGroup("Org").getNumMembers();

		ds.setValue("Regular Members", numRegular);
		ds.setValue("Associate Members", numAssociate);
		ds.setValue("Mailing list Members", numUsers - numAssociate - numRegular);

		chart = ChartFactory.createPieChart("Status breakdown for " + numUsers + " members", ds, 
				false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}
	
	private JFreeChart getChapterRankPie(Session s, String url)
	{
		String[] path = url.split("/");
		String lastPiece = path[path.length -1];
		Integer chapterId = new Integer(lastPiece.substring(0, lastPiece.length()-4));
		GroupChapterModel chapter = (GroupChapterModel) s.get(GroupChapterModel.class, chapterId);
		
		JFreeChart chart;
		DefaultPieDataset ds = new DefaultPieDataset();

		String query = "select count(*) as num from roles r1, roles r2 " +
				"where r1.userid=r2.userid and r1.groupid=? and r1.level='m' and r2.level='m' " +
				"and r1.end is null and r2.end is null and r2.groupid=?";
		int numRegular = ((Integer)s.createSQLQuery(query)
			.addScalar("num", Hibernate.INTEGER)
			.setInteger(0, chapter.getId())
			.setInteger(1, Helpers.getGroup("Regular").getId())
			.list().get(0)).intValue();

		int numAssociate = ((Integer)s.createSQLQuery(query)
			.addScalar("num", Hibernate.INTEGER)
			.setInteger(0, chapter.getId())
			.setInteger(1, Helpers.getGroup("Associate").getId())
			.list().get(0)).intValue();
		int numUsers = chapter.getNumMembers();

		ds.setValue("Regular Members", numRegular);
		ds.setValue("Associate Members", numAssociate);
		ds.setValue("Mailing list Members", numUsers - numAssociate - numRegular);

		chart = ChartFactory.createPieChart("Status breakdown for " + numUsers + " chapter members", ds, 
				false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}

	private JFreeChart getStudentPie(Session s)
	{
		JFreeChart chart;
		DefaultPieDataset ds = new DefaultPieDataset();

		String query = "select count(*) from UserModel as u where u.student='y'";
		int numStudent = ((Long)s.createQuery(query).list().get(0)).intValue();

		query = "select count(*) from UserModel as u where u.student='n'";

		int numNonStudent = ((Long)s.createQuery(query).list().get(0)).intValue();

		int numRegular = Helpers.getGroup("Regular").getNumMembers();
		int numAssociate = Helpers.getGroup("Associate").getNumMembers();
		
		ds.setValue("Student users", numStudent);
		ds.setValue("Unspecified", numAssociate + numRegular - numStudent - numNonStudent);
		ds.setValue("Non-Student users", numNonStudent);

		chart = ChartFactory.createPieChart("Student Status Breakdown for Associate/Regular members",
		                                    ds, false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}

	private JFreeChart getLanguagePie(Session s)
	{
		JFreeChart chart;
		DefaultPieDataset ds = new DefaultPieDataset();

		String query = "select count(*) from UserModel as u where u.language='en'";
		int numEn = ((Long)s.createQuery(query).list().get(0)).intValue();

		query = "select count(*) from UserModel as u where u.language='fr'";
		int numFr = ((Long)s.createQuery(query).list().get(0)).intValue();

		int numRegular = Helpers.getGroup("Regular").getNumMembers();
		int numAssociate = Helpers.getGroup("Associate").getNumMembers();
		ds.setValue("English", numEn);
		ds.setValue("Unspecified", numAssociate + numRegular - numEn - numFr);
		ds.setValue("French", numFr);

		chart = ChartFactory.createPieChart("Preferred Language Breakdown for Associate/Regular members",
		                                    ds, false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}

	private JFreeChart getGenderPie(Session s)
	{
		JFreeChart chart;
		DefaultPieDataset ds = new DefaultPieDataset();

		String query = "select count(*) from UserModel as u where u.gender='f'";
		int numFemale = ((Long)s.createQuery(query).list().get(0)).intValue();

		query = "select count(*) from UserModel as u where u.gender='m'";

		int numMale = ((Long)s.createQuery(query).list().get(0)).intValue();



		int numRegular = Helpers.getGroup("Regular").getNumMembers();
		int numAssociate = Helpers.getGroup("Associate").getNumMembers();
		ds.setValue("Female users", numFemale);
		ds.setValue("Unspecified", numAssociate + numRegular - numFemale - numMale);
		ds.setValue("Male users", numMale);

		chart = ChartFactory.createPieChart("Gender Breakdown for Associate/Regular members", ds,
		                                    false, false, false);

		PiePlot plot = ((PiePlot)chart.getPlot());
		StandardPieItemLabelGenerator n = new StandardPieItemLabelGenerator("{0} = {1} ({2})",
		                                                                    new DecimalFormat("0"),
		                                                                    new DecimalFormat("0.0%"));
		plot.setLabelGenerator(n);
		return chart;
	}

	private JFreeChart getListMemberships(Session s, int groupId, UserModel currentUser)
	{
		final int NUM_MONTHS = 6;
		Logger log = Logger.getLogger(this.getClass());
		JFreeChart chart;
		Date startDate;
		TreeMap<Day, Integer> memberChange = new TreeMap<Day, Integer>();
		GroupModel g = (GroupModel)s.get(GroupModel.class, groupId);

		if (g == null)
		{
			log.warn("Someone requested chapter stats with a bad group id");
		}
		else if (!Permissions.canAdministerGroupMembership(currentUser, g))
		{
			log.warn(currentUser.getUsername() + "tried to access list memberships graph!");
		}
		
		Calendar cal = GregorianCalendar.getInstance();
		Date now = cal.getTime();
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 1);
		cal.add(Calendar.MONTH, -NUM_MONTHS);
		startDate = cal.getTime();
		
		//Set Initial Number of Members		
		int initMembers = g.getNumMembers() + g.getNumRecipients();
		
		//Pull a list of all of the membership start dates
		List starts = s.createQuery("SELECT rm.start, count(*) " +
				"FROM RoleModel as rm " +
				"WHERE rm.group = :group " +
				"AND (rm.level = 'r' OR rm.level = 'm') " +
				"AND rm.start > :startDate " +
				"GROUP BY rm.start "
				).setEntity("group", g).setDate("startDate", startDate).list();
		
		for( Object o : starts )
		{
			Day date = new Day((Date)(((Object[])o)[0]));
			int change = ((Long)(((Object[])o)[1])).intValue();
			memberChange.put(date, change);
			initMembers -= change;
		}

		//Pull a list of all of the membership end dates
		List ends = s.createQuery("SELECT rm.end, count(*) " +
				"FROM RoleModel as rm " +
				"WHERE rm.group = :group " +
				"AND (rm.level = 'r' OR rm.level = 'm') " +
				"AND rm.end > :startDate " +
				"GROUP BY rm.end "
				).setEntity("group", g).setDate("startDate", startDate).list();
		
		for( Object o : ends )
		{
			Day date = new Day((Date)(((Object[])o)[0]));
			int change = ((Long)(((Object[])o)[1])).intValue();
			if( memberChange.containsKey(date) )
			{
				memberChange.put(date,
						memberChange.get(date)-change);
			}
			else
			{
				memberChange.put(date, -change);
			}
			initMembers += change;
		}
		
		TimeSeriesCollection theData = new TimeSeriesCollection();

		TimeSeries signups = new TimeSeries("List Membership", Day.class);
		theData.addSeries(signups);
		while( startDate.before(now))
		{
			Day d = new Day(startDate);
			if(memberChange.containsKey(d))
			{
				initMembers += memberChange.get(d);
			}
			signups.add(d, initMembers);
			cal.add(Calendar.DATE, 1);
			startDate = cal.getTime();
		}
		
		chart = ChartFactory.createTimeSeriesChart("List Members",
               "Day", "Number of Members", theData, true, true,
               true);

		XYPlot plot = (XYPlot)chart.getPlot();

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
		renderer.setSeriesStroke(0, new BasicStroke(2.0f));
		return chart;
	}
	
	
}
