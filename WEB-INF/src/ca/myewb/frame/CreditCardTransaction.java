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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import ca.myewb.model.EmailModel;
import ca.myewb.model.UserModel;

public class CreditCardTransaction
{
	
	private Hashtable<String, String> input = new Hashtable<String, String>();
	private Hashtable<String, String> output = new Hashtable<String, String>();
	private float totalAmount = 0;
	private int itemCount = 0;
	private boolean attempted = false;
	private boolean succeeded = false;
	private String orderNumber;
	private Date date = null;
	
	public Date getDate() {
		return date;
	}

	public void setCardInfo(String owner, String number, String month, String year)
	{
		input.put("trnCardOwner", owner);
		input.put("trnCardNumber", number);
		input.put("trnExpMonth", month);
		input.put("trnExpYear", year);
	}
	
	public void setContactInfo(String name, String email, String phone, String address)
	{
		input.put("ordName", name);
		input.put("ordEmailAddress", email);
		input.put("ordPhoneNumber", phone);
		
		String[] splitAddress = address.split("\n");
		input.put("ordAddress1", (splitAddress.length > 0 && splitAddress[0] != null) ? splitAddress[0] : "");
		input.put("ordAddress2", (splitAddress.length > 2 && splitAddress[2] != null) ? splitAddress[2] : "");
		input.put("ordCity", (splitAddress.length > 3 && splitAddress[3] != null) ? splitAddress[3] : "");
		input.put("ordProvince", (splitAddress.length > 4 && splitAddress[4] != null) ? splitAddress[4] : "");
		input.put("ordPostalCode", (splitAddress.length > 5 && splitAddress[5] != null) ? splitAddress[5] : "");
		input.put("ordCountry", (splitAddress.length > 6 && splitAddress[6] != null) ? splitAddress[6] : "");
	}
	
	public void setContactInfo(UserModel user)
	{
		input.put("ordName", user.getFirstname() + " " + user.getLastname());
		input.put("ordEmailAddress", user.getEmail());
		input.put("ordPhoneNumber", user.getPhone());
		
		
		input.put("ordAddress1", user.getAddress1() + ", " + user.getSuite());
		input.put("ordAddress2", user.getAddress2());
		input.put("ordCity", user.getCity());
		input.put("ordProvince", user.getProvince());
		input.put("ordPostalCode", user.getPostalcode());
		input.put("ordCountry", user.getCountry());
	}
	
	public void addItem(String sku, int quantity, float cost, String name)
	{
		itemCount++;
		input.put("prod_id_" + itemCount, sku);
		input.put("prod_quantity_" + itemCount, new Integer(quantity).toString());
		input.put("prod_name_" + itemCount, name);
		input.put("prod_cost_" + itemCount, new Float(cost).toString());
		totalAmount += quantity*cost;
	}
	
	public void showOutput()
	{
		//for debugging purposes
		for(String key: output.keySet())
		{
			System.out.println(key + " = " + output.get(key));
		}
	}
	
	public void attemptTransaction(String trnPrefix) throws UnsupportedEncodingException, MalformedURLException, IOException
	{
		input.put("trnAmount", new Float(totalAmount).toString());
		orderNumber = trnPrefix + "-" + new Long(System.currentTimeMillis()).toString();
		input.put("trnOrderNumber", orderNumber);
		
		
		/*		
		SAMPLE CODE -- REPLACE WITH YOUR OWN PROCESSOR'S INTEGRATION CODE


 		//add these hard-coded bits 
		input.put("requestType", "BACKEND");
		input.put("merchant_id", "MERCHANT ID GOES HERE");
		
		//convert input hashtable into url string
		String inputString = "";
		for(String key: input.keySet())
		{
			inputString += URLEncoder.encode(key, "UTF-8") 
				+ "=" 
				+ URLEncoder.encode(input.get(key), "UTF-8")
				+ "&";
		}

		//set up the connection
	    URL url = new URL( "https://www.beanstream.com/scripts/process_transaction.asp" ); 
	    URLConnection conn = url.openConnection(); 
	    conn.setDoOutput( true ); 
	     
	    //send the input string
        OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
        wr.write(inputString);
        wr.flush();
    
        // grab the output string
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String outputString = "";
        String line;
        while ((line = rd.readLine()) != null) 
        {
            outputString += line;
        }
        wr.close();
        rd.close();
        
        //convert the output string into an output hashtable
        String[] kvPairs = outputString.split("&");
		for(String pair: kvPairs)
		{
			try
			{
				String[] kv = pair.split("=");
				output.put(URLDecoder.decode(kv[0], "UTF-8"), URLDecoder.decode(kv[1], "UTF-8"));
			}
			catch(ArrayIndexOutOfBoundsException e)
			{
				;
			}
		}
 		*/
		
		
		//the 2 following lines are placeholders
		output.put("errorType", "Y");
		output.put("messageText", "Credit card process integration not implemented");
		
		
		attempted = true;
		date = new Date();
		
		if(output.get("errorType").equals("N")
				&& output.get("messageText").equals("Approved"))
		{
			succeeded = true;
		}
		else
		{
			succeeded = false;
		}
	}
	

