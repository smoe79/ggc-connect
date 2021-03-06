package edu.ggc.it.love;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.ggc.it.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * This activity prompts the user for their profile information
 * on their first run of the activity, or forward them to the
 * search page if the information has already been entered.
 * @author Jacob
 *
 */
public class SetupActivity extends Activity{
	private Button classButton;
	private Schedule schedule;
	private TableRow subjectRow;
	private Spinner subjectInput;
	private TableRow courseRow;
	private Spinner courseInput;
	private TableRow searchByRow;
	private ToggleButton searchByToggle;
	private TableRow sectionRow;
	private Spinner sectionInput;
	private String[] subjectIds;
	private Banner banner;
	private Map<String, Map<String, String>> courseMap;
	private Map<String, Map<String, String>> sectionMap;
	private boolean showCourseNumber;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.love_setup);
		
		banner = new Banner(this);
		
		courseMap = new HashMap<String, Map<String, String>>();
		sectionMap = new HashMap<String, Map<String, String>>();
		showCourseNumber = false;
		
		// find views
		subjectRow = (TableRow)findViewById(R.id.ds_subject_row);
		subjectInput = (Spinner)findViewById(R.id.ds_subject_input);
		courseRow = (TableRow)findViewById(R.id.ds_course_row);
		courseInput = (Spinner)findViewById(R.id.ds_course_input);
		searchByRow = (TableRow)findViewById(R.id.ds_search_by_row);
		searchByToggle = (ToggleButton)findViewById(R.id.ds_search_by_toggle);
		sectionRow = (TableRow)findViewById(R.id.ds_section_row);
		sectionInput = (Spinner)findViewById(R.id.ds_section_input);
		subjectIds = getResources().getStringArray(R.array.ds_subject_codes);
		
		// listen for buttons to be clicked
		SetupListener listener = new SetupListener();
		
		classButton = (Button)findViewById(R.id.ds_class_button);
		classButton.setOnClickListener(listener);
		searchByToggle.setOnClickListener(listener);
	}
	
	private enum AddStep {
		ADD, SUBJECT, COURSE, SECTION
	}
	
	private class SetupListener implements OnClickListener{
		private AddStep step = AddStep.ADD;
		
		@Override
		public void onClick(View button) {
			if (button.getId() == R.id.ds_class_button){
				switch (step){
					case ADD:
						showSubjects();
						step = AddStep.SUBJECT;
						break;
					case SUBJECT:
						showCourses();
						step = AddStep.COURSE;
						break;
					case COURSE:
						showSections();
						step = AddStep.SECTION;
						break;
					case SECTION:
						addCourse();
						step = AddStep.ADD;
						break;
				}
			} else if (button.getId() == R.id.ds_search_by_toggle){
				showCourseNumber = !showCourseNumber;
				showCourses();
			}
		}
		
		private void showSubjects(){
			subjectRow.setVisibility(View.VISIBLE);
			classButton.setText(R.string.ds_next_label);
		}
		
		private void showCourses(){
			int selected = subjectInput.getSelectedItemPosition();
			String code = subjectIds[selected];
			Map<String, String> courses = null;
			if (courseMap.containsKey(code)){
				courses = courseMap.get(code);
			} else{
				courses = banner.getCourseNumbers(code);
				courseMap.put(code, courses);
			}
			
			List<String> displayValues = null;
			if (showCourseNumber)
				displayValues = new ArrayList<String>(courses.keySet());
			else
				displayValues = new ArrayList<String>(courses.values());
			
			Collections.sort(displayValues);
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(SetupActivity.this,
					android.R.layout.simple_spinner_item, displayValues);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			courseInput.setAdapter(adapter);
			courseRow.setVisibility(View.VISIBLE);
			searchByRow.setVisibility(View.VISIBLE);
		}
		
		private void showSections(){
			int selected = subjectInput.getSelectedItemPosition();
			String subject = subjectIds[selected];
			String course = (String)courseInput.getSelectedItem();
			if (!showCourseNumber){
				// we need the number, but this is the name
				// TODO: this does not handle the user changing the subject after the course
				// has been selected
				for (Map.Entry<String, String> entry: courseMap.get(subject).entrySet()){
					if (entry.getValue().equals(course)){
						course = entry.getKey();
						break;
					}
				}
			}
			
			Map<String, String> sections = null;
			String courseCode = subject + course;
			if (sectionMap.containsKey(courseCode)){
				sections = sectionMap.get(courseCode);
			} else{
				sections = banner.getSections(subject, course);
				sectionMap.put(courseCode, sections);
			}
			
			List<String> sectionNumbers = new ArrayList<String>(sections.keySet());
			Collections.sort(sectionNumbers);
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(SetupActivity.this,
					android.R.layout.simple_spinner_item, sectionNumbers);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			sectionInput.setAdapter(adapter);
			sectionRow.setVisibility(View.VISIBLE);
			classButton.setText(R.string.ds_done_label);
		}
		
		private void addCourse(){
			Toast toast = Toast.makeText(SetupActivity.this, "not totally implemented",
					Toast.LENGTH_SHORT);
			toast.show();
			
			subjectRow.setVisibility(View.GONE);
			courseRow.setVisibility(View.GONE);
			searchByRow.setVisibility(View.GONE);
			sectionRow.setVisibility(View.GONE);
			classButton.setText(R.string.ds_add_class_label);
		}
		
	}
}
