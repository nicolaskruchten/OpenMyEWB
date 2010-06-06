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

package ca.myewb.build;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import ca.myewb.frame.Helpers;
import ca.myewb.frame.HibernateUtil;
import ca.myewb.frame.SafeHibList;
import ca.myewb.model.EventModel;
import ca.myewb.model.GroupChapterModel;
import ca.myewb.model.GroupModel;
import ca.myewb.model.PostModel;
import ca.myewb.model.RoleModel;
import ca.myewb.model.UserModel;

public class DevDBSetup {
	
	public static void main(String[] args) throws Exception
	{
		HibernateUtil.createFactory("dev");
		Session session = HibernateUtil.currentSession();
		Transaction tx = session.beginTransaction();

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
		
		DevDBSetup.populate();
		
		tx.commit();
	}
	 
	@SuppressWarnings("unused")
	public static void populate() throws Exception
	{
		UserModel randyBachman, burtonCummings, sydBarrett, rogerWaters, davidGilmour, justinTimberlake,
			singerRecipient, singerSender, singerLeader, guesswhoMember;
		GroupChapterModel theGuessWho, pinkFloyd;
		GroupModel leadSingers, songwriters, bachmanTurnerOverdrive;
		PostModel gw, gwExec, pf, pfExec, ls, sw, bto;
		PostModel gwReply, gqExecReply, pfReply, pfExecReply, lsReply, swReply, btoReply;
		EventModel liveAtParamount, rehearsal, onTheRoad, setup;
		
		System.out.println("Populating users...");
		
		//User Population
		randyBachman = UserModel.newAssociateSignUp("randy@theguesswho.ca", "Randy", "Bachman", "guesswho");
		burtonCummings = UserModel.newAssociateSignUp("burton@theguesswho.ca", "Burton", "Cummings", "guesswho");
		sydBarrett = UserModel.newAssociateSignUp("syd@pinkfloyd.co.uk", "Syd", "Barrett", "pinkfloyd");
		rogerWaters = UserModel.newAssociateSignUp("roger@pinkfloyd.co.uk", "Roger", "Waters", "pinkfloyd");
		davidGilmour = UserModel.newAssociateSignUp("david@pinkfloyd.co.uk", "David", "Gilmour", "pinkfloyd");
		justinTimberlake = UserModel.newAssociateSignUp("justin@nsuck.com", "Justin", "Timberlake", "istink");
		singerRecipient = UserModel.newMailingListSignUp("recipient@singers.org");
		singerSender = UserModel.newMailingListSignUp("sender@singers.org");
		singerLeader = UserModel.newMailingListSignUp("leader@singers.org");
		guesswhoMember = UserModel.newMailingListSignUp("guesswhomember@guesswho.ca");
		
		System.out.println("Populating chapters...");
		
		//Chapter Setup
		theGuessWho = GroupChapterModel.newChapter();
		pinkFloyd = GroupChapterModel.newChapter();
		
		theGuessWho.save("Guess Who Professional Chapter", "guesswho", "1 Guess St.\n\n\nWinnipeg\nMB\nH0H0H0\nCA", "(416) 222-3561", "(416) 222-3561", "chapter@theguesswho.ca", "http://theguesswho.ca/", false, true);
		pinkFloyd.save("Pink Floyd University Chapter", "pinkfloyd", "1 Pink Ln.\n\n\nLondon\nON\nH0H0H0\nCA", "(416) 222-3561", "(416) 222-3561", "chapter#pinkfloyd.co.uk", "http://pinkfloyd.co.uk/", false, false);

		System.out.println("Populating non-chapter lists...");
		
		//Group Setup
		leadSingers = GroupModel.newGroup();
		songwriters = GroupModel.newGroup();
		bachmanTurnerOverdrive = GroupModel.newGroup(theGuessWho);
		
		leadSingers.save("Lead Singers", "leadsingers", "Group for the lead singers of bands", false);
		songwriters.save("Songwriters", "songwriters", "A group for any songwriters", true);
		bachmanTurnerOverdrive.save("Bachman-Turner Overdrive", "BTO", "For those who went on to BTO", false);

		System.out.println("Populating chapter members...");
		
		//Putting people in groups
		randyBachman.joinChapter(theGuessWho);
		randyBachman.renew(randyBachman, true);
		randyBachman.upgradeToExec();
		randyBachman.setExecTitle("Exec member");
		burtonCummings.joinChapter(theGuessWho);
		sydBarrett.joinChapter(pinkFloyd);
		rogerWaters.joinChapter(pinkFloyd);
		rogerWaters.upgradeToExec();
		rogerWaters.setExecTitle("Exec member");
		davidGilmour.joinChapter(pinkFloyd);
		davidGilmour.upgradeToExec();
		davidGilmour.setExecTitle("President");
		davidGilmour.addGroup(Helpers.getGroup("UniPresidents"), 'm');
		guesswhoMember.joinChapter(theGuessWho);
		
		System.out.println("Populating non-chapter list members...");
		
		//Putting people in Lists
		davidGilmour.upgradeToListLeader(leadSingers);
		rogerWaters.subscribe(leadSingers);
		randyBachman.upgradeToListSender(leadSingers);
		
		randyBachman.subscribe(bachmanTurnerOverdrive);

		randyBachman.subscribe(songwriters);
		burtonCummings.upgradeToListSender(songwriters);
		davidGilmour.subscribe(songwriters);
		rogerWaters.upgradeToListLeader(songwriters);
		
		singerRecipient.subscribe(leadSingers);
		singerSender.upgradeToListSender(leadSingers);
		singerLeader.upgradeToListLeader(leadSingers);		
		
		System.out.println("Fudging signup dates...");
		Criteria roleCrit = HibernateUtil.currentSession().createCriteria(RoleModel.class);
		List<RoleModel> roles = new SafeHibList<RoleModel>(roleCrit).list();
		Calendar c = GregorianCalendar.getInstance();
		c.add(Calendar.MONTH, -3);
		Date d = c.getTime();
		
		for(RoleModel r : roles)
		{
			if(r.getGroup().getAdmin())
			{
				r.setStart(d);
			}
			else
			{
				c = GregorianCalendar.getInstance();
				c.add(Calendar.DATE, (int)(Math.random() * (-90)));
				r.setStart(c.getTime());
			}
		}
		

		System.out.println("Populating posts...");
		
		//Making Posts
		gw = PostModel.newPost(burtonCummings, theGuessWho, "The Guess Who", "Guess Who's First Post", "I dunno", "guess who, canada");
		gwExec = PostModel.newPost(randyBachman, theGuessWho.getExec(), "The Guess Who Exec", "Guess Who's First Exec List Post", "I do know!", "guess who, canada, exec");
		pf = PostModel.newPost(rogerWaters, pinkFloyd, "Pink Floyd", "Pink Floyd's First Post", "Up agains teh wall", "pink floyd, psychadelic rock");
		pfExec = PostModel.newPost(davidGilmour, pinkFloyd.getExec(), "Pink Floyd Execs", "Pink Floyd's First Post", "Wish you were here!", "pink floyd, psychadelic rock");
		ls = PostModel.newPost(davidGilmour, leadSingers, "Lead Singers", "Lead Singers's First Post", "haha. we rock", "lead singers, rock");
		sw = PostModel.newPost(rogerWaters, songwriters, "Songwriters", "Songwriters's First Post", "they write, they write yay", "writing, music");
		bto = PostModel.newPost(randyBachman, bachmanTurnerOverdrive, "Bachman Turner Overdrive", "BTO's First Post", "you ain't seen nothing yet", "bto, canada");

		System.out.println("Fudging post dates...");
		Criteria postCrit = HibernateUtil.currentSession().createCriteria(PostModel.class);
		List<PostModel> posts = new SafeHibList<PostModel>(postCrit).list();

		for(PostModel p : posts)
		{
			c = GregorianCalendar.getInstance();
			c.add(Calendar.DATE, (int)(Math.random() * (-90)));
			p.setDate(c.getTime());
		}
		
		System.out.println("Populating Replies...");
		gw.reply(randyBachman, "GW Reply", "");
		gwExec.reply(randyBachman, "GW Exec Reply", "");
		pf.reply(davidGilmour, "PF Reply", "");
		pfExec.reply(davidGilmour, "PF Exec Reply", "");
		ls.reply(davidGilmour, "Lead Singers Reply", "");
		sw.reply(rogerWaters, "Writers Reply", "");
		bto.reply(randyBachman, "BTO Reply", "");
		
		System.out.println("Fudging reply dates...");
		Criteria replyCrit = HibernateUtil.currentSession().createCriteria(PostModel.class);
		replyCrit.add(Restrictions.isNotNull("parent"));
		List<PostModel> replies = new SafeHibList<PostModel>(replyCrit).list();
		
		for(PostModel r : replies)
		{
			Date now = new Date();
			c.setTime(r.getParent().getDate());
			Date before = c.getTime();
			
			c = GregorianCalendar.getInstance();
			c.add(Calendar.DATE, (int)(Math.random() * (before.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)));
			r.setDate(c.getTime());
		}
		
				
		System.out.println("Populating events...");
		
		//Creating Events
		c = GregorianCalendar.getInstance();
		c.set(2007, 6, 31, 10, 30);
		Date startDate = c.getTime();
		c.add(Calendar.DATE, 2);
		Date endDate = c.getTime();
		
		rehearsal = EventModel.newEvent("Band Rehearsal", startDate, endDate, "Bachman's Basement", "Garageband", theGuessWho, false, "");
		
		c.set(2007, 7, 5, 5, 23);
		startDate = c.getTime();
		c.add(Calendar.WEEK_OF_YEAR, 1);
		endDate = c.getTime();
		onTheRoad = EventModel.newEvent("Driving", startDate, endDate, "The i95", "On the road again...", theGuessWho, false, "");

		c.set(2007, 7, 15, 12, 49);
		startDate = c.getTime();
		c.add(Calendar.HOUR, 7);
		endDate = c.getTime();
		setup = EventModel.newEvent("Setup", startDate, endDate, "Outside Paramount", "roadies!", theGuessWho, false, "");

		c.set(2007, 7, 15, 20, 35);
		startDate = c.getTime();
		c.add(Calendar.HOUR, 4);
		endDate = c.getTime();
		liveAtParamount = EventModel.newEvent("Concert: Live at the Paramount", startDate, endDate, "Paramount Stage", "Yeah for concerts", theGuessWho, false, "");
		
	}
}
