package models;

import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class SessionPart extends Model {
	
	public String title;	
	public Date startDate;
	public Date endDate;
	public String content;
	
	@Required
	@ManyToOne
	public StudySession studySession;
	
	@OneToMany(cascade=CascadeType.ALL)
	public Set<Activity> activities;
	
	public SessionPart(String title, 
					   Date startDate,
					   Date endDate,
					   String content) {
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.content = content;
		this.activities = new TreeSet<Activity>();
		create();
	}
}
