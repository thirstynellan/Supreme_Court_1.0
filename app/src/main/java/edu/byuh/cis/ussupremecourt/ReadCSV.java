package edu.byuh.cis.ussupremecourt;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


public class ReadCSV {

	HashMap <Integer, Judge> apostleList = new  HashMap <Integer, Judge> ();
	//private ArrayList<Apostle> apostleList = new ArrayList<Apostle> ();
	private Bitmap apostleImage;
	
	public ReadCSV(AssetManager am, Context mainActivity, int bWidth, int bHeight ) {
		
		ConvertToObject(am, mainActivity, bWidth, bHeight);
		//printApostleList();
	}
	
//	public void resizeAllImages(int w, int h) {
//		for (Apostle a : apostleList.values()) {
//			Bitmap oldImage = a.getPhoto();
//			Bitmap newImage = Bitmap.createScaledBitmap(oldImage, w, h, true);
//			a.setPhoto(newImage);
//		}
//	}
	
	public void ConvertToObject (AssetManager am, Context mainActivity, int bWidth, int bHeight ) {
		String CSVFile = "judges"; // the csv file
		//AssetManager assetManager = getAssets();
		//BufferedReader br = null;
		//String line ="";
		String splitBy= "\\|";
		
		try{ 
			//br=new BufferedReader(new FileReader(CSVFile));
			InputStream is = am.open(CSVFile);
			//CSVReader csv = new CSVReader (new InputStreamReader(is));
			Scanner s = new Scanner(is);
			//while ((line = br.readLine()) != null ){
			while (s.hasNextLine()) {
				//split by comma, apostleAttributes comtain a single line of the csv file
				String line = s.nextLine();
				String[] apostleAttributes = line.split(splitBy);
				
				//instanciate a single apostle object
				Judge judge = new Judge();
				//set the arrtibutes of each apostle
				judge.setId(apostleAttributes[0]);
				Integer IDnumber  = new Integer (apostleAttributes[0]); // used for the hashmap
				judge.setName(apostleAttributes[1]);
				judge.setBirth(apostleAttributes[2]);
				judge.setDeath(apostleAttributes[3]);
				//get the file name of the apostle picture and change it to a bitmap object
				Log.d("CS203", apostleAttributes[4].trim());
				judge.resID = mainActivity.getResources().getIdentifier(apostleAttributes[4].trim(), "drawable", mainActivity.getPackageName());
				apostleImage = BitmapFactory.decodeResource(mainActivity.getResources(), judge.resID);
				apostleImage = Bitmap.createScaledBitmap(apostleImage, bWidth, bHeight, true);
				judge.setPhoto(apostleImage);
				
//				Log.d("the line",line);
//				Log.d("the bio",apostleAttributes[5]);
				try {
					
					InputStream is1 = am.open(apostleAttributes[5]);
					
					int size = is1.available();//get the size of the file.by block,not very reliable
					byte[] buffer = new byte[size];
					is1.read(buffer);
					//is1.close();
					//change byte buffer to string
					String text = new String (buffer);
//					Log.d("print the bio",text);
					judge.setBio(text);
				}
				
				catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				catch (IOException e){
					e.printStackTrace();
				}
				//add each apostle object to the apostleList
				apostleList.put(IDnumber, judge);
				//Log.d("debugging", apostleList.get(IDnumber).getName());
				//apostleList.add(apostle);
				
				
			}
			is.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
//	public void printApostleList( ) {
//		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
//		Log.d("zise of the hash", ""+apostleList.size());
//		for (int i =1; i<apostleList.size(); i++) { // hashmap has no index 0, as their id starts at 1		
//			Log.d("apostles",apostleList.get(i).getName() + ", his bio is: "+ apostleList.get(i).getBio());
//		}
//	}
	
//	public Calendar convertToCalendar (String date) {
//		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy");
//		Calendar cal = Calendar.getInstance();
//		try {
//			cal.setTime(df.parse(date));
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}
//	

}
