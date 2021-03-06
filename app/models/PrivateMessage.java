package models;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import play.db.jpa.Model;

@Entity
public class PrivateMessage extends Model implements Comparable {
	
	@ManyToOne
	@JoinTable(name="PrivateMessage_From")
	public SocialUser from;
	
	@ManyToOne
	@JoinTable(name="PrivateMessage_To")
	public SocialUser to;
	
	public String subject;
	
	@Lob
	public String body;
	
	public Date timestamp;
	
	@OneToOne(mappedBy="message", cascade = CascadeType.ALL)
	public MessageProperties messageProperties;
	
	public PrivateMessage(SocialUser from, 
				   		  SocialUser to, 
				   		  String title, 
				   		  String body) {
		
		if(from == null) {
			throw new IllegalArgumentException("Parameter 'from' cannot be null");
		}
		if(to == null) {
			throw new IllegalArgumentException("Parameter 'to' cannot be null");
		}
		if(title == null) {
			throw new IllegalArgumentException("Parameter 'title' cannot be null");
		}
		if(body == null) {
			throw new IllegalArgumentException("Parameter 'body' cannot be null");
		}
		
		this.from = from;
		this.to = to;		
		this.subject = title;
		this.body = body;
		this.timestamp = new Date();
		this.messageProperties = new MessageProperties(this);
	}

	public static void send(SocialUser from,
							SocialUser to,
							String subject,
							String content) {
		PrivateMessage message = new PrivateMessage(from, to, subject, content);
		message.save();
		MessageCenter messageCenter = MessageCenter.findByUserId(message.from.id);
		messageCenter.inbox.add(message);
		messageCenter.save();
	}
	
	@Override
	public int compareTo(Object o) {
		PrivateMessage other = (PrivateMessage)o;
		
		long result = this.timestamp.getTime() - other.timestamp.getTime();
		
		int retVal = 0;
		if(result > 0) {
			retVal = 1;
		} else if (result < 0) {
			retVal = -1;
		} 
		
		return retVal;
	}
	
	@Override
	public String toString() {
		return this.from + " " + this.to + " '" + this.subject + "'"; 
	}
}