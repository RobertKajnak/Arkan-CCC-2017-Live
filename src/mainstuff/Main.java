package mainstuff;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Collectors;

import javax.print.attribute.SupportedValuesAttribute;

public class Main {
	
	static HashMap<String, Loc> locations = new HashMap<String, Loc>();
	static ArrayList<Loc> locationsArray = new ArrayList<Loc>();
	static ArrayList<Loc> hyperStations = new ArrayList<Loc>();
	static Loc startJourney;
	static Loc endJourney;
	static Loc startHyper;
	static Loc endHyper;
	static int requiredJournies;
	static int numberOfLines;
	static double MaxDistance;
	//static HashMap< HashMap<Loc, Loc>, Double> journeys = new HashMap< HashMap<Loc, Loc>, Double>();
	static ArrayList<Journey> journeys = new ArrayList<Journey>();
	//static int numberOfNewJournes = 0;
	//ArrayList<Loc> locations = new ArrayList<Loc>();
	ArrayList<Loc> bestPath = new ArrayList<Loc>();
	double bestDistance = Double.MAX_VALUE;
	
	public static void main(String[] args) {
		readFile();
		
		boolean ok = false;
		long trials = 0;

		while (!ok) {
			
			trials++;
			if (trials>1000){
				System.out.println("Soon");
				trials = 0;
			}
			
			double d = MaxDistance;
			Loc start = locationsArray.get(new Random().nextInt(locationsArray.size()));
			ArrayList<Loc> path = new ArrayList<Loc>();
			path.add(start);
			
			while (d > 0) {
				//System.out.println("Building path" + trials);
				Loc loc = locationsArray.get(new Random().nextInt(locationsArray.size()));
				while (path.contains(loc)) {
					//System.out.println(loc.name);
					loc = locationsArray.get(new Random().nextInt(locationsArray.size()));
				}
				
				path.add(loc);
				//System.out.println(d);
				d -= loc.dist(path.get(path.size()-2));
			}
			
			ok = calculateDistance(path);
			if (ok) {
				System.out.print(path.size() - 1);
				
				for (Loc loc : path) {
					System.out.print(" " + loc.name);
				}
				
				//System.out.print("d= " + d + " min1= " + (d + path.get(path.size()-1).dist(path.get(path.size()-2))));
				return;
			}
		}
		
		
		
		//if (startJourney.driveTime(startHyper)<startJourney.driveTime(endJourney)){
		
		//rekt(MaxDistance, locationsArray.get(0), new ArrayList<Loc>());
		
		int maxFaster=0;int maxFasterOuter=0;
		Loc startLocTemp=null, endLocTemp =null,startLocTempOuter = null, endLocTempOuter=null;
		
		for (int i =0; i<locationsArray.size()-1;i++) {
			{
				
				for (int j = i+1; j< locationsArray.size();j++) {
					int nrOfFaster= isFaster(locationsArray.get(i), locationsArray.get(j));
					if (nrOfFaster>maxFaster){
						maxFaster = nrOfFaster;
						startLocTemp = locationsArray.get(i);
						endLocTemp = locationsArray.get(j);
					}
					//if (>requiredJournies){
							//nr++;
							//System.out.println(locationsArray.get(i).name + " " + locationsArray.get(j).name);
							//return;
					//}
					
				}
				if (maxFaster>maxFasterOuter){
					maxFasterOuter = maxFaster;
					startLocTempOuter = startLocTemp;
					endLocTempOuter = endLocTemp;
				}
			}
			
		}
		
		hyperStations.add(startLocTempOuter);
		hyperStations.add(endLocTempOuter);
		
		double totalDistance = startLocTempOuter.dist(endLocTempOuter);
		startLocTemp = endLocTempOuter;
		while (totalDistance<MaxDistance){
			startLocTemp = getFastest(startLocTemp);
			if (startLocTemp== null)
				break;
			totalDistance += startLocTemp.dist(hyperStations.get(hyperStations.size()-1));
			hyperStations.add(startLocTemp);
		}
		
		System.out.print(hyperStations.size()-1);
		for (Loc loc : hyperStations) {
			System.out.print(" " + loc.name);
		}
		
		//double t = (start.dist(end)/250.0 + 200);
		//System.out.println(nr);
	}
	
	public static Loc getFastest(Loc startLoc){
		int maxFaster = 0;
		Loc endLocTemp = null;
		for (int j = 0; j< locationsArray.size();j++) {
			if (hyperStations.contains(locationsArray.get(j)))
				continue;
					
			int nrOfFaster= isFaster(startLoc, locationsArray.get(j));
			if (nrOfFaster>maxFaster && endLocTemp!=null && startLoc.dist(locationsArray.get(j))>startLoc.dist(endLocTemp)){
				maxFaster = nrOfFaster;
				endLocTemp = locationsArray.get(j);
			}
			
		}
		return endLocTemp;
	}
	
