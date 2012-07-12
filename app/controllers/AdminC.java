package controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import models.Activity;
import models.Course;
import models.CourseCategory;
import models.CourseGroup;
import models.CourseSection;
import models.SocialUser;

import org.yaml.snakeyaml.Yaml;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.data.binding.Binder;
import play.data.binding.types.DateBinder;
import play.db.Model;
import play.mvc.Controller;
import play.mvc.With;
import play.test.Fixtures;
import play.vfs.VirtualFile;

@Check("admin")
@With(Secure.class)
public class AdminC extends Controller{
	
	public static final org.apache.log4j.Logger cLogger = Logger.log4j.getLogger(AdminC.class);
	
	public static void index() {
		render();
	}
	
	public static void uploadData() {
		render();
	}
	
	public static void manageCourses() {
		List<CourseCategory> categories = CourseCategory.findAll();
		List<Course> courses = Course.findAll();
		render(categories, courses);
	}
	
	public static void manageCourse(long id) {
		Course course = Course.findById(id);
		notFoundIfNull(course);
		List<CourseSection> sections = course.fetchSectionsByPlacement();
		notFoundIfNull(sections);
		render(course, sections);
	}
	
	public static void saveCourse(String title, String sections, long categoryId) {
		String callToActionString = "Put course contents here !";
		try {
			//Create the course
			Course course = new Course(title, callToActionString);
			CourseCategory category = CourseCategory.findById(categoryId);
			if(category != null) {
				course.category = category;
				course.save();
			}
			
			//Create sections
			BufferedReader reader = new BufferedReader(new StringReader(sections));
			String line = null;
			while((line = reader.readLine()) != null) {
				CourseSection section = new CourseSection(course, line, callToActionString);
				section.save();
			}
			manageCourses();
		} catch(Exception ioe) {
			flash.error("Could not create course and sections " + ioe);
			render("emptypage.html");
		}
	}
	
	public static void manageSection(long sectionId) {
		CourseSection section = CourseSection.findById(sectionId);
		notFoundIfNull(section);
		render(section);
	}
	
	public static void saveSection(long courseId, String title, String contents) {
		Course course = Course.findById(courseId);
		notFoundIfNull(course);
		CourseSection section = new CourseSection(course, title, contents);
		section.save();
		manageCourse(course.id);
	}
	
	public static void deleteSectionConfirm(long sectionId) {
		CourseSection section = CourseSection.findById(sectionId);
		notFoundIfNull(section);
		render(section);
	}
	
	public static void deleteSection(long sectionId) {
		CourseSection section = CourseSection.findById(sectionId);
		notFoundIfNull(section);
		section.delete();		
		manageCourse(section.course.id);
	}
	
	public static void changeActivityPlacements(long sectionId) {
		Map<String, String[]> all = params.all();
		for(String key : all.keySet()) {
			if(key.startsWith("placement")) {
				String sActivityId = key.substring(key.indexOf("-")+1);
				long activityId = Long.parseLong(sActivityId);
				Activity activity = Activity.findById(activityId);
				notFoundIfNull(activity);
				String placementArr[] = all.get(key);
				activity.placement = Integer.parseInt(placementArr[0]);
				activity.save();
			}
		}
		manageSection(sectionId);
	}
	
	public static void deleteActivityConfirm(long activityId, long sectionId) {
		Activity activity = Activity.findById(activityId);
		notFoundIfNull(activity);
		render(activity, sectionId);
	}
	
	public static void deleteActivity(long activityId, long sectionId) {
		CourseSection section = CourseSection.findById(sectionId);
		notFoundIfNull(section);
		Activity activity = Activity.findById(activityId);
		notFoundIfNull(activity);
		section.activities.remove(activity);
		section.save();
		try {
			if(activity.activityResponses.size() == 0) {
				activity.delete();
			} else {
				String  msg = "Did not delete activity with id " + activity.id + 
					      " because it has some responses. The activity has " +
					      " however, been removed from section " + section.id;
				flash.error(msg);
			}
						
		} catch(Exception e) {			
			//TODO: Generate a random number here for being able to quickly read the real exception
			cLogger.warn("Could not delete activity " + activity.id, e);
		}
		manageSection(sectionId);
	}
	
	public static void saveActivity(long sectionId, String title, String content) {
		CourseSection section = CourseSection.findById(sectionId);
		notFoundIfNull(section);
		Activity activity = new Activity(title, content);
		section.activities.add(activity);
		section.save();
		manageSection(sectionId);
	}
	
	public static void changePlacements(long courseId) {
		Map<String, String[]> all = params.all();
		for(String key : all.keySet()) {
			if(key.startsWith("placement")) {
				String sSectionId = key.substring(key.indexOf("-")+1);
				long sectionId = Long.parseLong(sSectionId);
				CourseSection section = CourseSection.findById(sectionId);
				notFoundIfNull(section);
				String placementArr[] = all.get(key);
				section.placement = Integer.parseInt(placementArr[0]);
				section.save();
			}
		}
		manageCourse(courseId);
	}
	
	public static void manageCourseGroups() {
		List<CourseGroup> courseGroups = CourseGroup.findAll();
		render(courseGroups);
	}
	
	public static void editCourseGroup(long id) {
		CourseGroup courseGroup = CourseGroup.findById(id);
		render(courseGroup);
	}
	
	public static void saveExistingCourseGroup(String sanitizedTitle, List<Long> courseParticipants) {
		CourseGroup courseGroup = CourseGroup.findBySanitizedTitle(sanitizedTitle);
		notFoundIfNull(courseGroup);
		courseGroup.users.clear();
		for(Long aParticipant : courseParticipants) {
			SocialUser aSocialUser = SocialUser.findById(aParticipant);
			if(aSocialUser != null) {
				courseGroup.users.add(aSocialUser);
			}
		}
		courseGroup.save();
		flash.success("The course group has been created successfully");
		manageCourseGroups();
	}
	
