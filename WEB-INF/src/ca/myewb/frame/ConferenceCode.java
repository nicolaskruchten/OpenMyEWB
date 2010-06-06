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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

import ca.myewb.model.ConferenceRegistrationModel;

public class ConferenceCode 
{
	private String codeString;
	private String typeCode;
	private int number;
	private static String secret = "try'n'guess me, betcha can't";
	private static Logger log = Logger.getLogger(ConferenceCode.class);
	
	private static Hashtable<String, String> types = new Hashtable<String, String>();
	static
	{
		types.put("a", "test type");
		types.put("b", "subsidized");
		types.put("c", "on/qc subsidized");
		types.put("d", "gta subsidized");
	}
	
	public int getNumber() {
		return number;
	}

	public String getCode() {
		return codeString;
	}


	public ConferenceCode(String code)
	{
		this.codeString = code.trim();
	}
	
	public String getType()
	{
		return ConferenceCode.types.get(typeCode);
	}
	
	public boolean isValid()
	{
		if(codeString == null || codeString.equals(""))
		{
			//empty
			return false;
		}
		
		String prefix = codeString.substring(0, 4);
		
		if(!generateCode(prefix).equals(codeString))
		{
			//malformed
			return false;
		}
		
		Criteria crit = HibernateUtil.currentSession()
			.createCriteria(ConferenceRegistrationModel.class)
			.add(Restrictions.eq("code", codeString))
			.add(Restrictions.eq("cancelled", false));
		if(!crit.list().isEmpty())
		{
			//already used
			return false;
		}
		
		// Check the conferene blacklist
		// Blacklisted codes should be in a file <root>/conference-blacklist.txt
		// one blacklisted code per line; you only need to specify the prefix
		// (letter + 3-digit code number, ie a001) anything else, is ignored
		try
		{
			BufferedReader file = new BufferedReader(new FileReader(Helpers.getLocalRoot() +
					"/conference-blacklist.txt"));
			String line = file.readLine();
			while (line != null)
			{
				if (line.length() > 0)
				{
					if (line.startsWith(prefix))
						return false;

					String[] blacklist = line.split("-");
					if (blacklist.length > 1)
					{
						char blacklistPrefix = line.charAt(0);

						if (blacklistPrefix == prefix.charAt(0))
						{
							int blStart = Integer.parseInt(blacklist[0].substring(1));
							int blEnd = Integer.parseInt(blacklist[1].substring(1));
							int currentCode = Integer.parseInt(prefix.substring(1));

							if (currentCode >= blStart && currentCode <= blEnd)
								return false;
						}
					}
				}
				
				line = file.readLine();
			}
		}
		catch (FileNotFoundException ex)
		{
			log.error("Can't open conference code blacklist!", ex);
		}
		catch (IOException ex)
		{
			log.error("Error reading conference code blacklist!", ex);
		}
		
		this.typeCode = prefix.substring(0,1);
		this.number = Integer.parseInt(prefix.substring(1));
		
		return true;
	}
	
	public static String generateCode(String prefix) 
	{
		return prefix + Helpers.md5(prefix + ConferenceCode.secret);
	}
	
	public static void main(String[] args) throws IOException
	{
		 BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		 System.out.println("How many codes would you like?");
		 int n = Integer.parseInt(br.readLine());
		 System.out.println("What type? (1 char)");
		 String type = br.readLine();
		 System.out.println("Starting at what number?");
		 int start = Integer.parseInt(br.readLine());
		 System.out.println("OK, here you go:");
		 DecimalFormat f = new DecimalFormat("000");
		 for(int i=start; i<start+n; i++)
		 {
			 System.out.println(ConferenceCode.generateCode(type + f.format(i)));
		 }
		 
	}
	
}
