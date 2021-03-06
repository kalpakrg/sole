package controllers;

import models.Answer;
import models.Question;
import models.QuestionLiked;
import models.SocialUser;
import models.User;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.With;

@With(SocialAuthC.class)
public class QuestionC extends Controller {
	
	private static final org.apache.log4j.Logger cLogger = Logger.log4j.getLogger(QuestionC.class);
	
	public static void like(long questionId) {
		String textToRender = "";
		if(Security.isConnected()) {
			SocialUser user = SocialUser.findById(Long.parseLong(Security.connected()));
			Question question = Question.findById(questionId);
			if(user != null && question != null) {
				question.like(user);
				textToRender = String.valueOf(question.likes());
			} else {
				String msg = "Could not find ";
				if(user == null) {
					msg += " user '" + user.id + "' ";
				}
				if(question == null) {
					msg += " question '" + questionId + "'";
				}
				cLogger.error(msg);
			}
		}
		renderText(textToRender);
	}
	
	public static void likeAnswer(long answerId) {
		String textToRender = "";
		if(Security.isConnected()) {
			SocialUser user = SocialUser.findById(Long.parseLong(Security.connected()));
			Answer answer = Answer.findById(answerId);
			if(user != null && answer != null) {
				answer.like(user);
				textToRender = String.valueOf(answer.likes());
			} else {
				String msg = "Could not find ";
				if(user == null) {
					msg += " user '" + user.id + "' ";
				}
				if(answer == null) {
					msg += " question '" + answerId + "'";
				}
				cLogger.error(msg);
			}
		}
		renderText(textToRender);
	}
}