	public boolean isAttempted()
	{
		return attempted;
	}
	

	public boolean isSucceeded()
	{
		if(!attempted)
		{
			throw new IllegalStateException();
		}
		return succeeded;
	}

	public Hashtable<String, String> getOutput()
	{
		if(!attempted)
		{
			throw new IllegalStateException();
		}
		return output;
	}

	public String getOrderNumber()
	{
		return orderNumber;
	}

	public float getTotalAmount()
	{
		return totalAmount;
	}
	
	public String getEmail()
	{
		return input.get("ordEmailAddress");
	}
	
	public String getName()
	{
		return input.get("ordName");
	}
	
	public List<Hashtable<String, String>> getItems()
	{
		Vector<Hashtable<String, String>> items = new Vector<Hashtable<String, String>>();
		
		for(int i=1; i <= itemCount; i++)
		{
			Hashtable<String, String> item = new Hashtable<String, String>();
			item.put("sku", input.get("prod_id_" + i));
			item.put("quantity", input.get("prod_quantity_" + i));
			item.put("name", input.get("prod_name_" + i));
			item.put("cost", input.get("prod_cost_" + i));
			items.add(item);
		}
		
		return items;
	}

	public static String checkCardType(String cardType, String cardNum)
	{		
		if (cardType.equals("visa")) 
		{
			if (cardNum.length() != 16 || cardNum.charAt(0) != '4') 
			{
				return "This number is not a Visa number";
			}
		} 
		else if (cardType.equals("mc")) 
		{
			if (cardNum.length() != 16 || 
					(!cardNum.substring(0, 2).equals("36") && 
							(cardNum.charAt(0) != '5' || Integer.parseInt(cardNum.substring(1, 2)) >= 6))) 
			{
				return "This number is not a MasterCard number";
			}
		} 
		else if (cardType.equals("amex")) 
		{
			if (cardNum.length() != 15	|| 
					(!cardNum.substring(0, 2).equals("34") && !cardNum.substring(0, 2).equals("37"))) 
			{
				return "This number is not an American Express number";
			}
		} 
		else 
		{
			return "Unknown Card type";
		}
		
		return null;
	}

	public static boolean checkCardNo(String cardNum) {
	
		int oddoeven = cardNum.length() & 1;
		int sum = 0;
	
		for (int i = 0; i < cardNum.length(); i++) {
			int digit = cardNum.charAt(i) - '0';
			if (((i & 1) ^ oddoeven) == 0) {
				digit *= 2;
				if (digit > 9)
					digit -= 9;
			}
			sum += digit;
		}
		if (sum % 10 == 0)
			return true;
		else
			return false;
	}

	public static String checkExpiry(String expiryStr)
	{
		if(expiryStr.length() != 4)
		{
			return "Please use MMYY format when entering the expiry date";
		}
		
		int month = 0;
		int year = 0;
		
		try 
		{
			month = Integer.parseInt(expiryStr.substring(0,2));
			year = 2000 + Integer.parseInt(expiryStr.substring(2));
		}
		catch (Exception e) 
		{
			return "Please use MMYY format when entering the expiry date";
		}		
	
		Calendar cal = Calendar.getInstance();
		if((year < cal.get(Calendar.YEAR)) 
				|| ((month-1 < cal.get(Calendar.MONTH)) && (year == cal.get(Calendar.YEAR))))
		{
			return "Your card has expired, please check the date or use another card.";
		}
		
		return null;
	}

	public void sendReceipt(String subject) throws Exception
	{

		String bodyText = doTemplateMerge("emails/receipt.txt.vm");
		String bodyHTML = doTemplateMerge("emails/receipt.html.vm");
		
		Vector<String> emailAddress = new Vector<String>();
		emailAddress.add(getEmail());
		
		EmailModel.sendEmail(Helpers.getSystemEmail(), emailAddress, "[" + Helpers.getEnShortName() + "-receipt] Credit Card Transaction Receipt: " + subject, 
				bodyText, bodyHTML, "receipt", false);
	}
	
	private String doTemplateMerge(String templatePath) throws Exception 
	{
		Template template = Velocity.getTemplate(templatePath);
		VelocityContext ctx = new VelocityContext();
		ctx.put("txn", this);
		ctx.put("helpers", new Helpers());
	
		StringWriter writer = new StringWriter();
		template.merge(ctx, writer);
	
		String toString = writer.toString();
	
		return toString;
	}
}
