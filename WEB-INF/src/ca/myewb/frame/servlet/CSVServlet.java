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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.myewb.frame.Helpers;


public class CSVServlet extends HttpServlet
{
	public void doGet(HttpServletRequest req, HttpServletResponse res)
	           throws ServletException, IOException
	{
		//just make an action that stores a couple of things in the session, then redirect here and bammo, the user gets a CSV file!
		//useful for member lists, mailing list lists, event attendees lists, regular member lists, etc etc etc
		Hashtable currentVars = (Hashtable)req.getSession()
		                        .getAttribute("interpageVars");

		String fileName = null;
		List csvData = null;

		try
		{
			fileName = (String)currentVars.remove("csvFileName");
			csvData = (List)currentVars.remove("csvData");
		}
		catch (Exception npe)
		{
			res.sendRedirect(Helpers.getDefaultURL());
		}

		if ((fileName == null) || (csvData == null))
		{
			res.sendRedirect(Helpers.getDefaultURL());
		}

		res.setContentType("application/csv");
		res.setCharacterEncoding("ISO-8859-1");
		res.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		ServletOutputStream outputStream = res.getOutputStream();
		
		PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream, "ISO-8859-1"));

		Iterator it = csvData.iterator();

		while (it.hasNext())
		{
			String[] row = (String[])it.next();

			for (int col = 0; col < row.length; col++)
			{
				if (row[col] != null)
				{
					//  not perfect validation; if someone purposely enters "" it will still break the csv
					// (must delimite all fields with "" in case they contain a newline or comma;
					// embedded " is indicated by "" apparently - http://www.edoceo.com/utilis/csv-file-format.php) 
					row[col] = row[col].replaceAll("\"", "\"\"");

					// And clean it up, remove extra whitespace
					while (row[col].indexOf("\n\n") != -1)
					{
						row[col] = row[col].replaceAll("\n\n", "\n");
					}

					// Finally write it
					out.write("\"" + row[col] + "\"");
				}
				else
				{
					out.write("");
				}

				if (col != (row.length - 1))
				{
					out.write(",");
				}
			}

			out.write("\n");
		}

		out.close();
	}
}