	static boolean calculateDistance(ArrayList<Loc> hyperStations){
		
		int number = 0;
		
		for (Journey journey : journeys) {
		
			//System.out.println(journey.start.name);
			
			Loc hpl = getClosest(journey.start, hyperStations);
			Loc epl = getClosest(journey.end, hyperStations);
		
			int hpli = hyperStations.indexOf(hpl);
			int epli = hyperStations.indexOf(epl);
			if (hpli>epli){
				int c=epli;
				epli=hpli;
				hpli = c;
			}
			
			double totalTime = 0;
			for( int i=hpli; i<epli;i++){
				totalTime+=hyperStations.get(i).hyperTime(hyperStations.get(i+1));
			}
		
			 totalTime+=journey.start.driveTime(hpl) + journey.end.driveTime(epl);
		
			if (totalTime < journey.time) {
				number++;
			}
		}
		return number > requiredJournies;
	}
	
	public static Loc getClosest(Loc a, ArrayList<Loc> hyperStations) {
		double minDistance = Double.MAX_VALUE;
		Loc minLoc = null;
		
		for (Loc station : hyperStations) {
			if (station.dist(a) < minDistance) {
				minDistance = station.dist(a);
				minLoc = station;
			}
		}
		
		return minLoc;
	}
	
	public static int isFaster(Loc startHyper, Loc endHyper) {
		int total = 0;
		for (Journey journey : journeys) {
			double [] times = new double[3];
			times[0]= journey.time;
			times[1]= journey.start.driveTime(startHyper) + startHyper.hyperTime(endHyper) + endHyper.driveTime(journey.end);
			times[2] = journey.start.driveTime(endHyper) + startHyper.hyperTime(endHyper) + startHyper.driveTime(journey.end);
			
			double t = Math.min(Math.min(times[0], times[1]), times[2]);
			if (t < journey.time) {
				total++;
			}
		}
		return total;
	}
	
	public static void rekt(double dist, Loc loc, ArrayList<Loc> path) {
		if (dist < 0) {
			
			
			if (calculateDistance(path)) {
				System.out.println();
				System.out.println(path.size());
				for (int i = 0; i < path.size(); i++) {
					 
					System.out.print(" " + path.get(i).name);
				}
				
			}
			return;
		}
		else {
			for (Loc l : locationsArray) {
				
				double newD = dist - loc.dist(l);
				
				if (!path.contains(l)) {
					ArrayList<Loc> newPath = new ArrayList<Loc>();
					for (Loc p : path) {
						newPath.add(p);
					}
					newPath.add(l);
					rekt(newD, l, newPath);
				}
			}
		}
	}
	
	
	
	public static void readFile() {
		
		String path = "Inputs\\" + "level6-3.txt";
				
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(path));
			
			int n =	Integer.parseInt(br.readLine());
			
			for (int i=0;i<n;i++){
				String[] s = br.readLine().split(" ");
				Loc temp = new Loc(s[0], Integer.parseInt(s[1]), Integer.parseInt(s[2]));
				locations.put(s[0],temp);
				locationsArray.add(temp);
			}
			
			// Journeys
			int journeyNr = Integer.parseInt(br.readLine());
			
			for (int i = 0; i < journeyNr; i++) {
				String []s =br.readLine().split(" ");
				Loc jstart = locations.get(s[0]);
				Loc jend = locations.get(s[1]);
				double time = Double.parseDouble(s[2]);
				
				Journey newJourney = new Journey(jstart, jend, time);
				
				journeys.add(newJourney);
			}
			
			requiredJournies = Integer.parseInt(br.readLine());
			MaxDistance = Integer.parseInt(br.readLine());
			
			/*String s[] = br.readLine().split(" ");
			startJourney = locations.get(s[0]);
			endJourney = locations.get(s[1]);*/
			
			/*s= br.readLine().split(" ");
			numberOfLines = Integer.parseInt(s[0]);
			for (int i=1;i<=numberOfLines;i++){
				hyperStations.add(locations.get(s[i]));
			}*/
			
			/*String[] s =br.readLine().split(" ");
			startHyper = locations.get(s[0]);
			endHyper = locations.get(s[1]);*/
			
			
			br.close();
		}
		catch(Exception e){
			// Whatever
			System.err.println(e.getStackTrace());
		}
	}
	
	
}
class Loc {
	public double x;
	public double y;
	public String name;
	
	public Loc(String n, double a, double b) {
		this.name = n;
		x = a;
		y = b;
	}
	
	public double dist(Loc a) {
		return Math.sqrt((this.x - a.x) * (this.x- a.x) + (this.y-a.y) * (this.y-a.y));
	}
	
	public double hyperTime(Loc a) {
		double distance = this.dist(a);
		return (distance / 250.0) + 200;
	}
	
	public double driveTime(Loc a) {
		double distance = this.dist(a);
		return distance / 15.0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Loc other = (Loc) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(x) != Double.doubleToLongBits(other.x))
			return false;
		if (Double.doubleToLongBits(y) != Double.doubleToLongBits(other.y))
			return false;
		return true;
	}
}

class Journey {
	public Loc start;
	public Loc end;
	public double time;
	
	public Journey(Loc a, Loc b, double t) {
		start = a;
		end = b;
		time = t;
	}
}