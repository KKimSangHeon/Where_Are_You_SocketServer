package db;

public class QueryData {
	public String name;
	public String classid;
	public int coordinate_x;
	public int coordinate_y;
	public boolean checker;
	
	
	public QueryData(String name , String classid, int coordinate_x, int coordinate_y , boolean checker){
		this.name=name;
		this.classid=classid;
		this.coordinate_x=coordinate_x;
		this.coordinate_y=coordinate_y;
		this.checker=checker;		
	}
}
