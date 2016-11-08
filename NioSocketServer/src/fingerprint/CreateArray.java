package fingerprint;
import java.io.*;
import java.util.Scanner;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
//http://blog.naver.com/hyuk4132/220687928458 엑셀 파일 불러오기 참고
public class CreateArray {
	
	
	public double[][][] createArray(){
		
		int num_of_x_coordinate,num_of_y_coordinate,start_minor,end_minor;
		num_of_x_coordinate=5;
		num_of_y_coordinate=12;
		start_minor=16692;
		end_minor=16697;
				
		//[0][][] ->average table  [1][][] -> var table
		double[][][] array=new double[2][][];
		array[0]=initArray(num_of_x_coordinate,num_of_y_coordinate,start_minor,end_minor);
		array[1]=initArray(num_of_x_coordinate,num_of_y_coordinate,start_minor,end_minor);		
		
		
		inputDataToArray(array[0],array[1]);
	
		return 	array;
	}
	
	/*
	 * initialize array  
	 * 
	 * 
	 */
	public double[][] initArray(int num_of_x_coordinate,int num_of_y_coordinate,int start_minor,int end_minor){
		double[][] array = new double[num_of_x_coordinate*num_of_y_coordinate+1][end_minor-start_minor+3];
		double coordinate_x=100,coordinate_y=100;
		final double MAX_COORDINATE_Y=num_of_y_coordinate*100;
		
		for(int i=0;i<array.length;i++){		
	
				if(i!=0){
						array[i][0]=coordinate_x;
						array[i][1]=coordinate_y;
				
						coordinate_y+=100;
						
						if(coordinate_y>MAX_COORDINATE_Y){
							coordinate_x+=100;
							coordinate_y=100;
						}								
				}
				else{		//initialize row 0
					for(int j=0;j<array[i].length;j++){
						if(j<2)
							array[i][j]=-9999;
						else{
							array[i][j]=start_minor+j-2;						
						}			
					}
				}			
		}
		return array;
	}

	public void inputDataToArray(double[][] average_arr,double[][] var_arr){
		int row_length=average_arr.length;
		int column_length=average_arr[0].length;	
		final int BA_Column=52;	//average
		final int BB_Column=53;	//var
		String temp_average,temp_var;
		int xls_row=1;
		
		try {
			 Workbook wb=Workbook.getWorkbook(new File("data.xls"));
			 Sheet s=wb.getSheet(0); // 인덱스 0 번의 시트
			 Cell c;
				for(int row=1;row<row_length;row++)
				{
					for(int column=2;column<column_length;column++){

						 c=s.getCell(BA_Column,xls_row);
						 temp_average=c.getContents();					

						 
						 if(temp_average.equals("#DIV/0"))
						 {
							 xls_row+=2;
						 }
						 c=s.getCell(BA_Column,xls_row);
						 temp_average=c.getContents();
												 
						 c=s.getCell(BB_Column,xls_row);
						 temp_var=c.getContents();
						 
						 average_arr[row][column]=Double.parseDouble(temp_average);
						 var_arr[row][column]=Double.parseDouble(temp_var);
						
						 xls_row++;
						 
					}
					
				}
		

		} catch (BiffException e) {	System.out.println("BiffException");} catch (IOException e) {System.out.println("파일존재하지 않음");	}		
	}
}