	public static void createCourseGroup() {
		List<Course> courses = Course.findAll();
		render(courses);
	}
	
	public static void newCourseGroup(String title, Long courseId, List<Long> courseParticipants) {
		Course course = Course.findById(courseId);
		notFoundIfNull(course);
		CourseGroup courseGroup = new CourseGroup(title, course);
		for(Long aParticipant : courseParticipants) {
			SocialUser aSocialUser = SocialUser.findById(aParticipant);
			if(aSocialUser != null) {
				courseGroup.users.add(aSocialUser);
			}
		}
		courseGroup.save();
		flash.success("The course group has been created successfully");
		manageCourseGroups();
	}
	
	public static void submitData(String data) {
		System.out.println("Adding data '" + data + "'");
		Pattern keyPattern = Pattern.compile("([^(]+)\\(([^)]+)\\)");
		
		try {
			Yaml yaml = new Yaml();
			Object o = yaml.load(data);
			
	        if (o instanceof LinkedHashMap<?, ?>) {
	            @SuppressWarnings("unchecked") LinkedHashMap<Object, Map<?, ?>> objects = (LinkedHashMap<Object, Map<?, ?>>) o;
	            Map<String, Object> idCache = new HashMap<String, Object>();
	            for (Object key : objects.keySet()) {
	                Matcher matcher = keyPattern.matcher(key.toString().trim());
	                if (matcher.matches()) {
	                    String type = matcher.group(1);
	                    String id = matcher.group(2);
	                    if (!type.startsWith("models.")) {
	                        type = "models." + type;
	                    }
	                    if (idCache.containsKey(type + "-" + id)) {
	                        throw new RuntimeException("Cannot load fixture data, duplicate id '" + id + "' for type " + type);
	                    }
	                    Map<String, String[]> params = new HashMap<String, String[]>();
	                    if (objects.get(key) == null) {
	                        objects.put(key, new HashMap<Object, Object>());
	                    }
	                    serialize(objects.get(key), "object", params);
	                    @SuppressWarnings("unchecked")
	                    Class<Model> cType = (Class<Model>)Play.classloader.loadClass(type);
	                    resolveDependencies(cType, params, idCache);
	                    Model model = (Model)Binder.bind("object", cType, cType, null, params);
	                    for(Field f : model.getClass().getFields()) {
	                        // TODO: handle something like FileAttachment
	                        if (f.getType().isAssignableFrom(Map.class)) {
	                            f.set(model, objects.get(key).get(f.getName()));
	                        }
	
	                    }
	                    System.out.println("About to save model '" + model + "'");
	                    model._save();
	                    Class<?> tType = cType;
	                    while (!tType.equals(Object.class)) {
	                        idCache.put(tType.getName() + "-" + id, Model.Manager.factoryFor(cType).keyValue(model));
	                        tType = tType.getSuperclass();
	                    }
	                }
	            }
	        }
	        // Most persistence engine will need to clear their state
	        for(PlayPlugin plugin : Play.plugins) {
	            plugin.afterFixtureLoad();
	        }
		} catch(Exception e) {
			render("errors/505.html", e);
		}
		index();
	}
	
	private static void serialize(Map<?, ?> values, String prefix, Map<String, String[]> serialized) {
        for (Object key : values.keySet()) {
            Object value = values.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof Map<?, ?>) {
                serialize((Map<?, ?>) value, prefix + "." + key, serialized);
            } else if (value instanceof Date) {
                serialized.put(prefix + "." + key.toString(), new String[]{new SimpleDateFormat(DateBinder.ISO8601).format(((Date) value))});
            } else if (value instanceof List<?>) {
                List<?> l = (List<?>) value;
                String[] r = new String[l.size()];
                int i = 0;
                for (Object el : l) {
                    r[i++] = el.toString();
                }
                serialized.put(prefix + "." + key.toString(), r);
            } else if (value instanceof String && value.toString().matches("<<<\\s*\\{[^}]+}\\s*")) {
                Matcher m = Pattern.compile("<<<\\s*\\{([^}]+)}\\s*").matcher(value.toString());
                m.find();
                String file = m.group(1);
                VirtualFile f = Play.getVirtualFile(file);
                if (f != null && f.exists()) {
                    serialized.put(prefix + "." + key.toString(), new String[]{f.contentAsString()});
                }
            } else {
                serialized.put(prefix + "." + key.toString(), new String[]{value.toString()});
            }
        }
    }
	
	private static void resolveDependencies(Class<Model> type, Map<String, String[]> serialized, Map<String, Object> idCache) {
        Set<Field> fields = new HashSet<Field>();
        Class<?> clazz = type;
        while (!clazz.equals(Object.class)) {
            Collections.addAll(fields, clazz.getDeclaredFields());
            clazz = clazz.getSuperclass();
        }
        for (Model.Property field : Model.Manager.factoryFor(type).listProperties()) {
            if (field.isRelation) {
                String[] ids = serialized.get("object." + field.name);
                if (ids != null) {
                    for (int i = 0; i < ids.length; i++) {
                        String id = ids[i];
                        id = field.relationType.getName() + "-" + id;
                        if (!idCache.containsKey(id)) {
                            throw new RuntimeException("No previous reference found for object of type " + field.name + " with key " + ids[i]);
                        }
                        ids[i] = idCache.get(id).toString();
                    }
                }
                serialized.remove("object." + field.name);
                serialized.put("object." + field.name + "." + Model.Manager.factoryFor((Class<? extends Model>)field.relationType).keyName(), ids);
            }
        }
    }

}
