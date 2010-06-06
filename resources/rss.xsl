<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:template match="/rss">
<html>
    <head>
        <title><xsl:value-of select="channel/title"/></title>
    </head>
    <body style="margin: 20px;font-family:Verdana,sans-serif;">
    

	<h1  align="center">What's an RSS feed?</h1>

    	<table align="center">
	<tr>		
		<td align="right">


			<a title="Subscribe with Google">
				<xsl:attribute name="href">http://fusion.google.com/add?feedurl=<xsl:value-of select="channel/link" /></xsl:attribute>
				<img src="http://buttons.googlesyndication.com/fusion/add.gif" alt="Subscribe with Google" border="0" width="104" height="17" />
			</a>

			<br />
			<br />

			<a title="Subscribe with My Yahoo">
				<xsl:attribute name="href">http://us.rd.yahoo.com/my/atm/EWB/myEWB/*http://add.my.yahoo.com/rss?url=<xsl:value-of select="channel/link" /></xsl:attribute>
				<img src="http://us.i1.yimg.com/us.yimg.com/i/us/my/addtomyyahoo4.gif" alt="Subscribe with My Yahoo" border="0" width="91" height="17" />
			</a>

			<br />
			<br />

			<a title="Subscribe with My MSN">
				<xsl:attribute name="href">http://my.msn.com/addtomymsn.armx?id=<xsl:value-of select="channel/link" />&amp;ru=http://my.ewb.ca/</xsl:attribute>
				<img src="http://www.microsoft.com/library/media/1033/windows/rss/images/addtomymsn.gif" alt="Subscribe with My MSN" border="0" width="71" height="14" />
			</a>

			<br />
			<br />

			<a title="Subscribe with Bloglines">
				<xsl:attribute name="href">http://www.bloglines.com/sub/<xsl:value-of select="channel/link" /></xsl:attribute>
				<img src="http://www.bloglines.com/images/sub_modern11.gif" alt="Subscribe with Bloglines" border="0" width="76" height="17" />
			</a>

			<br />
			<br />

			<a title="Subscribe with NewsGator">
				<xsl:attribute name="href">http://www.newsgator.com/ngs/subscriber/subext.aspx?url=<xsl:value-of select="channel/link" /></xsl:attribute>
				<img src="http://www.newsgator.com/images/ngsub1.gif" alt="Subscribe with NewsGator" border="0" width="91" height="17" />
			</a>

			<br />
			<br />



		</td>
		<td width="20"></td>
		<td width="500">

		<p>This page is the human-readable version of one of <a href="http://my.ewb.ca/">myEWB</a>'s RSS feeds, which are document meants for computer programs to read in order to help you keep up to date with myEWB postings and happenings. You can give the address of this document to one of a variety of special programs which can make your online experience
		more convenient. We have listed a number of such programs to the left.
		Using one or more of these buttons, you can add myEWB's RSS feed to a feed reader you already use, 
		or get started using RSS feeds!</p>

	       <p> We encourage you to read the article 
	       <a href="http://news.bbc.co.uk/2/hi/help/3223484.stm" style="font-weight: bold;">What is RSS?</a> 
	       at the BBC website for more information.</p>


			<br />
		</td>
	</tr>
	<tr>
		<td colspan="3">

		       <p align="center">
			       <b>The address, or URL, for this feed is: 
					<a>
						<xsl:attribute name="href"><xsl:value-of select="channel/link" /></xsl:attribute>
						<xsl:value-of select="channel/link" />
					</a>
				</b>
			</p>

			<p style="width: 600px;" align="center"><b>Feed Description</b>: <xsl:value-of select="channel/description" /></p>

			<br />

			<p>Items in this feed:</p>

			<ul>
				<xsl:for-each select="channel/item">
					<li>
						<a>
							<xsl:attribute name="href"><xsl:value-of select="link" /></xsl:attribute>
							<xsl:value-of select="title"/>
						</a>
					</li>
				</xsl:for-each>
			</ul>
		</td>
	</tr>
	</table>
	

    </body>
</html>
</xsl:template>
</xsl:stylesheet>
