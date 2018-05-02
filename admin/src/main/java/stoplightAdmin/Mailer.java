package jnm219.admin;

// Mailer class that sends email using a SendGrid SMTP server.
// https://sendgrid.com/docs/Integrate/Code_Examples/v3_Mail/java.html

import com.sendgrid.*;
import java.io.IOException;

public class Mailer
{
	Email mFrom = new Email("mts219@lehigh.edu");	// todo: allow caller to set pair of key and from values.
	String mApiKey = "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";

	Email mTo;			// SendGrid class
	String mSubject;
	Content mContent;	// SendGrid class
	Mail mMail;			// SendGrid class

	public Mailer(String to, String subject, String content)
	{
		mTo = new Email(to);
		mSubject = subject;
		mContent = new Content("text/plain", content);
	}

	// default constructor for testing only
	public Mailer()
	{
		this("mts219@lehigh.edu", "Java Mailer class", "Test message sent via SendGrid SMTP server.");
		//this("straathof@me.com", "hello", "world");	// call another constructor of this class.
	}

	public boolean send()
	{
		try
		{
			Request request = new Request();		// Java class for sending requests to a web server.
			mMail = new Mail(mFrom, mSubject, mTo, mContent);
			SendGrid sg = new SendGrid(mApiKey);	// SendGrid class

			request.setMethod(Method.POST);
			request.setEndpoint("mail/send");		// SendGrid should just send mail, not any fancy actions.
			request.setBody(mMail.build());			// build() returns a JSON string that setBody() wants.

			Response response = sg.api(request);	// send request to SMTP server and get response.

			if (response.getStatusCode() != 200 && response.getStatusCode() != 200)
				return false;	// something went wrong but not severe enough to throw an exception.

			// fall through
		}
		catch (IOException ex)
		{
			System.out.println(ex.getMessage());	// SendGrid messages contain JSON strings we don't bother to parse.
			return false;
		}
		return true;
	}
}