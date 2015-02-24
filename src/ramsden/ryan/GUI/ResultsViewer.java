package ramsden.ryan.GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import ramsden.ryan.Control;
import ramsden.ryan.SQL;

public class ResultsViewer {

	private TableDialog answerViewer;
	public HashMap<Integer, ArrayList<Answer>> seedStorage = new HashMap<Integer, ArrayList<Answer>>();
	private ScrollBar sb;
	private TextComponent results;
	private TextInput textInput;

	public ResultsViewer(Rectangle r) {
		init(r);
	}

	public void search(String text) {
		if(Control.manager == null) return;
		SQL sql = Control.manager.getSQL();
		if(sql == null || !sql.isConnected() || answerViewer.isHidden()) return;
		answerViewer.clear();
		seedStorage.clear();
		ResultSet rs;
		ArrayList<String> students = new ArrayList<String>();
		if(text.matches("[A-z]+")) rs = sql.query("SELECT * FROM `students` WHERE ((`firstname` LIKE '%"+text+"%') OR (`surname` LIKE '%"+text+"%'))"); 
		else rs = sql.query("SELECT * FROM `students` WHERE `studentID` LIKE '%"+text.replaceFirst("s", "")+"%'");
		try {
			while(rs.next()) {
				//Check if this user has any answers;
				students.add(rs.getString(1)+":"+rs.getString(2)+" "+rs.getString(3));
			}
		} catch (SQLException e) {}

		for(String s : students) {
			int i=0, correct=0, id = Integer.parseInt(s.split(":")[0]);
			ResultSet ans = sql.query("SELECT * FROM `answers` WHERE `studentID`="+id);
			try { 
				ArrayList<Answer> seed = new ArrayList<Answer>();
				while(ans.next() || i == 0) { 
					i++;
					seed.add(new Answer(ans.getInt(2), ans.getString(3)));
					if(Integer.parseInt(ans.getString(4)) == 1) correct++;
					
				};
				seedStorage.put(id, seed);
				answerViewer.addRow(new String[]{s.split(":")[1],s.split(":")[0], correct+"/"+i+" "+(int)(100*(double)correct/i)+"%"});
			} catch (SQLException e) {e.printStackTrace();}
		}

		results.setText(answerViewer.rowArray.size() + " Results");
	}

	public void sort()
	{

	}

	public void init(Rectangle r)
	{
		answerViewer = new TableDialog(null, r, 3, 20, 30, 40, 370);
		answerViewer.parent = this;
		answerViewer.setCloseable(true);
		textInput = new TextInput(answerViewer, "", new Rectangle(30, 235, 160, 20)) {
			@Override
			public void addCharacter(char keyChar)
			{
				super.addCharacter(keyChar);
				search(text);
			}
		};
		new TextComponent(answerViewer, "Name", new Point(35, 5), 16, Font.BOLD);
		new TextComponent(answerViewer, "Student #", new Point(35+answerViewer.columnWidth, 4), 16, Font.BOLD);
		new TextComponent(answerViewer, "Questions", new Point(35+answerViewer.columnWidth*2, 4), 16, Font.BOLD);
		results = new TextComponent(answerViewer, "0 Results", new Point(210, 237), 14);
		sb = new ScrollBar(answerViewer, new Rectangle(r.width-41, 30, 20, 200), 15, this);
		sb.setColor(Color.gray);
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
		answerViewer.centerOnScreen();
		setHidden(!answerViewer.isHidden());
	}

	public void viewAnswers(int studentID) {
		System.out.println("Viewing answers for student: "+studentID);
		for (int i = 0; i < seedStorage.get(studentID).size(); i++) {
			int seed = seedStorage.get(studentID).get(i).seed;
			String answer = seedStorage.get(studentID).get(i).answer;
			System.out.println("Seed: "+seed+" Answer: "+answer);
		}
		
	}
	
	class Answer {
		int seed;
		String answer;
		public Answer(int s, String string) {
			seed = s;
			answer = string;
		}
	}
}
