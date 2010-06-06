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

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;


public class BuildStaticFiles
{
	private static Hashtable<String, String> blueColors = new Hashtable<String, String>();
	private static Hashtable<String, String> greenColors = new Hashtable<String, String>();
	private static Hashtable<String, String> yellowColors = new Hashtable<String, String>();
	private static Hashtable<String, String> orangeColors = new Hashtable<String, String>();
	private static Hashtable<String, String> redColors = new Hashtable<String, String>();
	private static Hashtable<String, String> purpleColors = new Hashtable<String, String>();
	private static Hashtable<String, Hashtable> colorValues = new Hashtable<String, Hashtable>();

	{
		blueColors.put("dark", "#002BA2");
		blueColors.put("med", "#0049EE");
		blueColors.put("light", "#C2D1FF");

		greenColors.put("dark", "#00590F");
		greenColors.put("med", "#00A81C");
		greenColors.put("light", "#BCE2BC");

		yellowColors.put("dark", "#776000");
		yellowColors.put("med", "#F1CE00");
		yellowColors.put("light", "#FCF2BD");

		orangeColors.put("dark", "#964400");
		orangeColors.put("med", "#FF7900");
		orangeColors.put("light", "#FFCF9F");

		redColors.put("dark", "#750C0C");
		redColors.put("med", "#B71818");
		redColors.put("light", "#EFCACA");

		purpleColors.put("dark", "#530084");
		purpleColors.put("med", "#8B00CC");
		purpleColors.put("light", "#D9C1E5");

		colorValues.put("home", blueColors);
		colorValues.put("profile", greenColors);
		colorValues.put("chapter", yellowColors);
		colorValues.put("mailing", orangeColors);
		colorValues.put("events", redColors);
		colorValues.put("volunteering", purpleColors);
	}

	public static void main(String[] args)
	{
		try
		{
			//this class builds the style files, for performance, user caching etc
			(new BuildStaticFiles()).go();
			System.out.println("Static files successfully created!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void go() throws Exception
	{
		Velocity.init();

		Enumeration areas = colorValues.keys();

		while (areas.hasMoreElements())
		{
			String theArea = (String)areas.nextElement();

			VelocityContext ctx = new VelocityContext();
			ctx.put("areatab", "#" + theArea + "tab"); //to get around a wierd velocity thing
			ctx.put("area", theArea); //to get around a wierd velocity thing
			ctx.put("dark", colorValues.get(theArea).get("dark"));
			ctx.put("med", colorValues.get(theArea).get("med"));
			ctx.put("light", colorValues.get(theArea).get("light"));

			Enumeration localAreas = colorValues.keys();

			while (localAreas.hasMoreElements())
			{
				String localArea = (String)localAreas.nextElement();
				Enumeration shades = colorValues.get(localArea).keys();

				while (shades.hasMoreElements())
				{
					String shadeName = (String)shades.nextElement();
					ctx.put(localArea + shadeName,
					        colorValues.get(localArea).get(shadeName));
				}
			}

			Template template = null;

			template = Velocity.getTemplate("tmpl/frame/style.vm");

			Properties appProps = new Properties();
			java.net.URL url = ClassLoader.getSystemResource("app.properties");
			appProps.load(url.openStream());
			ctx.put("base", appProps.getProperty("appprefix"));

			StringWriter sw = new StringWriter();
			template.merge(ctx, sw);

			File theFile = new File("style/" + theArea + ".css");
			FileWriter writer = new FileWriter(theFile);
			writer.write(sw.toString());
			writer.flush();
			writer.close();
		}
	}
}
