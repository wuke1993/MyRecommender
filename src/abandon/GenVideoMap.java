package abandon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.recommender.utility.StoreStringIntoFile;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
* @author : wuke
* @date   : 20180424 19:34:15
* Title   : GenVideoMap
* Description : 弃用
*   根据课程视频关联度矩阵生成跨课程视频关联图谱
*/
public class GenVideoMap {
	private static String AdjacencyMatrix = "e:\\data\\my_kus\\video_relationship\\relationship_videos.txt";
	private static String VideosPath = "e:\\data\\my_kus\\video_relationship\\videos.txt";
	private static double[][] VideosAM = new double[224][224];
	private static String VideosAMPath = "e:\\data\\my_kus\\video_relationship\\video_am.json";
	
	public static void main(String[] args) {
		GenVideoMap.genVideoMap();
		JSONArray nodesJsonArr = GenVideoMap.genNodes(VideosPath);
		JSONArray edgesJsonArr = GenVideoMap.genEdges();
		
		ArrayList<String> courses_names = new ArrayList<String>();
		courses_names.add("operating_system");
		courses_names.add("computer_network");
		courses_names.add("java");
		GenVideoMap.genAll(courses_names, nodesJsonArr, edgesJsonArr, VideosAMPath);
	}
	
	public static void genVideoMap() {
		BufferedReader reader = null;
		String laststr = "";
		try {
			FileInputStream fileInputStream = new FileInputStream(GenVideoMap.AdjacencyMatrix);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			
			String tempString = null;
			String[] strArr = null;
			int i = 0;
			while ((tempString = reader.readLine()) != null) {
				strArr = tempString.split(" ");
				for (int j = 0; j < strArr.length; j++) {
					GenVideoMap.VideosAM[i][j] = Double.parseDouble(strArr[j]);
				}
				i++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		/*double sum = 0.0;
		double avg = 0.0; // 1.0384155861874416
		double max = 0.0; // 3.185551195577025
		for (int i = 0; i <224; i++) {
			for (int j = 0; j < 224; j++) {
				if (VideosAM[i][j] > max) {
					max = VideosAM[i][j];
				}
				sum += VideosAM[i][j];
			}
		}
		avg = sum / (224 * 224);
		System.out.println(avg + " " + max);*/
		
		for (int i = 0; i <224; i++) {
			for (int j = 0; j < 224; j++) {
				if (VideosAM[i][j] < 2.5) {
					VideosAM[i][j] = 0.0;
				}
			}
		}
	}
	
	private static JSONArray genNodes(String videosPath) {
        ArrayList<String> videos = new ArrayList<String>();
		
		File file = new File(videosPath);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			String str = null;
			while((str = reader.readLine()) != null) {
				videos.add(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		JSONArray jsonArr = new JSONArray();
		JSONObject json = null;
		Node node = null;
		int i = 0;
		for (String s : videos) {
			if (i < 100) {
				node = new Node(s, 1, 0, "rect"); // 操作系统原理的100个视频
			} else if (i > 153) {
				node = new Node(s, 1, 2, "rect"); // Java语言的70个视频
			} else {
				node = new Node(s, 1, 1, "rect"); // 计算机网络原理的54个视频
			}
						
			json = (JSONObject) JSON.toJSON(node);
			jsonArr.add(json);
			
			i++;
		}
		
		return jsonArr;
	}
    
    private static JSONArray genEdges() {
		JSONArray jsonArr = new JSONArray();
		JSONObject json = null;
		
		for (int i = 0; i < VideosAM.length; i++) {
			for (int j = 0; j < i; j++) {
				if (VideosAM[i][j] > 0.0) {
					int source = i;
					int target = j;
					
					json = new JSONObject();
					json.put("source", source);
					json.put("target", target);
					
					jsonArr.add(json);
				}				
			}
		}
		
		return jsonArr;
	}
    
    private static void genAll(ArrayList<String> courses_names, JSONArray nodesJsonArr, JSONArray edgesJsonArr, String storePath) {
    	JSONArray categoriesJsonArr = new JSONArray();
		for (String str : courses_names) {
			JSONObject course = new JSONObject();
			course.put("name", str);
			course.put("keyword", "{}");
			course.put("base", str);
	        categoriesJsonArr.add(course);
		}
		
		JSONObject all = new JSONObject();
		all.put("type", "force");
		all.put("categories", categoriesJsonArr);
		all.put("nodes", nodesJsonArr);
		all.put("links", edgesJsonArr);
		
		StoreStringIntoFile.storeString(all.toString(), storePath, false);
	}
}
