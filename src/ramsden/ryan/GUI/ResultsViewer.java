package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import ramsden.ryan.Control;
import ramsden.ryan.Question;
import ramsden.ryan.SQL;

public class ResultsViewer {

	private TableDialog answerViewer;
	public HashMap<Integer, ArrayList<Answer>> seedStorage = new HashMap<Integer, ArrayList<Answer>>();
	private ScrollBar sb;
	private TextComponent results;
	private TextInput textInput;
	private Comparator<String> comparator;
	private String searchQuery;

	public ArrayList<Button> currAnswers = new ArrayList<Button>();
	public String currStudentName;
	public int currStudentID;
	private TextComponent currViewing;


	public ResultsViewer(Rectangle r) {
		init(r);

		comparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
				if(!s1.substring(0, searchQuery.length()).equals(s2.substring(0, searchQuery.length()))) {
					if(s1.startsWith(searchQuery)) return -1;
					if(s2.startsWith(searchQuery)) return 1;
				}
				return s1.compareTo(s2);
			}
		};
	}

	public void search(String text) {
		if(Control.manager == null) return;
		SQL sql = Control.manager.getSQL();
		if(sql == null || !sql.isConnected() || answerViewer.isHidden()) return;
		searchQuery = text;
		answerViewer.clear();
		seedStorage.clear();
		ResultSet rs;
		ArrayList<String> students = new ArrayList<String>();
		if(text.matches("[A-z]+")) rs = sql.query("SELECT * FROM `students` WHERE ((`firstname` LIKE '%"+text+"%') OR (`surname` LIKE '%"+text+"%'))"); 
		else rs = sql.query("SELECT * FROM `students` WHERE `studentID` LIKE '%"+text.replaceFirst("s", "")+"%'");
		try {
			while(rs.next()) {
				//Check if this user has any answers;
				students.add(rs.getString(2)+" "+rs.getString(3)+":"+rs.getString(1));
			}
		} catch (SQLException e) {}
		Collections.sort(students, comparator);
		for(String s : students) {
			int i=0, correct=0, id = Integer.parseInt(s.split(":")[1]);
			ResultSet ans = sql.query("SELECT * FROM `answers` WHERE `studentID`="+id);
			try { 
				ArrayList<Answer> seed = new ArrayList<Answer>();
				while(ans.next() || i == 0) { 
					i++;
					if(Integer.parseInt(ans.getString(4)) == 1) correct++;
					seed.add(new Answer(ans.getInt(2), ans.getString(3), Integer.parseInt(ans.getString(4)) == 1));


				};
				seedStorage.put(id, seed);
				answerViewer.addRow(new String[]{s.split(":")[0],s.split(":")[1], correct+"/"+i+" "+(int)(100*(double)correct/i)+"%"});
			} catch (SQLException e) {e.printStackTrace();}
		}
		if(answerViewer.getText(1, answerViewer.selectedRow) != null) {
			viewAnswers(answerViewer.getText(0, answerViewer.selectedRow),Integer.parseInt(answerViewer.getText(1, answerViewer.selectedRow)));
		}
		results.setText(answerViewer.rowArray.size() + " Results");
	}

	public void sort()
	{

	}

	public void init(Rectangle r)
	{
		answerViewer = new TableDialog(null, r, 3, 20, 50);
		answerViewer.parent = this;
		answerViewer.setCloseable(true);
		textInput = new TextInput(answerViewer, "", new Rectangle(20, 255, answerViewer.columnWidth[1], 20)) {
			@Override
			public void addCharacter(int keyChar, char key)
			{
				super.addCharacter(keyChar, key);
				search(text);
			}
			@Override
			public boolean validate(char c)
			{
				return (Character.isLetterOrDigit(c) || (c+"").equals(" "));
			}
		};
		Color c = Color.LIGHT_GRAY;
		new TextComponent(answerViewer, "Student Answers", new Point(10, 8), 18, Font.BOLD,c);
		new TextComponent(answerViewer, "Name", new Point(20, 30), 14, Font.BOLD,c);
		new TextComponent(answerViewer, "ID", new Point(35+answerViewer.columnWidth[1], 30), 14, Font.BOLD,c);
		new TextComponent(answerViewer, "Questions", new Point(20+answerViewer.columnWidth[2], 30), 14, Font.BOLD,c);
		results = new TextComponent(answerViewer, "", new Point(answerViewer.columnWidth[1]+25, 255), 14);
		currViewing = new TextComponent(answerViewer, "", new Point(20, 285), 14,Font.BOLD, c);
		sb = new ScrollBar(answerViewer, new Rectangle(answerViewer.columnWidth[3]+20, 50, 20, 201), 40, this);
		sb.setColor(TableDialog.bd);
		newScroll(0);
	}

	public void newScroll(double amount)
	{
		answerViewer.scroll = amount;
	}

	public ScrollBar getScrollBar() {
		return sb;
	}

	public boolean isHidden()
	{
		return answerViewer.isHidden();
	}

	public void setHidden(boolean b) {

		if(b && Control.manager != null && !Control.manager.isConnected()) return;
		answerViewer.setHidden(b);
		if(!isHidden()) {
			search(""); //This will just get the initial results.
			Control.selectedTextBox = textInput;
		}
	}

	public void toggle() {
		//answerViewer.centerOnScreen();
		setHidden(!answerViewer.isHidden());
	}

	public void viewAnswers(String studentName, final int studentID) {
		for(Button b : currAnswers) b.remove();
		currAnswers.clear();
		currStudentName = studentName;
		currStudentID = studentID;
		currViewing.setText(studentName);
		for (int i = 0; i < seedStorage.get(studentID).size(); i++) {
			final int seed = seedStorage.get(studentID).get(i).seed;
			final String ans = seedStorage.get(studentID).get(i).answer;
			Button b = new Button(answerViewer, new Rectangle(20+(i%5)*54, 310+(i/5)*54, 50, 50), (seed%2==0 ? "2D" : "1D")) {
				@Override
				public void click(Point p, boolean onClickDown) {
					super.click(p, onClickDown);
					if(!onClickDown) {
						Question q = new Question(seed);
						q.myAnswer = ans;
						Control.setQuestion(q);
					}
				}
			};
			new TextComponent(b, (1+i)+"." ,new Point(2,-4), 8, Font.PLAIN);
			b.getTextComponent().setSize(16);
			b.getTextComponent().setStyle(Font.BOLD);
			b.setColor(seedStorage.get(studentID).get(i).correct ? Question.Colour.GREEN.getColor() : Question.Colour.RED.getColor());
			b.centerText();
			currAnswers.add(b);
		}
	}

	class Answer {
		int seed;
		String answer;
		boolean correct;
		public Answer(int s, String string, boolean c) {
			seed = s;
			answer = string;
			correct = c;
		}
	}
}
