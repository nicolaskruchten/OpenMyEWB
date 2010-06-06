#!/usr/bin/perl

$appPropertiesPath = "path/to/app.properties";

use Mail::Bulkmail  "path/to/cfg.file"; 
use MIME::Lite;
use Mysql;
use Text::Unaccent;

#load up props
%props = ();
open(PROPS, "< $appPropertiesPath");
while(<PROPS>)
{
    chomp;
    @line = split(/ = /, $_);
    $props{$line[0]} = $line[1];
}
close(PROPS); 



# Ensure only one instance of this script is running at a time by checking
#  for existance of temp file
if(!(-e "running2.txt"))
{
	#############################
	### SCRIPT INITIALIZATION ###
	#############################

	# Prepare to run by taking timestamp
	open(RESULTS, "> notrunning2.txt");
	$thestring = localtime;
	print RESULTS $thestring . "\n";
	close RESULTS;


	# Create temp file, indicating script is running		
	rename "notrunning2.txt", "running2.txt";


	# create a db connection
	$dbh = Mysql->connect('localhost', $props{"dbprefix"} . $props{"dbsuffix"}, $props{"dbuser"}, $props{"dbpass"});
	
	$dbh->query("SET AUTOCOMMIT = 1");

	# get emails that need sending
	$result = $dbh->query("SELECT id, recipients, shortname,
		sender, subject, textMessage, htmlMessage
		FROM email WHERE progress='waiting'");

	# Process each email individually
	while(@emails = $result->fetchrow) 
	{
		#######################
		### PARSE VARIABLES ###
		#######################

		# Parse fields into user-friendly variables!
		$id = $emails[0];
		# $emails[1] is the recipient list and will be parsed later
		$totalshortname = $emails[2];
		$sender = $emails[3];
		$subject = $emails[4];
		$textMessage = $emails[5];
		$htmlMessage = $emails[6];

		#print STDOUT "Processing email #$ id \n";
	
		# Attempt to fix the Article of the Week triple-send thing...
		# by marking this as "in progress".		
		$dbh->query("UPDATE email SET progress='sending' WHERE id=$id");

		# Retrieve email addresses and sort into list
		@recipients = split(', ', $emails[1]);

		# send to sender & admin
		$senderemail = $sender;
		$senderemail =~ s/.*<//;
		$senderemail =~ s/>.*//;
		
		if(($totalshortname ne $props{"enshortname"}) && ($totalshortname ne $props{"enshortname"}. "-watchlist"))
		{
			$recipients[scalar @recipients] = $senderemail;
		}

		for($i=0; $i < @recipients; $i++)
		{
			$recipients[$i] = lc($recipients[$i]);
		}

		# sort the list by domain & remove duplicates
		@temp = sort {(reverse $a) cmp (reverse $b)} @recipients;
		$prev = "not equal to $temp[0]";
		@recipients = grep($_ ne $prev && ($prev = $_, 1), @temp);

		# For stats
		$numsentto = @recipients;

		#################
		### SEND MAIL ###
		#################

		# create a message using MIME:Lite because we need multipart
		$msg = MIME::Lite->new( 
				Type		=> 'multipart/alternative',
				Datestamp	=> 0
			);

		# attach the plaintext version
		$textattachment = MIME::Lite->new(
				Type		=> 'text/plain',
				Data		=> $textMessage,
				Encoding        => "quoted-printable"
			);
		$textattachment->attr('content-type.charset' => 'ISO-8859-1');
		$msg->attach($textattachment);

		# attach the nicely formatter version
		$htmlattachment = MIME::Lite->new(
				Type		=> 'text/html',
				Data		=> $htmlMessage,
				Encoding        => "quoted-printable"
			);
		$htmlattachment->attr('content-type.charset' => 'ISO-8859-1');
		$msg->attach($htmlattachment);
		
		# create the bulkmail object to send to hundreds of people
		$bulk = Mail::Bulkmail->new(
				'LIST'		=> \@recipients,
				'Message'	=> $msg->body_as_string,
				'To'		=> 'list-' . $totalshortname . '@' . $props{"domain"},
				'From'		=> unac_string('ISO-8859-1', $sender),
				'Sender'	=> $props{"systememail"},
				'Subject'	=> "$subject "
			) || die 'Problem setting up email: ' . Mail::Bulkmail->error();


		# hack to deal with the multi-part text stuff
		@headerinfo = split /:\s|\n/, $msg->header_as_string ;

		for (my $i = 0; $i < @headerinfo; $i+=2) 
		{
			if($headerinfo[$i] eq "Content-Type")
			{
				$headerinfo[$i] = "Content-type";
			}
			$bulk->header($headerinfo[$i], $headerinfo[$i+1]);
		}

		# send the thing!
		$bulk->bulkmail || die 'Problem sending: ' . $bulk->error;

		# Log the mail and remove from queue
		$date = localtime;
		$dbh->query("UPDATE email SET progress='sent', numsentto='$numsentto', date='$date' WHERE id='$id'");
		
		# rolling cleanup (note: fewer than 100 left after other cleanup call)
		$deleteThreshold = $id - 1000;
		$dbh->query("delete from email WHERE progress='sent' and id <'$deleteThreshold'");

	}

	# db cleanup 2: get rid of passwords and useless welcome emails from queue
	$dbh->query("delete from email WHERE progress='sent' and shortname='" . $props{"enshortname"} . "'");

	################
	### CLEAN UP ###
	################
	rename "running2.txt", "notrunning2.txt";
	#print STDOUT "Finished all emails\n";
}
else
{
	#######################
	### SECOND INSTANCE ###
	#######################

	#print STDOUT "Another instance detected...\n";
	
	open(FILEHANDLE, "< running2.txt");
	@info = stat FILEHANDLE;
	@moddate = localtime($info[9]);
	@nowdate = localtime();
	
	# Deal with stale instances (ie, running for over an hour)
	if(($nowdate[2] - $moddate[2]) >= 2)
	{
		# remove the marker, so the next time this script is called it will run
		rename "running2.txt", "notrunning2.txt";
		open(RESULTS, ">>  lasterror.txt");
		$thestring = localtime;
		print RESULTS $thestring . "\n";
		close RESULTS;
		print STDOUT "Stale instance removed \n";
	}

}
