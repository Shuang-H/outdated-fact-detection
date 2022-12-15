import java.io.*;
import java.util.*;

public class Selection {
	TreeMap<Double, ArrayList<Vertex>> vertexMap = new TreeMap<Double, ArrayList<Vertex>>();
	HashMap<String, Vertex> findVertexMap = new HashMap<String, Vertex>();
	HashMap<String, ArrayList<ArrayList<String>>> instanceMap = new HashMap<String, ArrayList<ArrayList<String>>>();
	
	public void Preprocess(String MLOutputFile, String KBFile, String vertexFile){
		HashMap<String, Double> hashMap = new HashMap<String, Double>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(MLOutputFile));
			String line = null;
			while((line = reader.readLine()) != null){
				String[] splits = line.split(",");
				String key = splits[0] + "," + splits[1] + "," + splits[2] + "," + splits[3] + "," + splits[4];
				double label = Double.parseDouble(splits[5]); 
				if(label >= 0.5){
					key = key + "," + 1;
				}else{
					key = key + "," + 0;
				}
				double weight = Double.parseDouble(splits[7]); 
				hashMap.put(key, weight);
			}
			reader.close();
			
			BufferedWriter writer = new BufferedWriter(new FileWriter(vertexFile));
			reader = new BufferedReader(new FileReader(KBFile));
			while((line = reader.readLine()) != null){
				String[] splits = line.split("\t");
				String fact = splits[0];
				splits = splits[1].split(" ");
				for(int i = 2; i < splits.length; i++){
					double value = Double.parseDouble(splits[i]);
					if((int) value == value){
						splits[i] = String.valueOf((int) value);
					}
				}
				double value = Double.parseDouble(splits[1]);
				splits[1] = String .format("%.6f",value);
				
				String key = splits[1] + "," + splits[2] + "," + splits[3] + "," + splits[4] + "," + splits[5] + "," + splits[0];
				if(hashMap.containsKey(key)){
					double weight = hashMap.get(key);
					writer.write(fact + "\t" + splits[0] + "\t" + weight);
					writer.newLine();
				}
				
			}
			reader.close();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void buildGraph(String vertexFile, String ruleFile){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(vertexFile));
			String line = null;
			while((line = reader.readLine()) != null){
				String[] splits = line.split("\t");
				String fact = splits[0];
				int label = Integer.parseInt(splits[1]); 
				double weight = Double.parseDouble(splits[2]);
				splits = fact.split(" ");
				/*
				String entity1 = splits[0].substring(splits[0].lastIndexOf("/")+1, splits[0].indexOf(">"));
				String relation = splits[1].substring(splits[1].lastIndexOf("/")+1, splits[1].indexOf(">"));
				String entity2 = fact.substring(line.indexOf(relation)+relation.length()+1);
				if(entity2.contains("\"") && !entity2.contains("<http://dbpedia.org/resource/")){
					entity2 = entity2.substring(entity2.indexOf("\"")+1, entity2.lastIndexOf("\""));
				}else{
					entity2 = entity2.substring(entity2.lastIndexOf("/")+1, entity2.lastIndexOf(">"));
				}
				*/
				if(splits.length != 3) continue;
				String entity1 = splits[0].substring(splits[0].indexOf("<")+1, splits[0].indexOf(">"));
				String relation = splits[1].substring(splits[1].indexOf("<")+1, splits[1].indexOf(">"));
				String entity2 = splits[2].substring(splits[2].indexOf("<")+1, splits[2].indexOf(">"));
				String triple = entity1 + " " + relation + " " + entity2;
				Vertex vertex = new Vertex(weight, label, triple);
				if(vertexMap.containsKey(weight)){
					vertexMap.get(weight).add(vertex);
				}else{
					ArrayList<Vertex> list = new ArrayList<Vertex>();
					list.add(vertex);
					vertexMap.put(weight, list);
				}
				findVertexMap.put(triple, vertex);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(ruleFile));
			String line = null;
			while((line = reader.readLine()) != null){
				String[] splits = line.split("\t");
				String key = splits[splits.length-1];
				ArrayList<String> list = new ArrayList<String>();
				for(int i = 0; i < splits.length-1; i++){
					list.add(splits[i]);
				}
				if(instanceMap.containsKey(key)){
					instanceMap.get(key).add(list);
				}else{
					ArrayList<ArrayList<String>> lists = new ArrayList<ArrayList<String>>();
					lists.add(list);
					instanceMap.put(key, lists);
				}
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void TopKSelection(int B, int k){
		int iterNum = B / k ;
		HashSet<String> hasAsked = new HashSet<String>();
		int hit = 0, expan = 0, correctExpan = 0;
		ArrayList<Double> weightList = new ArrayList<Double>(vertexMap.keySet());
		int currentPos = weightList.size()-1;
		for(int iter = 0; iter < iterNum; iter++){
			int count = 0;
			HashSet<String> askedTripleThisTime = new HashSet<String>();
			for(int i = currentPos; i >= 0; i--){
				double weight = weightList.get(i);
				ArrayList<Vertex> vertexList = vertexMap.get(weight);
				for(Vertex vertex : vertexList){
					if(!hasAsked.contains(vertex.triple)){
						hasAsked.add(vertex.triple);
						askedTripleThisTime.add(vertex.triple);
						if(vertex.label == 1) hit++;
						count++;
						if(count == k) break;
					}
				}
				currentPos--;
				if(count == k) break;	
			}	
			//expansion
			for(String triple : askedTripleThisTime){
				if(instanceMap.containsKey(triple)){
					ArrayList<ArrayList<String>> expanLists = instanceMap.get(triple);
					for(ArrayList<String> expanList : expanLists){
						if(expanList.size() == 1){
							String expanTriple = expanList.get(0);
							if(findVertexMap.containsKey(expanTriple)){
								expan++;
								if(findVertexMap.get(expanTriple).label == 1){
									correctExpan++;
								}
								hasAsked.add(expanTriple);
							}
						}
					}
				}
			}
		}
		System.out.println(hit + " " + expan + " " + correctExpan);
		double Precision = (double) (hit + correctExpan) / (B + expan);
		System.out.println("Precision = " + Precision);
		double Hit = (double) (hit) / (B);
		System.out.println("Hit = " + Hit);
	}
	
	public void InSelection(int B, int k){
		int iterNum = B / k ;
		HashSet<String> hasAsked = new HashSet<String>();
		int hit = 0, expan = 0, correctExpan = 0;
		ArrayList<Double> weightList = new ArrayList<Double>(vertexMap.keySet());
		int currentPos = weightList.size()-1;
		for(int iter = 0; iter < iterNum; iter++){
			int count = 0;
			HashSet<String> askedTripleThisTime = new HashSet<String>();
			HashSet<String> expanTripleThisTime = new HashSet<String>();
			for(int i = currentPos; i >= 0; i--){
				double weight = weightList.get(i);
				ArrayList<Vertex> vertexList = vertexMap.get(weight);
				for(Vertex vertex : vertexList){
					if(!hasAsked.contains(vertex.triple) && !expanTripleThisTime.contains(vertex.triple)){
						hasAsked.add(vertex.triple);
						askedTripleThisTime.add(vertex.triple);
						if(vertex.label == 1) hit++;
						if(instanceMap.containsKey(vertex.triple)){
							ArrayList<ArrayList<String>> expanLists = instanceMap.get(vertex.triple);
							for(ArrayList<String> expanList : expanLists){
								for(String expanTriple : expanList){
									expanTripleThisTime.add(expanTriple);
								}
							}
						}
						count++;
						if(count == k) break;
					}
				}
				currentPos--;
				if(count == k) break;	
			}
			//expansion
			for(String triple : askedTripleThisTime){
				if(instanceMap.containsKey(triple)){
					ArrayList<ArrayList<String>> expanLists = instanceMap.get(triple);
					for(ArrayList<String> expanList : expanLists){
						if(expanList.size() == 1){
							String expanTriple = expanList.get(0);
							if(findVertexMap.containsKey(expanTriple)){
								expan++;
								if(findVertexMap.get(expanTriple).label == 1){
									correctExpan++;
								}
								hasAsked.add(expanTriple);
							}
						}
					}
				}
			}
		}
		System.out.println(hit + " " + expan + " " + correctExpan);
		double Precision = (double) (hit + correctExpan) / (B + expan);
		System.out.println("Precision = " + Precision);
		double Hit = (double) (hit) / (B);
		System.out.println("Hit = " + Hit);
	}
	
	public static void main(String[] args){
		
	}
}
